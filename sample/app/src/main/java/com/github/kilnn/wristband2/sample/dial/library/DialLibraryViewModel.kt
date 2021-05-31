package com.github.kilnn.wristband2.sample.dial.library

import androidx.lifecycle.viewModelScope
import com.github.kilnn.wristband2.sample.dial.DialParamViewModule
import com.github.kilnn.wristband2.sample.dial.MediatorStateLiveData
import com.github.kilnn.wristband2.sample.dial.State
import com.github.kilnn.wristband2.sample.dial.entity.DialInfo
import com.github.kilnn.wristband2.sample.dial.task.DialParam
import com.github.kilnn.wristband2.sample.dial.task.TaskGetDialList
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class GroupInfoResult(val param: DialParam, val list: MutableList<DialInfo>) {
    override fun toString(): String {
        return "param:${param} , list size:${list.size}"
    }
}

class DialLibraryViewModel : DialParamViewModule() {

    private val taskGetDialList = TaskGetDialList()
    private var liveLocalDialList: MediatorStateLiveData<GroupInfoResult> = MediatorStateLiveData()
    private var liveRemoteDialList: MediatorStateLiveData<GroupInfoResult> = MediatorStateLiveData()

    private var jobGetLocalList: Job? = null
    private var jobGetRemoteList: Job? = null

    init {
        liveLocalDialList.addSource(liveDialParam()) { state ->
            when (state) {
                null, is State.Loading -> {
                    liveLocalDialList.setLoading()
                }
                is State.Failed -> {
                    liveLocalDialList.setFailed(state.error)
                }
                is State.Success -> {
                    refreshDialListInternal(state.result!!, true)
                }
            }
        }
        liveRemoteDialList.addSource(liveDialParam()) { state ->
            when (state) {
                null, is State.Loading -> {
                    liveRemoteDialList.setLoading()
                }
                is State.Failed -> {
                    liveRemoteDialList.setFailed(state.error)
                }
                is State.Success -> {
                    refreshDialListInternal(state.result!!, false)
                }
            }
        }
    }

    private fun refreshDialListInternal(dialParam: DialParam, isLocal: Boolean) {
        if (isLocal) {
            jobGetLocalList?.cancel()
            jobGetLocalList = viewModelScope.launch {
                liveLocalDialList.setLoading()
                try {
                    liveLocalDialList.setSuccess(GroupInfoResult(dialParam, taskGetDialList.execute(dialParam, isLocal)))
                } catch (e: Exception) {
                    liveLocalDialList.setFailed(e)
                }
            }
        } else {
            jobGetRemoteList?.cancel()
            jobGetRemoteList = viewModelScope.launch {
                liveRemoteDialList.setLoading()
                try {
                    liveRemoteDialList.setSuccess(GroupInfoResult(dialParam, taskGetDialList.execute(dialParam, isLocal)))
                } catch (e: Exception) {
                    liveRemoteDialList.setFailed(e)
                }
            }
        }
    }

    fun refreshDialList(isLocal: Boolean) {
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
                refreshDialListInternal(state.result!!, isLocal)
            }
        }
    }

    fun liveDialList(isLocal: Boolean): MediatorStateLiveData<GroupInfoResult> {
        return if (isLocal) {
            liveLocalDialList
        } else {
            liveRemoteDialList
        }
    }
}