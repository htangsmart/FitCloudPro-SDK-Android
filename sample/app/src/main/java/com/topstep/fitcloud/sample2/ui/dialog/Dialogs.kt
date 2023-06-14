package com.topstep.fitcloud.sample2.ui.dialog

import androidx.fragment.app.Fragment
import com.topstep.fitcloud.sample2.R

const val DIALOG_START_TIME = "start_time"
fun Fragment.showStartTimeDialog(timeMinute: Int) {
    TimePickerDialogFragment.newInstance(timeMinute, getString(R.string.ds_config_start_time))
        .show(childFragmentManager, DIALOG_START_TIME)
}

const val DIALOG_END_TIME = "end_time"
fun Fragment.showEndTimeDialog(timeMinute: Int) {
    TimePickerDialogFragment.newInstance(timeMinute, getString(R.string.ds_config_end_time))
        .show(childFragmentManager, DIALOG_END_TIME)
}

const val DIALOG_INTERVAL_TIME = "interval_time"
fun Fragment.showIntervalDialog(value: Int, from: Int, to: Int) {
    SelectIntDialogFragment.newInstance(
        min = 1,
        max = to / from,
        multiples = from,
        value = value,
        title = requireContext().getString(R.string.ds_config_interval_time),
        des = requireContext().getString(R.string.unit_minute)
    ).show(childFragmentManager, DIALOG_INTERVAL_TIME)
}

const val SBP_MIN = 50
const val SBP_DEFAULT = 125
const val SBP_MAX = 200
const val DBP_MIN = 20
const val DBP_DEFAULT = 80
const val DBP_MAX = 120
const val DIALOG_DBP = "dbp"
const val DIALOG_SBP = "sbp"
fun Fragment.showDbpDialog(value: Int) {
    SelectIntDialogFragment.newInstance(
        min = DBP_MIN,
        max = DBP_MAX,
        multiples = 1,
        value = value,
        title = getString(R.string.ds_dbp),
        des = getString(R.string.unit_mmhg)
    ).show(childFragmentManager, DIALOG_DBP)
}

fun Fragment.showSbpDialog(value: Int) {
    SelectIntDialogFragment.newInstance(
        min = SBP_MIN,
        max = SBP_MAX,
        multiples = 1,
        value = value,
        title = getString(R.string.ds_sbp),
        des = getString(R.string.unit_mmhg)
    ).show(childFragmentManager, DIALOG_SBP)
}
