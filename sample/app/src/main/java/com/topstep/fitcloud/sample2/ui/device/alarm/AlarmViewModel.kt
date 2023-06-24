package com.topstep.fitcloud.sample2.ui.device.alarm

import androidx.lifecycle.viewModelScope
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.utils.runCatchingWithLog
import com.topstep.fitcloud.sdk.v2.model.settings.FcAlarm
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await

data class AlarmState(
    val requestAlarms: Async<ArrayList<FcAlarm>> = Uninitialized,
)

sealed class AlarmEvent {
    class RequestFail(val throwable: Throwable) : AlarmEvent()

    class AlarmInserted(val position: Int) : AlarmEvent()
    class AlarmRemoved(val position: Int) : AlarmEvent()
    class AlarmMoved(val fromPosition: Int, val toPosition: Int) : AlarmEvent()

    object NavigateUp : AlarmEvent()
}

class AlarmViewModel : StateEventViewModel<AlarmState, AlarmEvent>(AlarmState()) {

    private val deviceManager = Injector.getDeviceManager()
    val helper = AlarmHelper()

    init {
        requestAlarms()
    }

    fun requestAlarms() {
        viewModelScope.launch {
            state.copy(requestAlarms = Loading()).newState()
            runCatchingWithLog {
                deviceManager.settingsFeature.requestAlarms().await()
            }.onSuccess {
                state.copy(requestAlarms = Success(ArrayList(helper.sort(it)))).newState()
            }.onFailure {
                state.copy(requestAlarms = Fail(it)).newState()
                AlarmEvent.RequestFail(it).newEvent()
            }
        }
    }

    private fun findAlarmAddPosition(alarm: FcAlarm, list: List<FcAlarm>): Int {
        var addPosition: Int? = null
        for (i in list.indices) {
            if (helper.comparator.compare(alarm, list[i]) < 0) {
                addPosition = i
                break
            }
        }
        if (addPosition == null) {
            addPosition = list.size
        }
        return addPosition
    }

    fun addAlarm(alarm: FcAlarm) {
        viewModelScope.launch {
            val alarms = state.requestAlarms()
            if (alarms != null) {
                val addPosition = findAlarmAddPosition(alarm, alarms)
                alarms.add(addPosition, alarm)
                AlarmEvent.AlarmInserted(addPosition).newEvent()
                setAlarmsAction.execute()
            }
        }
    }

    /**
     * @param position Delete position
     */
    fun deleteAlarm(position: Int) {
        viewModelScope.launch {
            val alarms = state.requestAlarms()
            if (alarms != null && position < alarms.size) {
                alarms.removeAt(position)
                AlarmEvent.AlarmRemoved(position).newEvent()
                setAlarmsAction.execute()
            }
        }
    }

    /**
     * @param position Modify position
     * @param alarmModified Modified data
     */
    fun modifyAlarm(position: Int, alarmModified: FcAlarm) {
        viewModelScope.launch {
            val alarms = state.requestAlarms()
            if (alarms != null && position < alarms.size) {
                if (alarms.contains(alarmModified)) {
                    throw IllegalStateException()//不能直接改list里的数据
                }
                alarms.removeAt(position)
                val addPosition = findAlarmAddPosition(alarmModified, alarms)
                alarms.add(addPosition, alarmModified)
                AlarmEvent.AlarmMoved(position, addPosition).newEvent()
                setAlarmsAction.execute()
            }
        }
    }

    fun sendNavigateUpEvent() {
        viewModelScope.launch {
            delay(1000)
            AlarmEvent.NavigateUp.newEvent()
        }
    }

    val setAlarmsAction = object : SingleAsyncAction<Unit>(
        viewModelScope,
        Uninitialized
    ) {
        override suspend fun action() {
            deviceManager.settingsFeature.setAlarms(state.requestAlarms()).await()
        }
    }
}