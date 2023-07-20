package com.topstep.fitcloud.sample2.data.device

import android.content.Context
import androidx.annotation.IntDef
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.polidea.rxandroidble3.exceptions.BleDisconnectedException
import com.topstep.fitcloud.sample2.data.config.ExerciseGoalRepository
import com.topstep.fitcloud.sample2.data.db.AppDatabase
import com.topstep.fitcloud.sample2.data.entity.DeviceBindEntity
import com.topstep.fitcloud.sample2.data.entity.toModel
import com.topstep.fitcloud.sample2.data.storage.InternalStorage
import com.topstep.fitcloud.sample2.data.user.UserInfoRepository
import com.topstep.fitcloud.sample2.data.wh.WomenHealthRepository
import com.topstep.fitcloud.sample2.fcSDK
import com.topstep.fitcloud.sample2.model.device.ConnectorDevice
import com.topstep.fitcloud.sample2.model.device.ConnectorState
import com.topstep.fitcloud.sample2.model.user.UserInfo
import com.topstep.fitcloud.sample2.model.wh.WomenHealthConfig
import com.topstep.fitcloud.sample2.utils.launchWithLog
import com.topstep.fitcloud.sample2.utils.runCatchingWithLog
import com.topstep.fitcloud.sdk.connector.FcConnectorState
import com.topstep.fitcloud.sdk.connector.FcDisconnectedReason
import com.topstep.fitcloud.sdk.exception.FcSyncBusyException
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import com.topstep.fitcloud.sdk.v2.features.*
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.FcFunctionConfig
import com.topstep.fitcloud.sdk.v2.model.config.FcWomenHealthConfig
import com.topstep.fitcloud.sdk.v2.model.data.*
import com.topstep.fitcloud.sdk.v2.model.settings.FcBatteryStatus
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

interface DeviceManager {

    val flowDevice: StateFlow<ConnectorDevice?>

    val flowState: StateFlow<ConnectorState>

    val flowBattery: StateFlow<FcBatteryStatus?>

    /**
     * Sync data state of [FcSyncState].
     * Null for current no any sync state
     */
    val flowSyncState: StateFlow<Int?>

    /**
     * [SyncEvent]
     */
    val flowSyncEvent: Flow<Int>

    /**
     * Does need weather
     */
    fun flowWeatherRequire(): Flow<Boolean>

    /**
     * Trying bind a new device.
     * If bind success, the device info will be automatically saved to storage
     */
    fun bind(address: String, name: String)

    /**
     * Rebind current device
     * If bind success, the device info will be automatically saved to storage
     */
    fun rebind()

    /**
     * Cancel if [bind] or [rebind] is in progress
     * Otherwise do nothing.
     */
    fun cancelBind()

    /**
     * Unbind device and clear the device info in the storage.
     */
    suspend fun unbind()

    /**
     * Reset device and clear the device info in the storage.
     */
    suspend fun reset()

    /**
     * When state is [ConnectorState.PRE_CONNECTING], get the number of seconds to retry the connection next time
     */
    fun getNextRetrySeconds(): Int

    /**
     * When state is [ConnectorState.DISCONNECTED], get the reason
     */
    fun getDisconnectedReason(): FcDisconnectedReason

    val configFeature: FcConfigFeature

    val settingsFeature: FcSettingsFeature

    val dataFeature: FcDataFeature

    val messageFeature: FcMessageFeature

    val specialFeature: FcSpecialFeature

    fun disconnect()

    fun reconnect()

    fun newDfuManager(): FcDfuManager

    fun syncData()

    @IntDef(
        SyncEvent.SYNCING,
        SyncEvent.SUCCESS,
        SyncEvent.FAIL_DISCONNECT,
        SyncEvent.FAIL,
    )
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.BINARY)
    annotation class SyncEvent {
        companion object {
            const val SYNCING = 0//正在同步
            const val SUCCESS = 1//同步成功
            const val FAIL_DISCONNECT = 2//同步失败，因为连接断开
            const val FAIL = 3//同步失败
        }
    }
}

