package com.topstep.fitcloud.sample2.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.entity.GameRecordEntity
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class GameFragment : DataListFragment<GameRecordEntity>() {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override val valueFormat: DataListAdapter.ValueFormat<GameRecordEntity> = object : DataListAdapter.ValueFormat<GameRecordEntity> {
        override fun format(context: Context, obj: GameRecordEntity): String {
            return dateTimeFormat.format(obj.time) + "    " + gameTypeText(obj.type)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnDate.isVisible = false
    }

    override fun queryData(date: Date): List<GameRecordEntity>? {
        return runBlocking { syncDataRepository.queryGame(authedUserId) }
    }

    //TODO Only part game types are listed here.
    // For more types, please refer to https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/05.Sync-Data#game
    private fun gameTypeText(type: Int): String {
        return when (type) {
            0 -> requireContext().getString(R.string.game_type_0)
            1 -> requireContext().getString(R.string.game_type_1)
            2 -> requireContext().getString(R.string.game_type_2)
            3 -> requireContext().getString(R.string.game_type_3)
            4 -> requireContext().getString(R.string.game_type_4)
            else -> requireContext().getString(R.string.game_type_unknown)
        }
    }

}