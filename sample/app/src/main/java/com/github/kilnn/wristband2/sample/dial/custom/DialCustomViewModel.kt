package com.github.kilnn.wristband2.sample.dial.custom

import androidx.lifecycle.viewModelScope
import com.github.kilnn.wristband2.sample.dial.DialParamViewModule
import com.github.kilnn.wristband2.sample.dial.MediatorStateLiveData
import com.github.kilnn.wristband2.sample.dial.State
import com.github.kilnn.wristband2.sample.dial.task.DialParam
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class GroupCustomResult(val param: DialParam, val custom: DialCustomCompat) {
    override fun toString(): String {
        return "param:${param} , custom:${custom}"
    }
}

class DialCustomViewModel : DialParamViewModule() {

    private val taskGetDialCustom = TaskGetDialCustomCompat()

    private var liveDialCustom: MediatorStateLiveData<GroupCustomResult> = MediatorStateLiveData()

    private var jobGetDialCustom: Job? = null

    init {
        liveDialCustom.addSource(liveDialParam()) { state ->
            when (state) {
                null, is State.Loading -> {
                    liveDialCustom.setLoading()
                }
                is State.Failed -> {
                    liveDialCustom.setFailed(state.error)
                }
                is State.Success -> {
                    refreshDialCustomInternal(state.result!!)
                }
            }
        }
    }

    private fun refreshDialCustomInternal(dialParam: DialParam) {
        jobGetDialCustom?.cancel()
        jobGetDialCustom = viewModelScope.launch {
            liveDialCustom.setLoading()
            try {
                liveDialCustom.setSuccess(GroupCustomResult(dialParam, taskGetDialCustom.execute(dialParam)))
            } catch (e: Exception) {
                liveDialCustom.setFailed(e)
            }
        }
    }

    fun refreshDialCustom() {
        when (val state = liveDialParam().value) {
            null, is State.Loading -> {
                //为null和Loading，一定是正在请求，所以不用再次处理请求了
            }
            is State.Failed -> {
                if (state.error is BleDisconnectedException) {
                    //设备断开连接，不执行刷新操作
                } else {
                    //上次请求失败，再次请求DialParam
                    refreshDialParam()
                }
            }
            is State.Success -> {
                refreshDialCustomInternal(state.result!!)
            }
        }
    }

    fun liveDialCustom(): MediatorStateLiveData<GroupCustomResult> {
        return liveDialCustom
    }

}