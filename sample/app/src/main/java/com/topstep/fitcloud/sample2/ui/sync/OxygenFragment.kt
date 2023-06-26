package com.topstep.fitcloud.sample2.ui.sync

import android.content.Context
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.entity.OxygenItemEntity
import kotlinx.coroutines.runBlocking
import java.util.*

class OxygenFragment : DataListFragment<OxygenItemEntity>() {

    override val valueFormat: DataListAdapter.ValueFormat<OxygenItemEntity> = object : DataListAdapter.ValueFormat<OxygenItemEntity> {
        override fun format(context: Context, obj: OxygenItemEntity): String {
            return timeFormat.format(obj.time) + "    " +
                    context.getString(R.string.percent_param, obj.oxygen)
        }
    }

    override fun queryData(date: Date): List<OxygenItemEntity>? {
        return runBlocking { syncDataRepository.queryOxygen(authedUserId, date) }
    }
}