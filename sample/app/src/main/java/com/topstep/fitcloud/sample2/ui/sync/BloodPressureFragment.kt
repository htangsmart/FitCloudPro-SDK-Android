package com.topstep.fitcloud.sample2.ui.sync

import android.content.Context
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.entity.BloodPressureItemEntity
import kotlinx.coroutines.runBlocking
import java.util.*

class BloodPressureFragment : DataListFragment<BloodPressureItemEntity>() {

    override val valueFormat: DataListAdapter.ValueFormat<BloodPressureItemEntity> = object : DataListAdapter.ValueFormat<BloodPressureItemEntity> {
        override fun format(context: Context, obj: BloodPressureItemEntity): String {
            return timeFormat.format(obj.time) + "    " +
                    context.getString(R.string.unit_mmhg_param, "${obj.dbp}/${obj.sbp}")
        }
    }

    override fun queryData(date: Date): List<BloodPressureItemEntity>? {
        return runBlocking { syncDataRepository.queryBloodPressure(authedUserId, date) }
    }

}