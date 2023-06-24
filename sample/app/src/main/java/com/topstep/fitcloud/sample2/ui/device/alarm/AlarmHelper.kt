package com.topstep.fitcloud.sample2.ui.device.alarm

import android.content.Context
import android.text.format.DateFormat
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sdk.v2.model.settings.FcAlarm
import com.topstep.fitcloud.sdk.v2.model.settings.FcRepeatFlag
import java.util.*

class AlarmHelper {

    private var is24HourFormat: Boolean? = null

    fun is24HourFormat(context: Context): Boolean {
        return is24HourFormat ?: DateFormat.is24HourFormat(context).also { is24HourFormat = it }
    }

    private var dayValuesSimple: Array<Int> = arrayOf(
        R.string.ds_alarm_repeat_00_simple,
        R.string.ds_alarm_repeat_01_simple,
        R.string.ds_alarm_repeat_02_simple,
        R.string.ds_alarm_repeat_03_simple,
        R.string.ds_alarm_repeat_04_simple,
        R.string.ds_alarm_repeat_05_simple,
        R.string.ds_alarm_repeat_06_simple
    )

    /**
     * Display [FcAlarm.repeat] as a readable String
     */
    fun repeatToSimpleStr(context: Context, repeat: Int): String {
        var text: String? = null
        var sumDays = 0
        var resultString = ""
        for (i in 0..6) {
            if (FcRepeatFlag.isRepeatEnabledIndex(repeat, i)) {
                sumDays++
                resultString += context.getString(dayValuesSimple[i])
            }
        }
        if (sumDays == 7) {
            text = context.getString(R.string.ds_alarm_repeat_every_day)
        } else if (sumDays == 0) {
            text = context.getString(R.string.ds_alarm_repeat_never)
        } else if (sumDays == 5) {
            val sat: Boolean = !FcRepeatFlag.isRepeatEnabledIndex(repeat, 5)
            val sun: Boolean = !FcRepeatFlag.isRepeatEnabledIndex(repeat, 6)
            if (sat && sun) {
                text = context.getString(R.string.ds_alarm_repeat_workdays)
            }
        } else if (sumDays == 2) {
            val sat: Boolean = FcRepeatFlag.isRepeatEnabledIndex(repeat, 5)
            val sun: Boolean = FcRepeatFlag.isRepeatEnabledIndex(repeat, 6)
            if (sat && sun) {
                text = context.getString(R.string.ds_alarm_repeat_weekends)
            }
        }
        if (text == null) {
            text = resultString
        }
        return text
    }

    fun sort(list: List<FcAlarm>): List<FcAlarm> {
        Collections.sort(list, comparator)
        return list
    }

    val comparator: Comparator<FcAlarm> by lazy {
        Comparator { o1, o2 ->
            //first sort by time ,and then by id
            val v1: Int = o1.hour * 60 + o1.minute
            val v2: Int = o2.hour * 60 + o2.minute
            if (v1 > v2) {
                1
            } else if (v1 < v2) {
                -1
            } else {
                o1.id - o2.id
            }
        }
    }

}