package com.topstep.fitcloud.sample2.ui.device.alarm

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sdk.v2.model.settings.FcRepeatFlag

class AlarmRepeatDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = arrayOf(
            getString(R.string.ds_alarm_repeat_00),
            getString(R.string.ds_alarm_repeat_01),
            getString(R.string.ds_alarm_repeat_02),
            getString(R.string.ds_alarm_repeat_03),
            getString(R.string.ds_alarm_repeat_04),
            getString(R.string.ds_alarm_repeat_05),
            getString(R.string.ds_alarm_repeat_06),
        )
        var repeat = (parentFragment as? Listener)?.dialogGetAlarmRepeat() ?: 0
        val checkedItems = BooleanArray(items.size) { index ->
            FcRepeatFlag.isRepeatEnabledIndex(repeat, index)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.ds_alarm_repeat)
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                repeat = FcRepeatFlag.setRepeatEnabledIndex(repeat, which, isChecked)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                (parentFragment as? Listener)?.dialogSetAlarmRepeat(repeat)
            }
            .create()
    }

    interface Listener {
        fun dialogGetAlarmRepeat(): Int
        fun dialogSetAlarmRepeat(repeat: Int)
    }

}