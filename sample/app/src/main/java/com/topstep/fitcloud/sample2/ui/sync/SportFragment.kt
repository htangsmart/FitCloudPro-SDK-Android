package com.topstep.fitcloud.sample2.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.topstep.fitcloud.sample2.data.entity.SportRecordEntity
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class SportFragment : DataListFragment<SportRecordEntity>() {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override val valueFormat: DataListAdapter.ValueFormat<SportRecordEntity> = object : DataListAdapter.ValueFormat<SportRecordEntity> {
        override fun format(context: Context, obj: SportRecordEntity): String {
            return dateTimeFormat.format(obj.time) + "    " + sportTypeText(obj.sportType)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnDate.isVisible = false
    }

    override fun queryData(date: Date): List<SportRecordEntity>? {
        return runBlocking { syncDataRepository.querySport(authedUserId) }
    }

    //TODO Only part game types are listed here.
    // For more types, please refer to https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/05.Sync-Data#game
    private fun sportTypeText(type: Int): String {
        return ""
    }

}