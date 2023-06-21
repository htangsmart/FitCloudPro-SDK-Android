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

const val DIALOG_HR_STATIC = "hr_static"
const val DIALOG_HR_DYNAMIC = "hr_dynamic"
fun Fragment.showHrStaticDialog(value: Int) {
    SelectIntDialogFragment.newInstance(
        min = 10,
        max = 15,
        multiples = 10,
        value = value,
        title = getString(R.string.ds_heart_rate_alarm_static),
        des = getString(R.string.unit_bmp)
    ).show(childFragmentManager, DIALOG_HR_STATIC)
}

fun Fragment.showHrDynamicDialog(value: Int) {
    SelectIntDialogFragment.newInstance(
        min = 100,
        max = 200,
        multiples = 1,
        value = value,
        title = getString(R.string.ds_heart_rate_alarm_dynamic),
        des = getString(R.string.unit_bmp)
    ).show(childFragmentManager, DIALOG_HR_DYNAMIC)
}


const val DIALOG_SBP_UPPER = "sbp_upper"
const val DIALOG_SBP_LOWER = "sbp_lower"
const val DIALOG_DBP_UPPER = "dbp_upper"
const val DIALOG_DBP_LOWER = "dbp_lower"
fun Fragment.showSbpUpperDialog(value: Int) {
    SelectIntDialogFragment.newInstance(
        min = 90,
        max = 180,
        multiples = 1,
        value = value,
        title = getString(R.string.ds_blood_pressure_alarm_sbp_upper),
        des = getString(R.string.unit_mmhg)
    ).show(childFragmentManager, DIALOG_SBP_UPPER)
}

fun Fragment.showSbpLowerDialog(value: Int) {
    SelectIntDialogFragment.newInstance(
        min = 60,
        max = 120,
        multiples = 1,
        value = value,
        title = getString(R.string.ds_blood_pressure_alarm_sbp_lower),
        des = getString(R.string.unit_mmhg)
    ).show(childFragmentManager, DIALOG_SBP_LOWER)
}

fun Fragment.showDbpUpperDialog(value: Int) {
    SelectIntDialogFragment.newInstance(
        min = 60,
        max = 120,
        multiples = 1,
        value = value,
        title = getString(R.string.ds_blood_pressure_alarm_dbp_upper),
        des = getString(R.string.unit_mmhg)
    ).show(childFragmentManager, DIALOG_DBP_UPPER)
}

fun Fragment.showDbpLowerDialog(value: Int) {
    SelectIntDialogFragment.newInstance(
        min = 40,
        max = 100,
        multiples = 1,
        value = value,
        title = getString(R.string.ds_blood_pressure_alarm_dbp_lower),
        des = getString(R.string.unit_mmhg)
    ).show(childFragmentManager, DIALOG_DBP_LOWER)
}