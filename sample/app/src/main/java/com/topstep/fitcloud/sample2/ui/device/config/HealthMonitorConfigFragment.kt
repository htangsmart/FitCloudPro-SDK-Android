package com.topstep.fitcloud.sample2.ui.device.config

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentHealthMonitorConfigBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.dialog.*
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.FcHealthMonitorConfig
import com.topstep.fitcloud.sdk.v2.model.config.toBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fchealthmonitorconfig
 *
 * ***Description**
 * Display and modify the config of data monitor
 *
 * **Usage**
 * 1. [HealthMonitorConfigFragment]
 * Display and modify
 */
class HealthMonitorConfigFragment : BaseFragment(R.layout.fragment_health_monitor_config), CompoundButton.OnCheckedChangeListener,
    TimePickerDialogFragment.Listener, SelectIntDialogFragment.Listener {

    private val viewBind: FragmentHealthMonitorConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var config: FcHealthMonitorConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = deviceManager.configFeature.getHealthMonitorConfig()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                    viewBind.layoutContent.setAllChildEnabled(it)
                    viewBind.itemIntervalTime.isVisible = deviceManager.configFeature.getDeviceInfo().isSupportFeature(FcDeviceInfo.Feature.HEALTH_MONITOR_CONFIG_INTERVAL)
                    viewBind.itemHeartRateAlarm.isVisible = deviceManager.configFeature.getDeviceInfo().isSupportFeature(FcDeviceInfo.Feature.HEART_RATE_ALARM)
                    viewBind.itemBloodPressureAlarm.isVisible = deviceManager.configFeature.getDeviceInfo().isSupportFeature(FcDeviceInfo.Feature.BLOOD_PRESSURE_ALARM)
                    updateUI()
                }
            }
            launch {
                deviceManager.configFeature.observerHealthMonitorConfig().asFlow().collect {
                    if (config != it) {
                        config = it
                        updateUI()
                    }
                }
            }
        }

        viewBind.itemIsEnabled.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemStartTime.clickTrigger(block = blockClick)
        viewBind.itemEndTime.clickTrigger(block = blockClick)
        viewBind.itemIntervalTime.clickTrigger(block = blockClick)
        viewBind.itemHeartRateAlarm.clickTrigger(block = blockClick)
        viewBind.itemBloodPressureAlarm.clickTrigger(block = blockClick)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            if (buttonView == viewBind.itemIsEnabled.getSwitchView()) {
                config.toBuilder().setEnabled(isChecked).create().saveConfig()
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemStartTime -> {
                showStartTimeDialog(config.getStart())
            }
            viewBind.itemEndTime -> {
                showEndTimeDialog(config.getEnd())
            }
            viewBind.itemIntervalTime -> {
                showIntervalDialog(config.getInterval(), 5, 720)
            }
            viewBind.itemHeartRateAlarm -> {
                findNavController().navigate(HealthMonitorConfigFragmentDirections.toHrAlarmConfig())
            }
            viewBind.itemBloodPressureAlarm -> {
                findNavController().navigate(HealthMonitorConfigFragmentDirections.toBpAlarmConfig())
            }
        }
    }

    override fun onDialogTimePicker(tag: String?, timeMinute: Int) {
        if (DIALOG_START_TIME == tag) {
            config.toBuilder().setStart(timeMinute).create().saveConfig()
        } else if (DIALOG_END_TIME == tag) {
            config.toBuilder().setEnd(timeMinute).create().saveConfig()
        }
    }

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        if (DIALOG_INTERVAL_TIME == tag) {
            config.toBuilder().setInterval(selectValue).create().saveConfig()
        }
    }

    private fun FcHealthMonitorConfig.saveConfig() {
        applicationScope.launchWithLog {
            deviceManager.configFeature.setHealthMonitorConfig(this@saveConfig).await()
        }
        this@HealthMonitorConfigFragment.config = this
        updateUI()
    }

    private fun updateUI() {
        val isConfigEnabled = viewBind.layoutContent.isEnabled

        viewBind.itemIsEnabled.getSwitchView().isChecked = config.isEnabled()
        if (isConfigEnabled) {//When device is disconnected, disabled the click event
            viewBind.layoutDetail.setAllChildEnabled(config.isEnabled())
        }

        viewBind.itemStartTime.getTextView().text = FormatterUtil.minute2Hmm(config.getStart())
        viewBind.itemEndTime.getTextView().text = FormatterUtil.minute2Hmm(config.getEnd())
        viewBind.itemIntervalTime.getTextView().text = getString(R.string.unit_minute_param, config.getInterval())
    }

}