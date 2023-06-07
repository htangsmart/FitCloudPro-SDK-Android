package com.topstep.fitcloud.sample2.ui.device.bind

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.polidea.rxandroidble3.exceptions.BleScanException
import com.topstep.fitcloud.sample2.fcSDK
import com.topstep.fitcloud.sample2.utils.PermissionHelper
import com.topstep.fitcloud.sample2.utils.doOnFinish
import com.topstep.fitcloud.sample2.utils.flowBluetoothAdapterState
import com.topstep.fitcloud.sdk.scanner.FcScanResult
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.TimeUnit

class ScannerHelper(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
) : DefaultLifecycleObserver {

    private val fcSDK = context.fcSDK

    private var stateJob: Job? = null
    private var scanDisposable: Disposable? = null

    /**
     * Whether an auto scan has been performed.
     * Auto scan only performed once.
     */
    private var isAutoScanned = false

    /**
     * The number of consecutive occurrences of an unknown error
     */
    private var errorUnknownCount = 0

    var listener: Listener? = null

    private val flowPermissionsState = flow {
        val hasPermissions = PermissionHelper.hasBle(context)
        emit(hasPermissions)
        if (!hasPermissions) {
            while (currentCoroutineContext().isActive && !PermissionHelper.hasBle(context)) {
                delay(1000)
            }
            //Delay for a while. Sometimes there will be errors in scanning immediately
            delay(500)
            emit(true)
        }
    }.flowOn(Dispatchers.Default)

    private val flowState: Flow<Int> = flowBluetoothAdapterState(context)
        .combine(flowPermissionsState) { isAdapterEnabled, hasPermissions ->
            if (!hasPermissions) {
                STATE_NO_PERMISSION
            } else if (!isAdapterEnabled) {
                STATE_BT_DISABLED
            } else {
                STATE_READY
            }
        }

    private fun getState(): Int {
        val hasPermissions = PermissionHelper.hasBle(context)
        return if (!hasPermissions) {
            STATE_NO_PERMISSION
        } else if (!bluetoothManager.adapter.isEnabled) {
            STATE_BT_DISABLED
        } else {
            STATE_READY
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        stateJob = owner.lifecycle.coroutineScope.launch {
            flowState.collect {
                checkState(it, true)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        //Cancel scan when onStop
        scanDisposable?.dispose()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        stateJob?.cancel()
    }

    /**
     * @return Whether scanning is started
     */
    private fun checkState(state: Int, performedByAuto: Boolean): Boolean {
        if (state == STATE_NO_PERMISSION) {
            listener?.requestPermission()
        } else {
            listener?.bluetoothAlert(state == STATE_BT_DISABLED)
            if (state == STATE_READY) {
                if (!performedByAuto || !isAutoScanned) {
                    isAutoScanned = true
                    scan()
                    return true
                }
            }
        }
        return false
    }

    private fun scan() {
        if (scanDisposable?.isDisposed != false) {
            //It is recommended not to set the scan duration too short
            scanDisposable = fcSDK.scanner.scan(120, TimeUnit.SECONDS, checkLocationServiceFirst = false, acceptEmptyDeviceName = true)
                .doOnSubscribe {
                    listener?.onScanStart()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnFinish {
                    listener?.onScanStop()
                }
                .subscribe({
                    listener?.onScanResult(it)
                }, {
                    //Analysis error
                    analysisScanError(it)
                }, {
                    errorUnknownCount = 0
                })
        }
    }

    private fun analysisScanError(throwable: Throwable) {
        val reason = if (throwable is BleScanException) {
            throwable.reason
        } else {
            BleScanException.UNKNOWN_ERROR_CODE
        }
        when (reason) {
            BleScanException.BLUETOOTH_DISABLED,
            BleScanException.BLUETOOTH_NOT_AVAILABLE,
            BleScanException.LOCATION_PERMISSION_MISSING,
            BleScanException.LOCATION_SERVICES_DISABLED -> {
                //Ignore these error states because it is handled elsewhere, or has been checked before the scan starts
            }
            BleScanException.SCAN_FAILED_ALREADY_STARTED,
            BleScanException.UNDOCUMENTED_SCAN_THROTTLE -> {
                //Prompt the user to re-search in a few seconds
                listener?.scanErrorDelayAlert()
            }
            else -> {
                errorUnknownCount++
                if (errorUnknownCount <= 3) {
                    listener?.scanErrorDelayAlert()
                } else {
                    //Prompt the user to restart Bluetooth or Mobile-Phone
                    listener?.scanErrorRestartAlert()
                }
            }
        }
    }

    /**
     * @return Whether scanning is started
     */
    fun start(): Boolean {
        return checkState(getState(), false)
    }

    fun stop() {
        scanDisposable?.dispose()
    }

    fun toggle() {
        if (scanDisposable?.isDisposed != false) {
            start()
        } else {
            stop()
        }
    }

    interface Listener {
        fun requestPermission()
        fun bluetoothAlert(show: Boolean)

        fun scanErrorDelayAlert()
        fun scanErrorRestartAlert()

        fun onScanStart()
        fun onScanStop()
        fun onScanResult(result: FcScanResult)
    }

    companion object {
        private const val TAG = "ScannerHelper"
        private const val STATE_NO_PERMISSION = 0
        private const val STATE_BT_DISABLED = 1
        private const val STATE_READY = 2
    }
}