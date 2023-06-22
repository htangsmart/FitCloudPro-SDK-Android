package com.topstep.fitcloud.sample2.ui.combine

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.github.kilnn.wheellayout.OneWheelLayout
import com.github.kilnn.wheellayout.WheelIntConfig
import com.github.kilnn.wheellayout.WheelIntFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.DeviceManager
import com.topstep.fitcloud.sample2.databinding.FragmentExerciseGoalBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.config.ExerciseGoal
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.dialog.*
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.utils.km2mi
import com.topstep.fitcloud.sample2.utils.mi2km
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcFunctionConfig

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/10.Other-Features#setting-exercise-goal
 *
 * ***Description**
 * Display and modify the exercise goal
 *
 * **Usage**
 * 1. [ExerciseGoalFragment]
 * Display and modify
 *
 * 2.[DeviceManager]
 * Set the exercise goal to device when device connected or goal changed.
 */
class ExerciseGoalFragment : BaseFragment(R.layout.fragment_exercise_goal),
    SelectIntDialogFragment.Listener, DistanceMetricDialogFragment.Listener, DistanceImperialDialogFragment.Listener {

    private val viewBind: FragmentExerciseGoalBinding by viewBinding()

    private val authedUserId: Long = Injector.requireAuthedUserId()
    private val deviceManager = Injector.getDeviceManager()
    private val exerciseGoalRepository = Injector.getExerciseGoalRepository()

    private var isLengthMetric: Boolean = false
    private lateinit var exerciseGoal: ExerciseGoal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isLengthMetric = !deviceManager.configFeature.getFunctionConfig().isFlagEnabled(FcFunctionConfig.Flag.LENGTH_UNIT)
        exerciseGoal = exerciseGoalRepository.flowCurrent.value
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateStep()
        updateDistance()
        updateCalories()
        viewBind.itemStep.clickTrigger(block = blockClick)
        viewBind.itemDistance.clickTrigger(block = blockClick)
        viewBind.itemCalories.clickTrigger(block = blockClick)
    }

    private fun updateStep() {
        viewBind.itemStep.getTextView().text = getString(R.string.unit_step_param, exerciseGoal.step)
    }

    private fun updateDistance() {
        if (isLengthMetric) {
            viewBind.itemDistance.getTextView().text = getString(
                R.string.unit_km_param, FormatterUtil.decimal1Str(getHalfFloat(exerciseGoal.distance))
            )
        } else {
            viewBind.itemDistance.getTextView().text = getString(
                R.string.unit_mi_param, FormatterUtil.decimal1Str(getHalfFloat(exerciseGoal.distance.km2mi()))
            )
        }
    }

    private fun updateCalories() {
        viewBind.itemCalories.getTextView().text = getString(
            R.string.unit_k_calories_param, exerciseGoal.calorie.toString()
        )
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemStep -> {
                showExerciseStepDialog(exerciseGoal.step)
            }
            viewBind.itemDistance -> {
                if (isLengthMetric) {
                    DistanceMetricDialogFragment().show(childFragmentManager, null)
                } else {
                    DistanceImperialDialogFragment().show(childFragmentManager, null)
                }
            }
            viewBind.itemCalories -> {
                showExerciseCalorieDialog(exerciseGoal.calorie)
            }
        }
    }

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        if (DIALOG_EXERCISE_STEP == tag) {
            exerciseGoal = exerciseGoal.copy(step = selectValue)
            updateStep()
            exerciseGoalRepository.modify(authedUserId, exerciseGoal)
        } else if (DIALOG_EXERCISE_CALORIE == tag) {
            exerciseGoal = exerciseGoal.copy(calorie = selectValue)
            updateCalories()
            exerciseGoalRepository.modify(authedUserId, exerciseGoal)
        }
    }


    override fun dialogGetDistanceMetric(): Float {
        return getHalfFloat(exerciseGoal.distance)
    }

    override fun dialogSetDistanceMetric(value: Float) {
        exerciseGoal = exerciseGoal.copy(distance = value)
        updateDistance()
        exerciseGoalRepository.modify(authedUserId, exerciseGoal)
    }

    override fun dialogGetDistanceImperial(): Float {
        return getHalfFloat(exerciseGoal.distance.km2mi())
    }

    override fun dialogSetDistanceImperial(value: Float) {
        exerciseGoal = exerciseGoal.copy(distance = value.mi2km())
        updateDistance()
        exerciseGoalRepository.modify(authedUserId, exerciseGoal)
    }

    /**
     * Convert to numbers in multiples of 0.5。Such as：
     * 0.24 -> 0
     * 0.25 -> 0.5
     * 0.49 -> 0.5
     * 1.74 -> 1.5
     */
    private fun getHalfFloat(value: Float): Float {
        if (value <= 0) return 0.0f
        val count1 = (value / 0.5f).toInt()
        val count2 = (value / 0.25f).toInt()
        return if (count1 * 2 != count2) (count1 + 1) * 0.5f else count1 * 0.5f
    }
}

class DistanceMetricDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val multiples = 0.5f
        val listener = parentFragment as? Listener

        val layout = OneWheelLayout(requireContext())
        layout.setConfig(WheelIntConfig(2, 80, false, getString(R.string.unit_km), object : WheelIntFormatter {
            override fun format(index: Int, value: Int): String {
                return FormatterUtil.decimal1Str(value * multiples)
            }
        }))

        if (listener != null) {
            layout.setValue((listener.dialogGetDistanceMetric() / multiples).toInt())
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.exercise_goal_distance)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener?.dialogSetDistanceMetric(layout.getValue() * multiples)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    interface Listener {
        fun dialogGetDistanceMetric(): Float
        fun dialogSetDistanceMetric(value: Float)
    }
}

class DistanceImperialDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val multiples = 0.5f
        val listener = parentFragment as? Listener

        val layout = OneWheelLayout(requireContext())
        layout.setConfig(WheelIntConfig(1, 50, false, getString(R.string.unit_mi), object : WheelIntFormatter {
            override fun format(index: Int, value: Int): String {
                return FormatterUtil.decimal1Str(value * multiples)
            }
        }))

        if (listener != null) {
            layout.setValue((listener.dialogGetDistanceImperial() / multiples).toInt())
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.exercise_goal_distance)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener?.dialogSetDistanceImperial(layout.getValue() * multiples)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    interface Listener {
        fun dialogGetDistanceImperial(): Float
        fun dialogSetDistanceImperial(value: Float)
    }
}
