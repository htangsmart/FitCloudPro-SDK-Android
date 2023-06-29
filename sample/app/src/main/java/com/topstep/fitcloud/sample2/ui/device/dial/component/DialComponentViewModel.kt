package com.topstep.fitcloud.sample2.ui.device.dial.component

import androidx.lifecycle.viewModelScope
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.dial.DialComponent
import com.topstep.fitcloud.sample2.model.dial.DialPushParams
import com.topstep.fitcloud.sample2.ui.base.Async
import com.topstep.fitcloud.sample2.ui.base.AsyncViewModel
import com.topstep.fitcloud.sample2.ui.base.Success
import com.topstep.fitcloud.sample2.ui.base.Uninitialized
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await

class DialComponentViewModel : AsyncViewModel<DialComponentViewModel.State>(State()) {

    data class State(
        val getParams: Async<DialPushParams> = Uninitialized,
        val setComponents: Async<Unit> = Uninitialized,
    )

    private val deviceManager = Injector.getDeviceManager()
    private val dialRepository = Injector.getDialRepository()

    init {
        viewModelScope.launch {
            deviceManager.flowStateConnected().collect {
                //每次连接的时候刷新一次
                if (it) {
                    getParams()
                }
            }
        }
    }

    fun getParams() {
        suspend {
            dialRepository.getDialPushParams()
        }.execute(State::getParams) {
            copy(getParams = it)
        }
    }

    /**
     * Set to initialization state，Avoid directly exiting by monitoring the previous state in the [DialComponentEditFragment]
     */
    suspend fun clearSetComponents() {
        state.copy(setComponents = Uninitialized).newState()
    }

    fun setComponents(position: Int, components: List<DialComponent>?) {
        suspend {
            val selects = if (components == null) {
                null
            } else {
                val array = ByteArray(components.size)
                for (i in components.indices) {
                    array[i] = components[i].styleCurrent.toByte()
                }
                array
            }
            deviceManager.settingsFeature.setDialComponent(position, selects).await()
        }.execute(State::setComponents) {
            var getParams = this.getParams
            if (it is Success && getParams is Success) {
                if (components != null) {
                    val spacePackets = getParams().dialSpacePackets?.toMutableList()
                    spacePackets?.removeAt(position)?.let { removed ->
                        spacePackets.add(position, removed.copy(components = components))
                    }
                    getParams = Success(
                        getParams().copy(
                            currentPosition = position,
                            dialSpacePackets = spacePackets
                        )
                    )
                } else {
                    getParams = Success(
                        getParams().copy(
                            currentPosition = position,
                        )
                    )
                }
            }
            copy(setComponents = it, getParams = getParams)
        }
    }

}
