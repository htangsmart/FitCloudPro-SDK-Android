package com.topstep.fitcloud.sample2.ui.device.config

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.CompoundButton
import com.github.kilnn.tool.system.SystemUtil
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentFunctionConfigBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcFunctionConfig
import com.topstep.fitcloud.sdk.v2.model.config.toBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcfunctionconfig
 *
 * ***Description**
 * Display and modify the simple functions on the device
 *
 * **Usage**
 * 1. [FunctionConfigFragment]
 * Display and modify
 */
class FunctionConfigFragment : BaseFragment(R.layout.fragment_function_config), CompoundButton.OnCheckedChangeListener {

    private val viewBind: FragmentFunctionConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var config: FcFunctionConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = deviceManager.configFeature.getFunctionConfig()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                    viewBind.layoutContent.setAllChildEnabled(it)
                    updateUI()
                }
            }
            launch {
                deviceManager.configFeature.observerFunctionConfig().asFlow().collect {
                    if (config != it) {
                        config = it
                        updateUI()
                    }
                }
            }
        }

        viewBind.itemWearRightHand.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemEnhancedMeasurement.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemTimeFormat12Hour.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemLengthUnitImperial.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemTemperatureUnitFahrenheit.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemDisplayWeather.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemDisconnectReminder.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemDisplayExerciseGoal.getSwitchView().setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            val flag = when (buttonView) {
                viewBind.itemWearRightHand.getSwitchView() -> {
                    FcFunctionConfig.Flag.WEAR_WAY
                }
                viewBind.itemEnhancedMeasurement.getSwitchView() -> {
                    FcFunctionConfig.Flag.ENHANCED_MEASUREMENT
                }
                viewBind.itemTimeFormat12Hour.getSwitchView() -> {
                    FcFunctionConfig.Flag.TIME_FORMAT
                }
                viewBind.itemLengthUnitImperial.getSwitchView() -> {
                    FcFunctionConfig.Flag.LENGTH_UNIT
                }
                viewBind.itemTemperatureUnitFahrenheit.getSwitchView() -> {
                    FcFunctionConfig.Flag.TEMPERATURE_UNIT
                }
                viewBind.itemDisplayWeather.getSwitchView() -> {
                    if (isChecked) {
                        //General weather function depends on location function
                        if (!SystemUtil.isLocationEnabled(requireContext())) {
                            buttonView.isChecked = false
                            requireContext().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            return
                        }
                        PermissionHelper.requestWeatherLocation(this) {
                        }
                    }
                    FcFunctionConfig.Flag.WEATHER_DISPLAY
                }
                viewBind.itemDisconnectReminder.getSwitchView() -> {
                    FcFunctionConfig.Flag.DISCONNECT_REMINDER
                }
                viewBind.itemDisplayExerciseGoal.getSwitchView() -> {
                    FcFunctionConfig.Flag.EXERCISE_GOAL_DISPLAY
                }
                else -> {
                    throw IllegalArgumentException()
                }
            }
            config.toBuilder().setFlagEnabled(flag, isChecked).create().saveConfig()
        }
    }

    private fun FcFunctionConfig.saveConfig() {
        applicationScope.launchWithLog {
            deviceManager.configFeature.setFunctionConfig(this@saveConfig).await()
        }
        this@FunctionConfigFragment.config = this
        updateUI()
    }

    private fun updateUI() {
        viewBind.itemWearRightHand.getSwitchView().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.WEAR_WAY)
        viewBind.itemEnhancedMeasurement.getSwitchView().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.ENHANCED_MEASUREMENT)
        viewBind.itemTimeFormat12Hour.getSwitchView().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.TIME_FORMAT)
        viewBind.itemLengthUnitImperial.getSwitchView().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.LENGTH_UNIT)
        viewBind.itemTemperatureUnitFahrenheit.getSwitchView().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.TEMPERATURE_UNIT)
        viewBind.itemDisplayWeather.getSwitchView().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.WEATHER_DISPLAY)
        viewBind.itemDisconnectReminder.getSwitchView().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.DISCONNECT_REMINDER)
        viewBind.itemDisplayExerciseGoal.getSwitchView().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.EXERCISE_GOAL_DISPLAY)
    }
}