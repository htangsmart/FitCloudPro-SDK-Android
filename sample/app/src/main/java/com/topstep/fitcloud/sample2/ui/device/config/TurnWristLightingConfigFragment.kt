package com.topstep.fitcloud.sample2.ui.device.config

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentTurnWristLightingConfigBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.dialog.*
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcTurnWristLightingConfig
import com.topstep.fitcloud.sdk.v2.model.config.toBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcturnwristlightingconfig
 *
 * ***Description**
 * Display and modify the config of raise hand to lighting the device
 *
 * **Usage**
 * 1. [TurnWristLightingConfigFragment]
 * Display and modify
 */
class TurnWristLightingConfigFragment : BaseFragment(R.layout.fragment_turn_wrist_lighting_config), CompoundButton.OnCheckedChangeListener,
    TimePickerDialogFragment.Listener {

    private val viewBind: FragmentTurnWristLightingConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var config: FcTurnWristLightingConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = deviceManager.configFeature.getTurnWristLightingConfig()
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
                deviceManager.configFeature.observerTurnWristLightingConfig().asFlow().collect {
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
        }
    }

    override fun onDialogTimePicker(tag: String?, timeMinute: Int) {
        if (DIALOG_START_TIME == tag) {
            config.toBuilder().setStart(timeMinute).create().saveConfig()
        } else if (DIALOG_END_TIME == tag) {
            config.toBuilder().setEnd(timeMinute).create().saveConfig()
        }
    }

    private fun FcTurnWristLightingConfig.saveConfig() {
        applicationScope.launchWithLog {
            deviceManager.configFeature.setTurnWristLightingConfig(this@saveConfig).await()
        }
        this@TurnWristLightingConfigFragment.config = this
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
    }

}