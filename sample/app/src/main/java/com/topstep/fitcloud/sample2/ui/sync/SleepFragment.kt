package com.topstep.fitcloud.sample2.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.entity.SleepItemEntity
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sdk.v2.model.data.FcSleepItem
import kotlinx.coroutines.runBlocking
import java.util.Date

class SleepFragment : DataListFragment<SleepItemEntity>() {

    override val layoutId: Int = R.layout.fragment_sleep

    private lateinit var tvDeepSleep: TextView
    private lateinit var tvLightSleep: TextView
    private lateinit var tvAwakeSleep: TextView
    private lateinit var layoutNapSleep: View
    private lateinit var tvNapSleep: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvDeepSleep = view.findViewById(R.id.tv_deep_sleep)
        tvLightSleep = view.findViewById(R.id.tv_light_sleep)
        tvAwakeSleep = view.findViewById(R.id.tv_awake_sleep)
        layoutNapSleep = view.findViewById(R.id.layout_nap_sleep)
        tvNapSleep = view.findViewById(R.id.tv_nap_sleep)
        super.onViewCreated(view, savedInstanceState)
    }

    override val valueFormat: DataListAdapter.ValueFormat<SleepItemEntity> = object : DataListAdapter.ValueFormat<SleepItemEntity> {
        override fun format(context: Context, obj: SleepItemEntity): String {
            val statusText = when (obj.status) {
                FcSleepItem.STATUS_DEEP -> context.getString(R.string.deep_sleep)
                FcSleepItem.STATUS_LIGHT -> context.getString(R.string.light_sleep)
                else -> context.getString(R.string.awake_sleep)
            }
            return statusText + "    " + timeFormat.format(obj.startTime) + " ->  " + timeFormat.format(obj.endTime)
        }
    }

    override fun queryData(date: Date): List<SleepItemEntity>? {
        val record = runBlocking { syncDataRepository.querySleepRecord(authedUserId, date) }
        tvDeepSleep.text = FormatterUtil.second2Hmm(record?.deepSleep ?: 0)
        tvLightSleep.text = FormatterUtil.second2Hmm(record?.lightSleep ?: 0)
        tvAwakeSleep.text = FormatterUtil.second2Hmm(record?.soberSleep ?: 0)
        if (record?.isSupportSleepNap == true) {
            tvNapSleep.visibility = View.VISIBLE
            tvNapSleep.text = FormatterUtil.second2Hmm(record.napSleep)
        } else {
            tvNapSleep.visibility = View.GONE
        }
        return runBlocking { syncDataRepository.querySleepItems(authedUserId, date) }
    }

}