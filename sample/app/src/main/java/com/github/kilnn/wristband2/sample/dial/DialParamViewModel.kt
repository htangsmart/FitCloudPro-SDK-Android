package com.github.kilnn.wristband2.sample.dial

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import androidx.lifecycle.viewModelScope
import com.github.kilnn.wristband2.sample.dial.task.DialParam
import com.github.kilnn.wristband2.sample.dial.task.TaskGetDialParam
import com.htsmart.wristband2.WristbandApplication
import com.htsmart.wristband2.bean.ConnectionState
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import io.reactivex.BackpressureStrategy
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class DialParamViewModule : ViewModel() {

    private val manager = WristbandApplication.getWristbandManager()
    private val liveWristbandState = manager.observerConnectionState().startWith(
        if (manager.isConnected) ConnectionState.CONNECTED else ConnectionState.CONNECTING
    ).toFlowable(BackpressureStrategy.BUFFER).toLiveData()

    private val taskGetDialParam = TaskGetDialParam()

    private val liveDialParam = MediatorStateLiveData<DialParam>()
    private var jobGetDialParam: Job? = null

    init {
        liveDialParam.addSource(Transformations.distinctUntilChanged(liveWristbandState)) {
            refreshDialParamInternal(it)
        }
    }

    private fun refreshDialParamInternal(connectState: ConnectionState?) {
        if (connectState == ConnectionState.CONNECTED) {
            jobGetDialParam?.cancel()
            jobGetDialParam = viewModelScope.launch {
                liveDialParam.setLoading()
                try {
                    //请求DialParam
                    liveDialParam.setSuccess(taskGetDialParam.execute())
                } catch (e: Exception) {
                    liveDialParam.setFailed(e)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            liveDialParam.setFailed(BleDisconnectedException())
        }
    }

    fun refreshDialParam() {
        refreshDialParamInternal(liveWristbandState.value)
    }

    fun liveDialParam(): MediatorStateLiveData<DialParam> {
        return liveDialParam
    }
}
