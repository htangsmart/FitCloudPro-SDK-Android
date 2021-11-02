package com.github.kilnn.wristband2.sample.sportpush

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import androidx.lifecycle.viewModelScope
import com.github.kilnn.wristband2.sample.dial.MediatorStateLiveData
import com.github.kilnn.wristband2.sample.sportpush.entity.SportPushParam
import com.htsmart.wristband2.WristbandApplication
import com.htsmart.wristband2.bean.ConnectionState
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import io.reactivex.BackpressureStrategy
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SportPushViewModel : ViewModel() {

    private val manager = WristbandApplication.getWristbandManager()
    private val liveWristbandState = manager.observerConnectionState().startWith(
        if (manager.isConnected) ConnectionState.CONNECTED else ConnectionState.CONNECTING
    ).toFlowable(BackpressureStrategy.BUFFER).toLiveData()

    private val taskGetSportPushParam = TaskGetSportPushParam()

    private val liveSportPushParam = MediatorStateLiveData<SportPushParam>()
    private var jobGetSportPushParam: Job? = null

    init {
        liveSportPushParam.addSource(Transformations.distinctUntilChanged(liveWristbandState)) {
            refreshSportPushParamInternal(it)
        }
    }

    private fun refreshSportPushParamInternal(connectState: ConnectionState?) {
        if (connectState == ConnectionState.CONNECTED) {
            jobGetSportPushParam?.cancel()
            jobGetSportPushParam = viewModelScope.launch {
                liveSportPushParam.setLoading()
                try {
                    //请求DialParam
                    liveSportPushParam.setSuccess(taskGetSportPushParam.execute())
                } catch (e: Exception) {
                    liveSportPushParam.setFailed(e)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            liveSportPushParam.setFailed(BleDisconnectedException())
        }
    }

    fun refreshSportPushParam() {
        refreshSportPushParamInternal(liveWristbandState.value)
    }

    fun liveSportPushParam(): MediatorStateLiveData<SportPushParam> {
        return liveSportPushParam
    }

}