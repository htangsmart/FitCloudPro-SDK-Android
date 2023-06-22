package com.topstep.fitcloud.sample2.ui.device.config

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentDndConfigBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.dialog.*
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDNDConfig
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.toBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcdndconfig
 *
 * ***Description**
 * Display and modify the dnd config
 *
 * **Usage**
 * 1. [DeviceConfigFragment]
 * According to whether [FcDeviceInfo.Feature.DND] supports, show or hide the entrance
 *
 * 2.[DNDConfigFragment]
 * Display and modify
 */
class DNDConfigFragment : BaseFragment(R.layout.fragment_dnd_config), CompoundButton.OnCheckedChangeListener, TimePickerDialogFragment.Listener {

    private val viewBind: FragmentDndConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var config: FcDNDConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = deviceManager.configFeature.getDNDConfig()
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
                deviceManager.configFeature.observerDNDConfig().asFlow().collect {
                    if (config != it) {
                        config = it
                        updateUI()
                    }
                }
            }
        }

        viewBind.itemAllDay.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemPeriodTime.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemStartTime.clickTrigger(block = blockClick)
        viewBind.itemEndTime.clickTrigger(block = blockClick)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            if (buttonView == viewBind.itemAllDay.getSwitchView()) {
                config.toBuilder().setEnableAllDay(isChecked).create().saveConfig()
            } else if (buttonView == viewBind.itemPeriodTime.getSwitchView()) {
                config.toBuilder().setEnabledPeriodTime(isChecked).create().saveConfig()
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
        }
    }

    override fun onDialogTimePicker(tag: String?, timeMinute: Int) {
        if (DIALOG_START_TIME == tag) {
            config.toBuilder().setStart(timeMinute).create().saveConfig()
        } else if (DIALOG_END_TIME == tag) {
            config.toBuilder().setEnd(timeMinute).create().saveConfig()
        }
    }

    private fun FcDNDConfig.saveConfig() {
        applicationScope.launchWithLog {
            deviceManager.configFeature.setDNDConfig(this@saveConfig).await()
        }
        this@DNDConfigFragment.config = this
        updateUI()
    }

    private fun updateUI() {
        val isConfigEnabled = viewBind.layoutContent.isEnabled

        viewBind.itemAllDay.getSwitchView().isChecked = config.isEnabledAllDay()
        viewBind.itemPeriodTime.getSwitchView().isChecked = config.isEnabledPeriodTime()
        if (isConfigEnabled) {//When device is disconnected, disabled the click event
            viewBind.itemStartTime.isEnabled = config.isEnabledPeriodTime()
            viewBind.itemEndTime.isEnabled = config.isEnabledPeriodTime()
        }
        viewBind.itemStartTime.getTextView().text = FormatterUtil.minute2Hmm(config.getStart())
        viewBind.itemEndTime.getTextView().text = FormatterUtil.minute2Hmm(config.getEnd())
    }

}