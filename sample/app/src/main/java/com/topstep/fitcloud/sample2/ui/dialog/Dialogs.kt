package com.topstep.fitcloud.sample2.ui.dialog

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sdk.v2.model.config.FcScreenVibrateConfig

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


private fun Fragment.baseSubsectionDialog(
    subConfig: FcScreenVibrateConfig.ReadOnlyBaseSubsection,
    @StringRes itemTextResId: Int,
    /**
     * 0:use index
     * 1:use index+1
     * 2:use values
     */
    itemTextType: Int,
    @StringRes titleResId: Int,
): ChoiceIntDialogFragment? {
    val values = subConfig.getItems()
    if (values?.isNotEmpty() == true) {
        val items = when (itemTextType) {
            0 -> {
                Array(values.size) { index ->
                    getString(itemTextResId, index)
                }
            }
            1 -> {
                Array(values.size) { index ->
                    getString(itemTextResId, index + 1)
                }
            }
            2 -> {
                Array(values.size) { index ->
                    getString(itemTextResId, values[index])
                }
            }
            else -> throw IllegalArgumentException()
        }
        return ChoiceIntDialogFragment.newInstance(
            items = items,
            selectValue = subConfig.getSelectPosition(),
            title = requireContext().getString(titleResId)
        )
    }
    return null
}

const val DIALOG_VIBRATE = "vibrate"
fun Fragment.showVibrateDialog(config: FcScreenVibrateConfig) {
    baseSubsectionDialog(
        subConfig = config.vibrate(),
        itemTextResId = R.string.unit_level_param,
        itemTextType = 0,
        titleResId = R.string.ds_vibration_intensity
    )?.show(childFragmentManager, DIALOG_VIBRATE)
}

const val DIALOG_SCREEN_BRIGHTNESS = "brightness"
fun Fragment.showScreenBrightnessDialog(config: FcScreenVibrateConfig) {
    baseSubsectionDialog(
        subConfig = config.brightness(),
        itemTextResId = R.string.unit_level_param,
        itemTextType = 1,
        titleResId = R.string.ds_screen_brightness
    )?.show(childFragmentManager, DIALOG_SCREEN_BRIGHTNESS)
}

const val DIALOG_SCREEN_BRIGHT_DURATION = "bt_dur"
fun Fragment.showScreenBrightDurationDialog(config: FcScreenVibrateConfig) {
    baseSubsectionDialog(
        subConfig = config.brightDuration(),
        itemTextResId = R.string.unit_second_param,
        itemTextType = 2,
        titleResId = R.string.ds_screen_bright_duration
    )?.show(childFragmentManager, DIALOG_SCREEN_BRIGHT_DURATION)
}

const val DIALOG_SCREEN_TURN_WRIST_BRIGHT_DURATION = "tw_bt_dur"
fun Fragment.showScreenTurnWristBrightDurationDialog(config: FcScreenVibrateConfig) {
    baseSubsectionDialog(
        subConfig = config.turnWristBrightDuration(),
        itemTextResId = R.string.unit_second_param,
        itemTextType = 2,
        titleResId = R.string.ds_screen_turn_wrist_bright_duration
    )?.show(childFragmentManager, DIALOG_SCREEN_TURN_WRIST_BRIGHT_DURATION)
}

const val DIALOG_SCREEN_LONG_TIME_BRIGHT_DURATION = "lt_bt_dur"
fun Fragment.showScreenLongTimeBrightDurationDialog(config: FcScreenVibrateConfig) {
    baseSubsectionDialog(
        subConfig = config.longTimeBrightDuration(),
        itemTextResId = R.string.unit_minute_param,
        itemTextType = 2,
        titleResId = R.string.ds_screen_long_time_bright_duration
    )?.show(childFragmentManager, DIALOG_SCREEN_LONG_TIME_BRIGHT_DURATION)
}

const val DIALOG_EXERCISE_STEP = "exercise_step"
fun Fragment.showExerciseStepDialog(value: Int) {
    SelectIntDialogFragment.newInstance(
        min = 1,
        max = 50,
        multiples = 1000,
        value = value,
        title = getString(R.string.exercise_goal_step),
        des = getString(R.string.unit_step)
    ).show(childFragmentManager, DIALOG_EXERCISE_STEP)
}

const val DIALOG_EXERCISE_CALORIE = "exercise_calorie"
fun Fragment.showExerciseCalorieDialog(value: Int) {
    SelectIntDialogFragment.newInstance(
        min = 1,
        max = 50,
        multiples = 30,
        value = value,
        title = getString(R.string.exercise_goal_calories),
        des = getString(R.string.unit_k_calories)
    ).show(childFragmentManager, DIALOG_EXERCISE_CALORIE)
}