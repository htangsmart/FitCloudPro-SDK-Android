package com.topstep.fitcloud.sample2.ui.device.dial

import androidx.lifecycle.viewModelScope
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.dial.DialPushParams
import com.topstep.fitcloud.sample2.ui.base.AsyncViewModel
import com.topstep.fitcloud.sample2.ui.base.SingleAsyncState
import kotlinx.coroutines.launch

/**
 * ViewModel only use for request [DialPushParams]
 */
class DialPushViewModel : AsyncViewModel<SingleAsyncState<DialPushParams>>(SingleAsyncState()) {

    val deviceManager = Injector.getDeviceManager()
    private val dialRepository = Injector.getDialRepository()

    init {
        viewModelScope.launch {
            deviceManager.flowStateConnected().collect {
                //Refresh every time device connected, because the DialPushParams may change
                if (it) {
                    refresh()
                }
            }
        }
    }

    /**
     * Refresh [DialPushParams]
     */
    fun refresh() {
        suspend {
            dialRepository.getDialPushParams()
        }.execute(SingleAsyncState<DialPushParams>::async) {
            copy(async = it)
        }
    }

}
