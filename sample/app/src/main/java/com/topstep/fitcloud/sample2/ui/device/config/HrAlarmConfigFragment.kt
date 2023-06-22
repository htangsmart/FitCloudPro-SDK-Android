package com.topstep.fitcloud.sample2.ui.device.config

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentHrAlarmConfigBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.dialog.*
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.launchWithLog
import com.topstep.fitcloud.sample2.utils.setAllChildEnabled
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.FcHeartRateAlarmConfig
import com.topstep.fitcloud.sdk.v2.model.config.toBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcheartratealarmconfig
 *
 * ***Description**
 * Display and modify the config of heart rate alarm
 *
 * **Usage**
 * 1. [HealthMonitorConfigFragment]
 * According to whether [FcDeviceInfo.Feature.HEART_RATE_ALARM] supports, show or hide the entrance
 *
 * 2.[HrAlarmConfigFragment]
 * Display and modify
 */
class HrAlarmConfigFragment : BaseFragment(R.layout.fragment_hr_alarm_config), CompoundButton.OnCheckedChangeListener, SelectIntDialogFragment.Listener {

    private val viewBind: FragmentHrAlarmConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var config: FcHeartRateAlarmConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = deviceManager.configFeature.getHeartRateAlarmConfig()
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
                deviceManager.configFeature.observerHeartRateAlarmConfig().asFlow().collect {
                    if (config != it) {
                        config = it
                        updateUI()
                    }
                }
            }
        }

        viewBind.itemStaticSwitch.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemDynamicSwitch.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemStaticValue.clickTrigger(block = blockClick)
        viewBind.itemDynamicValue.clickTrigger(block = blockClick)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            if (buttonView == viewBind.itemStaticSwitch.getSwitchView()) {
                config.toBuilder().setStaticEnabled(isChecked).apply {
                    if (isChecked && config.getStaticValue() == 0) {
                        setStaticValue(DEFAULT_VALUE)
                    }
                }.create().saveConfig()
            } else {
                config.toBuilder().setDynamicEnabled(isChecked).apply {
                    if (isChecked && config.getDynamicValue() == 0) {
                        setDynamicValue(DEFAULT_VALUE)
                    }
                }.create().saveConfig()
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemStaticValue -> {
                showHrStaticDialog(config.getStaticValue())
            }
            viewBind.itemDynamicValue -> {
                showHrDynamicDialog(config.getDynamicValue())
            }
        }
    }

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        if (DIALOG_HR_STATIC == tag) {
            config.toBuilder().setStaticValue(selectValue).create().saveConfig()
        } else if (DIALOG_HR_DYNAMIC == tag) {
            config.toBuilder().setDynamicValue(selectValue).create().saveConfig()
        }
    }

    private fun FcHeartRateAlarmConfig.saveConfig() {
        applicationScope.launchWithLog {
            deviceManager.configFeature.setHeartRateAlarmConfig(this@saveConfig).await()
        }
        this@HrAlarmConfigFragment.config = this
        updateUI()
    }

    private fun updateUI() {
        val isConfigEnabled = viewBind.layoutContent.isEnabled

        //Static
        viewBind.itemStaticSwitch.getSwitchView().isChecked = config.isStaticEnabled()
        if (isConfigEnabled) {
            viewBind.itemStaticValue.isEnabled = config.isStaticEnabled()
        }
        viewBind.itemStaticValue.getTextView().text = getString(R.string.unit_bmp_unit, config.getStaticValue())

        //Dynamic
        viewBind.itemDynamicSwitch.getSwitchView().isChecked = config.isDynamicEnabled()
        if (isConfigEnabled) {
            viewBind.itemDynamicValue.isEnabled = config.isDynamicEnabled()
        }
        viewBind.itemDynamicValue.getTextView().text = getString(R.string.unit_bmp_unit, config.getDynamicValue())
    }

    companion object {
        private const val DEFAULT_VALUE = 100
    }
}