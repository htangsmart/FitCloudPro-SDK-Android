package com.topstep.fitcloud.sample2.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.entity.StepItemEntity
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.utils.DateTimeUtils
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.utils.km2mi
import com.topstep.fitcloud.sdk.v2.model.config.FcFunctionConfig
import kotlinx.coroutines.runBlocking
import java.util.*

class StepFragment : DataListFragment<StepItemEntity>() {

    override val layoutId: Int = R.layout.fragment_step

    private lateinit var tvStep: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvCalories: TextView

    private val today = Date()
    private val deviceManager = Injector.getDeviceManager()
    private var isUnitMetric = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isUnitMetric = !deviceManager.configFeature.getFunctionConfig().isFlagEnabled(FcFunctionConfig.Flag.LENGTH_UNIT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvStep = view.findViewById(R.id.tv_step)
        tvDistance = view.findViewById(R.id.tv_distance)
        tvCalories = view.findViewById(R.id.tv_calories)
        super.onViewCreated(view, savedInstanceState)
    }

    override val valueFormat: DataListAdapter.ValueFormat<StepItemEntity> = object : DataListAdapter.ValueFormat<StepItemEntity> {
        override fun format(context: Context, obj: StepItemEntity): String {
            return timeFormat.format(obj.time) + "    " +
                    context.getString(R.string.unit_step_param, obj.step)
        }
    }

    override fun queryData(date: Date): List<StepItemEntity>? {
        val data = runBlocking { syncDataRepository.queryStep(authedUserId, date) }
        if (DateTimeUtils.isSameDay(date, today)) {
            displayByTodayData()
        } else {
            displayByItemsData(data)
        }
        return data
    }

    private fun displayByTodayData() {
        val todayData = runBlocking { syncDataRepository.queryTodayStep(authedUserId) }
        var step = 0
        var distance = 0f
        var calories = 0f
        if (todayData != null && DateTimeUtils.isSameDay(Date(todayData.timestamp), today)) {
            step = todayData.step
            distance = todayData.distance
            calories = todayData.calories
        }
        displayTotal(step, distance, calories)
    }

    private fun displayByItemsData(data: List<StepItemEntity>?) {
        var step = 0
        var distance = 0f
        var calories = 0f
        if (data != null) {
            for (item in data) {
                step += item.step
                distance += item.distance
                calories += item.calories
            }
        }
        displayTotal(step, distance, calories)
    }

    private fun displayTotal(step: Int, distance: Float, calories: Float) {
        tvStep.text = getString(R.string.unit_step_param, step)
        if (isUnitMetric) {
            tvDistance.text = getString(R.string.unit_km_param, FormatterUtil.decimal1Str(distance))
        } else {
            tvDistance.text = getString(R.string.unit_km_param, FormatterUtil.decimal1Str(distance.km2mi()))
        }
        tvCalories.text = getString(R.string.unit_k_calories_param, FormatterUtil.decimal1Str(calories))
    }
}