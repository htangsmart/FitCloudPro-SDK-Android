package com.topstep.fitcloud.sample2.ui.device.game.push

import androidx.lifecycle.viewModelScope
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.game.push.GamePushParams
import com.topstep.fitcloud.sample2.ui.base.AsyncViewModel
import com.topstep.fitcloud.sample2.ui.base.SingleAsyncState
import kotlinx.coroutines.launch

class GamePushViewModel : AsyncViewModel<SingleAsyncState<GamePushParams>>(SingleAsyncState()) {

    val deviceManager = Injector.getDeviceManager()
    private val gameRepository = Injector.getGameRepository()

    init {
        viewModelScope.launch {
            deviceManager.flowStateConnected().collect {
                //每次连接的时候刷新一次
                if (it) {
                    refresh()
                }
            }
        }
    }

    fun refresh() {
        suspend {
            gameRepository.getGamePushParams()
        }.execute(SingleAsyncState<GamePushParams>::async)
        {
            copy(async = it)
        }
    }

}