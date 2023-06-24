package com.topstep.fitcloud.sample2.data.device

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.polidea.rxandroidble3.exceptions.BleDisconnectedException
import com.topstep.fitcloud.sample2.data.config.ExerciseGoalRepository
import com.topstep.fitcloud.sample2.data.db.AppDatabase
import com.topstep.fitcloud.sample2.data.db.UserForConnect
import com.topstep.fitcloud.sample2.data.entity.DeviceBindEntity
import com.topstep.fitcloud.sample2.data.entity.toModel
import com.topstep.fitcloud.sample2.data.storage.InternalStorage
import com.topstep.fitcloud.sample2.data.wh.WomenHealthRepository
import com.topstep.fitcloud.sample2.fcSDK
import com.topstep.fitcloud.sample2.model.device.ConnectorDevice
import com.topstep.fitcloud.sample2.model.device.ConnectorState
import com.topstep.fitcloud.sample2.model.wh.WomenHealthConfig
import com.topstep.fitcloud.sample2.utils.launchWithLog
import com.topstep.fitcloud.sample2.utils.runCatchingWithLog
import com.topstep.fitcloud.sdk.connector.FcConnectorState
import com.topstep.fitcloud.sdk.connector.FcDisconnectedReason
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import com.topstep.fitcloud.sdk.v2.features.FcConfigFeature
import com.topstep.fitcloud.sdk.v2.features.FcSettingsFeature
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.FcFunctionConfig
import com.topstep.fitcloud.sdk.v2.model.config.FcWomenHealthConfig
import com.topstep.fitcloud.sdk.v2.model.settings.FcBatteryStatus
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import java.util.concurrent.TimeUnit

interface DeviceManager {

    val flowDevice: StateFlow<ConnectorDevice?>

    val flowState: StateFlow<ConnectorState>

    val flowBattery: StateFlow<FcBatteryStatus?>

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

    fun disconnect()

    fun reconnect()

    fun newDfuManager(): FcDfuManager

    fun syncData()
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
    private val womenHealthRepository: WomenHealthRepository,
    private val exerciseGoalRepository: ExerciseGoalRepository,
    appDatabase: AppDatabase,
) : DeviceManager {

    private val fcSDK = context.fcSDK
    private val connector = fcSDK.connector
    private val configDao = appDatabase.configDao()
    private val userDao = appDatabase.userDao()

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

    init {
        applicationScope.launch {
            //Connect or disconnect when device changed
            internalStorage.flowAuthedUserId.flatMapLatest {
                if (it == null) {
                    flowOf(null)
                } else {
                    userDao.flowUserForConnect(it)
                }
            }.combine(flowDevice) { user, device ->
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
        applicationScope.launch {
            connector.dataFeature().syncData().asFlow()
                .onStart {
                    Timber.tag(TAG).i("syncData onStart")
                }
                .onCompletion {
                    Timber.tag(TAG).i(it, "syncData onCompletion")
                }
                .catch {
                }
                .collect {

                }
            Timber.tag(TAG).i("syncData finish")
        }
    }

    companion object {
        private const val TAG = "DeviceManager"
    }

    private data class ConnectionParam(
        val user: UserForConnect?,
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

