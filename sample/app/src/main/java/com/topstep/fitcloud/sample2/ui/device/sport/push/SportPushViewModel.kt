package com.topstep.fitcloud.sample2.ui.device.sport.push

import androidx.lifecycle.viewModelScope
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.sport.push.SportPushParams
import com.topstep.fitcloud.sample2.ui.base.AsyncViewModel
import com.topstep.fitcloud.sample2.ui.base.SingleAsyncState
import kotlinx.coroutines.launch

class SportPushViewModel : AsyncViewModel<SingleAsyncState<SportPushParams>>(SingleAsyncState()) {

    val deviceManager = Injector.getDeviceManager()
    private val sportPushRepository = Injector.getSportPushRepository()

    init {
        viewModelScope.launch {
            deviceManager.flowStateConnected().collect {
                //Refresh every time you connect
                if (it) {
                    refresh()
                }
            }
        }
    }

    fun refresh() {
        suspend {
            sportPushRepository.getSportPushParams()
        }.execute(SingleAsyncState<SportPushParams>::async) {
            copy(async = it)
        }
    }

}
