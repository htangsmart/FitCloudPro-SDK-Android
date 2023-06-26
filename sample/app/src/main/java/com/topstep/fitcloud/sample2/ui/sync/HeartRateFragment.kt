package com.topstep.fitcloud.sample2.ui.sync

import android.content.Context
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.entity.HeartRateItemEntity
import kotlinx.coroutines.runBlocking
import java.util.*

class HeartRateFragment : DataListFragment<HeartRateItemEntity>() {

    override val valueFormat: DataListAdapter.ValueFormat<HeartRateItemEntity> = object : DataListAdapter.ValueFormat<HeartRateItemEntity> {
        override fun format(context: Context, obj: HeartRateItemEntity): String {
            return timeFormat.format(obj.time) + "    " +
                    context.getString(R.string.unit_bmp_unit, obj.heartRate)
        }
    }

    override fun queryData(date: Date): List<HeartRateItemEntity>? {
        return runBlocking { syncDataRepository.queryHeartRate(authedUserId, date) }
    }

}

