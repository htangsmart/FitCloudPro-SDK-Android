package com.topstep.fitcloud.sample2.ui.device

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kilnn.tool.dialog.prompt.PromptDialogHolder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.di.internal.CoroutinesInstance.applicationScope
import com.topstep.fitcloud.sample2.model.device.ConnectorState
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sdk.exception.FcDfuException
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class DfuViewModel : ViewModel() {

    sealed class DfuEvent {
        object OnSuccess : DfuEvent()
        class OnFail(val error: Throwable) : DfuEvent()
    }

    private val deviceManager = Injector.getDeviceManager()
    private val dfuManager by lazy { deviceManager.newDfuManager() }

    private val _flowDfuEvent = Channel<DfuEvent>()
    val flowDfuEvent = _flowDfuEvent.receiveAsFlow()

    fun flowDfuStateProgress(): Flow<FcDfuManager.StateProgress> {
        return dfuManager.observerStateProgress().asFlow()
    }

    private var dfuJob: Job? = null

    fun startDfu(dfuType: FcDfuManager.DfuType, uri: Uri, binFlag: Byte) {
        dfuJob?.cancel()
        dfuJob = viewModelScope.launch {
            try {
                dfuManager.start(dfuType, uri, binFlag).await()
                _flowDfuEvent.send(DfuEvent.OnSuccess)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _flowDfuEvent.send(DfuEvent.OnFail(e))
                }
            }
        }
    }

    fun setGUICustomDialComponent(spaceIndex: Int, styleIndex: Int) {
        applicationScope.launch {
            try {
                withTimeout(90 * 1000) {
                    deviceManager.flowState.filter { it == ConnectorState.CONNECTED }.first()
                }
                deviceManager.settingsFeature.setDialComponent(spaceIndex, byteArrayOf(styleIndex.toByte())).await()
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
    }

    fun isDfuIng(): Boolean {
        return dfuJob?.isActive == true
    }

    override fun onCleared() {
        super.onCleared()
        dfuManager.release()
    }

}

/**
 * General prompt message for Dfu
 */
fun PromptDialogHolder.showDfuFail(context: Context, throwable: Throwable) {
    Timber.w(throwable)
    var toastId = 0
    if (throwable is FcDfuException) {
        val errorType = throwable.errorType
        val errorCode = throwable.errorCode
        when (errorType) {
            FcDfuException.ERROR_TYPE_ENVIRONMENT -> {
                when (errorCode) {
                    FcDfuException.ERROR_CODE_BT_DISABLE -> toastId = R.string.device_state_bt_disabled
                    FcDfuException.ERROR_CODE_INSUFFICIENT_STORAGE -> toastId = R.string.ds_dfu_error_insufficient_storage
                }
            }
            FcDfuException.ERROR_TYPE_DFU_FILE -> {
                if (errorCode == FcDfuException.ERROR_CODE_DFU_FILE_DOWNLOAD) {
                    toastId = R.string.tip_download_failed
                }
            }
            FcDfuException.ERROR_TYPE_DFU_MODE -> {
                if (errorCode == FcDfuException.ERROR_CODE_DFU_MODE_LOW_BATTERY) {
                    toastId = R.string.ds_dfu_error_low_battery
                } else if (errorCode == FcDfuException.ERROR_CODE_DFU_MODE_ABORT) {
                    toastId = R.string.device_state_disconnected
                }
            }
            FcDfuException.ERROR_TYPE_DFU_DEVICE -> {
                if (errorCode == FcDfuException.ERROR_CODE_DFU_DEVICE_NOT_FOUND) {
                    toastId = R.string.ds_dfu_error_device_not_found
                }
            }
        }
        if (toastId != 0) {
            showFailed(context.getString(R.string.ds_push_fail, context.getString(toastId)))
        } else {
            showFailed(context.getString(R.string.ds_push_fail, "errorType:$errorType errorCode$errorCode"))
        }
    } else {
        showFailed(throwable)
    }
}