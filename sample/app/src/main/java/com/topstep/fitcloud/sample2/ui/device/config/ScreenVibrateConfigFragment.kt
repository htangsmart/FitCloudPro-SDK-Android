package com.topstep.fitcloud.sample2.ui.device.config

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentScreenVibrateConfigBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.dialog.*
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.launchWithLog
import com.topstep.fitcloud.sample2.utils.setAllChildEnabled
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.FcScreenVibrateConfig
import com.topstep.fitcloud.sdk.v2.model.config.toBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcscreenvibrateconfig
 *
 * ***Description**
 * Display and modify the screen and vibrate config
 *
 * **Usage**
 * 1. [DeviceConfigFragment]
 * According to whether [FcDeviceInfo.Feature.SCREEN_VIBRATE] supports, show or hide the entrance
 *
 * 2.[ScreenVibrateConfigFragment]
 * Display and modify
 */
class ScreenVibrateConfigFragment : BaseFragment(R.layout.fragment_screen_vibrate_config), ChoiceIntDialogFragment.Listener, CompoundButton.OnCheckedChangeListener {

    private val viewBind: FragmentScreenVibrateConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var config: FcScreenVibrateConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = deviceManager.configFeature.getScreenVibrateConfig()
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
                deviceManager.configFeature.observerScreenVibrateConfig().asFlow().collect {
                    if (config != it) {
                        config = it
                        updateUI()
                    }
                }
            }
        }

        viewBind.itemVibrate.clickTrigger(block = blockClick)
        viewBind.itemBrightness.clickTrigger(block = blockClick)
        viewBind.itemBrightDuration.clickTrigger(block = blockClick)
        viewBind.itemTurnWristBrightDuration.clickTrigger(block = blockClick)
        viewBind.itemLtBrightIsEnabled.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemLtBrightDuration.clickTrigger(block = blockClick)
        viewBind.itemAlwaysBright.getSwitchView().setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            if (buttonView == viewBind.itemLtBrightIsEnabled.getSwitchView()) {
                config.toBuilder().apply {
                    longTimeBrightDuration().setEnabled(isChecked)
                }.create().saveConfig()
            } else if (buttonView == viewBind.itemAlwaysBright.getSwitchView()) {
                config.toBuilder().apply {
                    alwaysBright().setEnabled(isChecked)
                }.create().saveConfig()
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemVibrate -> {
                showVibrateDialog(config)
            }
            viewBind.itemBrightness -> {
                showScreenBrightnessDialog(config)
            }
            viewBind.itemBrightDuration -> {
                showScreenBrightDurationDialog(config)
            }
            viewBind.itemTurnWristBrightDuration -> {
                showScreenTurnWristBrightDurationDialog(config)
            }
            viewBind.itemLtBrightDuration -> {
                showScreenLongTimeBrightDurationDialog(config)
            }
        }
    }

    override fun onDialogChoiceInt(tag: String?, selectValue: Int) {
        when (tag) {
            DIALOG_VIBRATE -> {
                config.toBuilder().apply {
                    vibrate().setSelectPosition(selectValue)
                }.create().saveConfig()
            }
            DIALOG_SCREEN_BRIGHTNESS -> {
                config.toBuilder().apply {
                    brightness().setSelectPosition(selectValue)
                }.create().saveConfig()
            }
            DIALOG_SCREEN_BRIGHT_DURATION -> {
                config.toBuilder().apply {
                    brightDuration().setSelectPosition(selectValue)
                }.create().saveConfig()
            }
            DIALOG_SCREEN_TURN_WRIST_BRIGHT_DURATION -> {
                config.toBuilder().apply {
                    turnWristBrightDuration().setSelectPosition(selectValue)
                }.create().saveConfig()
            }
            DIALOG_SCREEN_LONG_TIME_BRIGHT_DURATION -> {
                config.toBuilder().apply {
                    longTimeBrightDuration().setSelectPosition(selectValue)
                }.create().saveConfig()
            }
        }
    }

    private fun FcScreenVibrateConfig.saveConfig() {
        applicationScope.launchWithLog {
            deviceManager.configFeature.setScreenVibrateConfig(this@saveConfig).await()
        }
        this@ScreenVibrateConfigFragment.config = this
        updateUI()
    }

    private fun updateUI() {
        //Config of vibrate level
        config.vibrate().let {
            if (it.getItems()?.isNotEmpty() == true) {
                viewBind.itemVibrate.visibility = View.VISIBLE
                viewBind.itemVibrate.getTextView().text = getString(R.string.unit_level_param, it.getSelectPosition())
            } else {//no items representative does not support this setting
                viewBind.itemVibrate.visibility = View.GONE
            }
        }

        //Config of brightness level
        config.brightness().let {
            if (it.getItems()?.isNotEmpty() == true) {
                viewBind.itemBrightness.visibility = View.VISIBLE
                viewBind.itemBrightness.getTextView().text = getString(R.string.unit_level_param, it.getSelectPosition() + 1)
            } else {//no items representative does not support this setting
                viewBind.itemBrightness.visibility = View.GONE
            }
        }

        //Config of bright screen duration
        config.brightDuration().let {
            val items = it.getItems()
            if (items?.isNotEmpty() == true) {
                viewBind.itemBrightDuration.visibility = View.VISIBLE
                viewBind.itemBrightDuration.getTextView().text = getString(R.string.unit_second_param, items[it.getSelectPosition()])
            } else {//no items representative does not support this setting
                viewBind.itemBrightDuration.visibility = View.GONE
            }
        }

        //Config of duration when bright screen by turn wrist
        config.turnWristBrightDuration().let {
            val items = it.getItems()
            if (items?.isNotEmpty() == true) {
                viewBind.itemTurnWristBrightDuration.visibility = View.VISIBLE
                viewBind.itemTurnWristBrightDuration.getTextView().text = getString(R.string.unit_second_param, items[it.getSelectPosition()])
            } else {
                viewBind.itemTurnWristBrightDuration.visibility = View.GONE
            }
        }

        //Config of long lasting bright screen
        config.longTimeBrightDuration().let {
            val items = it.getItems()
            if (items?.isNotEmpty() == true) {
                viewBind.layoutLtBright.visibility = View.VISIBLE
                viewBind.itemLtBrightIsEnabled.getSwitchView().isChecked = it.isEnabled()
                viewBind.itemLtBrightDuration.getTextView().text = getString(R.string.unit_second_param, items[it.getSelectPosition()])
            } else {
                viewBind.layoutLtBright.visibility = View.GONE
            }
        }

        //Config of keep screen always bright
        config.alwaysBright().let {
            if (it.isSupport()) {
                viewBind.itemAlwaysBright.visibility = View.VISIBLE
                viewBind.itemAlwaysBright.getSwitchView().isChecked = it.isEnabled()
            } else {
                viewBind.itemAlwaysBright.visibility = View.GONE
            }
        }
    }

}