fun DeviceManager.flowStateConnected(): Flow<Boolean> {
    return flowState.map { it == ConnectorState.CONNECTED }.distinctUntilChanged()
}

fun DeviceManager.isConnected(): Boolean {
    return flowState.value == ConnectorState.CONNECTED
}

/**
 * Manage device connectivity and status
 */
internal class DeviceManagerImpl(
    context: Context,
    private val applicationScope: CoroutineScope,
    private val internalStorage: InternalStorage,
    private val userInfoRepository: UserInfoRepository,
    private val womenHealthRepository: WomenHealthRepository,
    private val exerciseGoalRepository: ExerciseGoalRepository,
    private val syncDataRepository: SyncDataRepository,
    appDatabase: AppDatabase,
) : DeviceManager {

    private val fcSDK = context.fcSDK
    private val connector = fcSDK.connector
    private val configDao = appDatabase.configDao()

    /**
     * Manually control the current device
     */
    private val deviceFromMemory: MutableStateFlow<ConnectorDevice?> = MutableStateFlow(null)

    /**
     * Flow device from storage
     */
    private val deviceFromStorage = internalStorage.flowAuthedUserId.flatMapLatest {
        //ToNote:Clear device in memory every time Authed user changed. Avoid connecting to the previous user's device after switching users
        deviceFromMemory.value = null
        if (it == null) {
            flowOf(null)
        } else {
            configDao.flowDeviceBind(it)
        }
    }.map {
        it.toModel()
    }

    /**
     * Combine device [deviceFromMemory] and [deviceFromStorage]
     */
    override val flowDevice: StateFlow<ConnectorDevice?> = deviceFromMemory.combine(deviceFromStorage) { fromMemory, fromStorage ->
        Timber.tag(TAG).i("device fromMemory:%s , fromStorage:%s", fromMemory, fromStorage)
        check(fromStorage == null || !fromStorage.isTryingBind)//device fromStorage, isTryingBind must be false

        //Use device fromMemory first
        fromMemory ?: fromStorage
    }.stateIn(applicationScope, SharingStarted.Eagerly, null)

    /**
     * Connector state combine adapter state and current device
     */
    override val flowState = combine(
        flowDevice,
        fcSDK.observerAdapterEnabled().startWithItem(fcSDK.isAdapterEnabled()).asFlow().distinctUntilChanged(),
        connector.observerConnectorState().map { simpleState(it) }.startWithItem(ConnectorState.DISCONNECTED).asFlow().distinctUntilChanged()
    ) { device, isAdapterEnabled, connectorState ->
        //Device trying bind success,save it
        if (device != null && device.isTryingBind && connectorState == ConnectorState.CONNECTED) {
            saveDevice(device)
        }
        combineState(device, isAdapterEnabled, connectorState)
    }.stateIn(applicationScope, SharingStarted.Eagerly, ConnectorState.NO_DEVICE)

    override val flowSyncState: StateFlow<Int?> = connector.dataFeature()
        .observerSyncState().asFlow().stateIn(applicationScope, SharingStarted.Lazily, null)

    private val _flowSyncEvent = Channel<Int>()
    override val flowSyncEvent = _flowSyncEvent.receiveAsFlow()

    init {
        applicationScope.launch {
            //Connect or disconnect when device changed
            userInfoRepository.flowCurrent.combine(flowDevice) { user, device ->
                ConnectionParam(user, device)
            }.collect {
                if (it.device == null || it.user == null) {
                    connector.close()
                } else {
                    connector.connect(
                        address = it.device.address,
                        userId = it.user.id.toString(),
                        bindOrLogin = it.device.isTryingBind,
                        sex = it.user.sex,
                        age = it.user.age,
                        height = it.user.height.toFloat(),
                        weight = it.user.weight.toFloat(),
                    )
                }
            }
        }
        applicationScope.launch {
            flowState.collect {
                Timber.tag(TAG).e("state:%s", it)
                if (it == ConnectorState.CONNECTED) {
                    onConnected()
                }
            }
        }
        applicationScope.launch {
            exerciseGoalRepository.flowCurrent.drop(1).collect {
                if (flowState.value == ConnectorState.CONNECTED) {
                    applicationScope.launchWithLog {
                        settingsFeature.setExerciseGoal(it.step, (it.distance * 1000_00).toInt(), it.calorie * 1000).await()
                    }
                }
            }
        }
        applicationScope.launch {
            womenHealthRepository.flowCurrent.drop(1).collectLatest {
                delay(1000)
                if (flowState.value == ConnectorState.CONNECTED) {
                    setWomenHealth(it)
                }
            }
        }
    }

    private fun onConnected() {
        val userId = internalStorage.flowAuthedUserId.value
        if (userId != null) {
            applicationScope.launchWithLog {
                if (connector.isBindOrLogin()) {//This connection is in binding mode
                    //Clear the Step data of the day
                    runCatchingWithLog {
                        syncDataRepository.saveTodayStep(userId, null)
                    }
                }

                runCatchingWithLog {
                    Timber.tag(TAG).i("setExerciseGoal")
                    exerciseGoalRepository.flowCurrent.value.let {
                        settingsFeature.setExerciseGoal(it.step, (it.distance * 1000_00).toInt(), it.calorie * 1000).await()
                    }
                }

                runCatchingWithLog {
                    val config = womenHealthRepository.flowCurrent.value
                    setWomenHealth(config)
                }

                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    syncData()
                }
            }
        } else {
            Timber.tag(TAG).w("onConnected error because no authed user")
        }
    }

    private suspend fun setWomenHealth(config: WomenHealthConfig?) {
        Timber.tag(TAG).i("device set WomenHealth")
        //The device don't support, return
        if (!configFeature.getDeviceInfo().isSupportFeature(FcDeviceInfo.Feature.WOMEN_HEALTH)) {
            return
        }
        val appConfig = womenHealthRepository.getConfigForDevice(config) ?: return
        val deviceConfig = configFeature.getWomenHealthConfig()

        if (appConfig.getMode() == FcWomenHealthConfig.Mode.NONE
            && deviceConfig.getMode() == FcWomenHealthConfig.Mode.NONE
        ) {
            //It's all NONE, so there's no need to change anymore
            return
        }
        if (appConfig != deviceConfig) {
            Timber.tag(TAG).i("device apply WomenHealth")
            connector.configFeature().setWomenHealthConfig(appConfig).await()
        }
    }

    override val flowBattery: StateFlow<FcBatteryStatus?> = flowState
        .filter { it == ConnectorState.CONNECTED }
        .flatMapLatest {
            Observable
                .interval(1000, 7500, TimeUnit.MILLISECONDS)
                .flatMap {
                    connector.settingsFeature().requestBattery().toObservable()
                }
                .retryWhen {
                    it.flatMap { throwable ->
                        if (throwable is BleDisconnectedException) {
                            //not retry when disconnected
                            Observable.error(throwable)
                        } else {
                            //retry when failed
                            Observable.timer(7500, TimeUnit.MILLISECONDS)
                        }
                    }
                }
                .asFlow()
                .catch {
                    //catch avoid crash
                }
        }
        .stateIn(applicationScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L), null)

    override fun flowWeatherRequire(): Flow<Boolean> {
        return flowDevice.flatMapLatest {
            if (it == null || it.isTryingBind) {
                flowOf(false)
            } else {
                connector.configFeature().observerAnyChanged().filter { type ->
                    type == FcConfigFeature.TYPE_DEVICE_INFO || type == FcConfigFeature.TYPE_FUNCTION_CONFIG
                }.debounce(1000, TimeUnit.MILLISECONDS).startWithItem(0).asFlow()
                    .map {
                        val deviceInfo = connector.configFeature().getDeviceInfo()
                        val functionConfig = connector.configFeature().getFunctionConfig()
                        deviceInfo.isSupportFeature(FcDeviceInfo.Feature.WEATHER) &&
                                functionConfig.isFlagEnabled(FcFunctionConfig.Flag.WEATHER_DISPLAY)
                    }
            }
        }
    }

    override fun bind(address: String, name: String) {
        val userId = internalStorage.flowAuthedUserId.value
        if (userId == null) {
            Timber.tag(TAG).w("bind error because no authed user")
            return
        }
        deviceFromMemory.value = ConnectorDevice(address, name, true)
        applicationScope.launchWithLog {
            configDao.clearDeviceBind(userId)
        }
    }

    override fun rebind() {
        val device = flowDevice.value
        if (device == null) {
            Timber.tag(TAG).w("rebind error because no device")
            return
        }
        bind(device.address, device.name)
    }

    override fun cancelBind() {
        val device = deviceFromMemory.value
        if (device != null && device.isTryingBind) {
            deviceFromMemory.value = null
        }
    }

    override suspend fun unbind() {
        connector.settingsFeature().unbindUser()
            .ignoreElement().onErrorComplete()
            .andThen(
                connector.settingsFeature().unbindAudioDevice().onErrorComplete()
            ).await()
        clearDevice()
    }

    override suspend fun reset() {
        connector.settingsFeature().deviceReset().await()
        clearDevice()
    }

    /**
     * Save device with current user
     */
    private suspend fun saveDevice(device: ConnectorDevice) {
        val userId = internalStorage.flowAuthedUserId.value
        if (userId == null) {
            Timber.tag(TAG).w("saveDevice error because no authed user")
            deviceFromMemory.value = null
        } else {
            deviceFromMemory.value = ConnectorDevice(
                device.address, device.name, false
            )
            val entity = DeviceBindEntity(userId, device.address, device.name)
            configDao.insertDeviceBind(entity)
        }
    }

    /**
     * Clear current user's device
     */
    private suspend fun clearDevice() {
        deviceFromMemory.value = null
        internalStorage.flowAuthedUserId.value?.let { userId ->
            configDao.clearDeviceBind(userId)
        }
    }

    override fun getNextRetrySeconds(): Int {
        return 0L.coerceAtLeast((connector.getNextRetryTime() - System.currentTimeMillis()) / 1000).toInt()
    }

    override fun getDisconnectedReason(): FcDisconnectedReason {
        return connector.getDisconnectedReason()
    }

    override val configFeature: FcConfigFeature = connector.configFeature()
    override val settingsFeature: FcSettingsFeature = connector.settingsFeature()
    override val dataFeature: FcDataFeature = connector.dataFeature()
    override val messageFeature: FcMessageFeature = connector.messageFeature()
    override val specialFeature: FcSpecialFeature = connector.specialFeature()

    override fun disconnect() {
        connector.disconnect()
    }

    override fun reconnect() {
        connector.reconnect()
    }

    override fun newDfuManager(): FcDfuManager {
        return connector.newDfuManager(true)
    }

    override fun syncData() {
        if (connector.dataFeature().isSyncing()) {
            return
        }
        applicationScope.launch {
            connector.dataFeature().syncData().asFlow()
                .onStart {
                    Timber.tag(TAG).i("syncData onStart")
                    _flowSyncEvent.send(DeviceManager.SyncEvent.SYNCING)
                }
                .onCompletion {
                    Timber.tag(TAG).i(it, "syncData onCompletion")
                    when (it) {
                        null -> {
                            _flowSyncEvent.send(DeviceManager.SyncEvent.SUCCESS)
                            val userId = internalStorage.flowAuthedUserId.value
                            if (userId != null) {
                                //Clear all gpsId every time synchronization is completed
                                //Because if the synchronization is all successful, then GpsData and SportData should have all returned successfully.
                                syncDataRepository.clearSportGpsId(userId)
                            }
                        }
                        is BleDisconnectedException -> {
                            _flowSyncEvent.send(DeviceManager.SyncEvent.FAIL_DISCONNECT)
                        }
                        !is FcSyncBusyException -> {
                            _flowSyncEvent.send(DeviceManager.SyncEvent.FAIL)
                        }
                    }
                }
                .catch {
                }
                .collect {
                    saveSyncData(it)
                }
            Timber.tag(TAG).i("syncData finish")
        }
    }

    private suspend fun saveSyncData(data: FcSyncData) {
        Timber.tag(TAG).i("saveSyncData:%d", data.type)
        val userId = internalStorage.flowAuthedUserId.value ?: return

        when (data.type) {
            FcSyncDataType.STEP -> {
                syncDataRepository.saveStep(userId, data.toStep(), data.deviceInfo.isSupportFeature(FcDeviceInfo.Feature.STEP_EXTRA))
            }

            FcSyncDataType.SLEEP -> syncDataRepository.saveSleep(userId, data.toSleep())

            FcSyncDataType.HEART_RATE -> syncDataRepository.saveHeartRate(userId, data.toHeartRate())
            FcSyncDataType.HEART_RATE_MEASURE -> syncDataRepository.saveHeartRate(userId, data.toHeartRateMeasure())

            FcSyncDataType.OXYGEN -> syncDataRepository.saveOxygen(userId, data.toOxygen())
            FcSyncDataType.OXYGEN_MEASURE -> syncDataRepository.saveOxygen(userId, data.toOxygenMeasure())

            FcSyncDataType.BLOOD_PRESSURE -> syncDataRepository.saveBloodPressure(userId, data.toBloodPressure())
            FcSyncDataType.BLOOD_PRESSURE_MEASURE -> syncDataRepository.saveBloodPressureMeasure(userId, data.toBloodPressureMeasure())

            FcSyncDataType.TEMPERATURE -> syncDataRepository.saveTemperature(userId, data.toTemperature())
            FcSyncDataType.TEMPERATURE_MEASURE -> syncDataRepository.saveTemperature(userId, data.toTemperatureMeasure())

            FcSyncDataType.PRESSURE -> syncDataRepository.savePressure(userId, data.toPressure())
            FcSyncDataType.PRESSURE_MEASURE -> syncDataRepository.savePressure(userId, data.toPressureMeasure())

            FcSyncDataType.ECG -> {
                syncDataRepository.saveEcg(userId, data.toEcg(), data.deviceInfo.isSupportFeature(FcDeviceInfo.Feature.TI_ECG))
            }

            FcSyncDataType.GAME -> syncDataRepository.saveGame(userId, data.toGame())

            FcSyncDataType.SPORT -> syncDataRepository.saveSport(userId, data.toSport())
            FcSyncDataType.GPS -> syncDataRepository.saveGps(userId, data.toGps())

            FcSyncDataType.TODAY_TOTAL_DATA -> syncDataRepository.saveTodayStep(userId, data.toTodayTotal())
        }
    }

    companion object {
        private const val TAG = "DeviceManager"
    }

    private data class ConnectionParam(
        val user: UserInfo?,
        val device: ConnectorDevice?,
    )
}

private fun simpleState(state: FcConnectorState): ConnectorState {
    return when {
        state == FcConnectorState.DISCONNECTED -> ConnectorState.DISCONNECTED
        state == FcConnectorState.PRE_CONNECTING -> ConnectorState.PRE_CONNECTING
        state <= FcConnectorState.PRE_CONNECTED -> {
            ConnectorState.CONNECTING
        }
        else -> {
            ConnectorState.CONNECTED
        }
    }
}

private fun combineState(device: ConnectorDevice?, isAdapterEnabled: Boolean, connectorState: ConnectorState): ConnectorState {
    return if (device == null) {
        ConnectorState.NO_DEVICE
    } else if (!isAdapterEnabled) {
        ConnectorState.BT_DISABLED
    } else {
        connectorState
    }
}

