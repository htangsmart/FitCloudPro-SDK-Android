package com.topstep.fitcloud.sample2.ui.sync

import android.content.Context
import com.topstep.fitcloud.sample2.data.entity.PressureItemEntity
import kotlinx.coroutines.runBlocking
import java.util.*

class PressureFragment : DataListFragment<PressureItemEntity>() {

    override val valueFormat: DataListAdapter.ValueFormat<PressureItemEntity> = object : DataListAdapter.ValueFormat<PressureItemEntity> {
        override fun format(context: Context, obj: PressureItemEntity): String {
            return timeFormat.format(obj.time) + "    ${obj.pressure}"
        }
    }

    override fun queryData(date: Date): List<PressureItemEntity>? {
        return runBlocking { syncDataRepository.queryPressure(authedUserId, date) }
    }
}