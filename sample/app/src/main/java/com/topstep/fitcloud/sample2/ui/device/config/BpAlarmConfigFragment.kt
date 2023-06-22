package com.topstep.fitcloud.sample2.ui.device.config

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentBpAlarmConfigBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.dialog.*
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcBloodPressureAlarmConfig
import com.topstep.fitcloud.sdk.v2.model.config.FcBloodPressureConfig
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.toBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcbloodpressurealarmconfig
 *
 * ***Description**
 * Display and modify the config of blood pressure alarm
 *
 * **Usage**
 * 1. [HealthMonitorConfigFragment]
 * According to whether [FcDeviceInfo.Feature.BLOOD_PRESSURE_ALARM] supports, show or hide the entrance
 *
 * 2.[BpAlarmConfigFragment]
 * Display and modify
 *
 * 3.[BloodPressureConfigFragment]
 * Check conflict
 */
class BpAlarmConfigFragment : BaseFragment(R.layout.fragment_bp_alarm_config), CompoundButton.OnCheckedChangeListener, SelectIntDialogFragment.Listener {

    private val viewBind: FragmentBpAlarmConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var config: FcBloodPressureAlarmConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = deviceManager.configFeature.getBloodPressureAlarmConfig()
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
                deviceManager.configFeature.observerBloodPressureAlarmConfig().asFlow().collect {
                    if (config != it) {
                        config = it
                        updateUI()
                    }
                }
            }
        }

        viewBind.itemIsEnabled.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemSbpUpper.clickTrigger(block = blockClick)
        viewBind.itemSbpLower.clickTrigger(block = blockClick)
        viewBind.itemDbpUpper.clickTrigger(block = blockClick)
        viewBind.itemDbpLower.clickTrigger(block = blockClick)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            config.toBuilder().setEnabled(isChecked).apply {
                if (isChecked) {
                    if (config.getSbpUpperLimit() == 0) {
                        setSbpUpperLimit(DEFAULT_SBP_UPPER)
                    }
                    if (config.getSbpLowerLimit() == 0) {
                        setSbpLowerLimit(DEFAULT_SBP_LOWER)
                    }
                    if (config.getDbpUpperLimit() == 0) {
                        setDbpUpperLimit(DEFAULT_DBP_UPPER)
                    }
                    if (config.getDbpLowerLimit() == 0) {
                        setDbpLowerLimit(DEFAULT_DBP_LOWER)
                    }
                }
            }.create().saveConfig()
            if (isChecked) {
                if (!checkSbpConflict()) {
                    checkDbpConflict()
                }
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemSbpUpper -> {
                showSbpUpperDialog(config.getSbpUpperLimit())
            }
            viewBind.itemSbpLower -> {
                showSbpLowerDialog(config.getSbpLowerLimit())
            }
            viewBind.itemDbpUpper -> {
                showDbpUpperDialog(config.getDbpUpperLimit())
            }
            viewBind.itemDbpLower -> {
                showDbpLowerDialog(config.getDbpLowerLimit())
            }
        }
    }

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        if (DIALOG_SBP_UPPER == tag || DIALOG_SBP_LOWER == tag) {
            config.toBuilder().apply {
                if (DIALOG_SBP_UPPER == tag) {
                    setSbpUpperLimit(selectValue)
                    if (selectValue < config.getSbpLowerLimit()) {
                        setSbpLowerLimit(selectValue)
                    }
                } else {
                    setSbpLowerLimit(selectValue)
                    if (selectValue > config.getSbpUpperLimit()) {
                        setSbpUpperLimit(selectValue)
                    }
                }
            }.create().also {
                checkSbpConflict()
            }.saveConfig()
        } else if (DIALOG_DBP_UPPER == tag || DIALOG_DBP_LOWER == tag) {
            config.toBuilder().apply {
                if (DIALOG_DBP_UPPER == tag) {
                    setDbpUpperLimit(selectValue)
                    if (selectValue < config.getDbpLowerLimit()) {
                        setDbpLowerLimit(selectValue)
                    }
                } else {
                    setDbpLowerLimit(selectValue)
                    if (selectValue > config.getDbpUpperLimit()) {
                        setDbpUpperLimit(selectValue)
                    }
                }
            }.create().also {
                checkDbpConflict()
            }.saveConfig()
        }
    }

    private fun FcBloodPressureAlarmConfig.saveConfig() {
        applicationScope.launchWithLog {
            deviceManager.configFeature.setBloodPressureAlarmConfig(this@saveConfig).await()
        }
        this@BpAlarmConfigFragment.config = this
        updateUI()
    }

    private fun updateUI() {
        val isConfigEnabled = viewBind.layoutContent.isEnabled

        viewBind.itemIsEnabled.getSwitchView().isChecked = config.isEnabled()
        if (isConfigEnabled) {
            viewBind.layoutSbp.setAllChildEnabled(config.isEnabled())
            viewBind.layoutDbp.setAllChildEnabled(config.isEnabled())
        }

        viewBind.itemSbpUpper.getTextView().text = getString(R.string.unit_mmhg_param, FormatterUtil.intStr(config.getSbpUpperLimit()))
        viewBind.itemSbpLower.getTextView().text = getString(R.string.unit_mmhg_param, FormatterUtil.intStr(config.getSbpLowerLimit()))
        viewBind.itemDbpUpper.getTextView().text = getString(R.string.unit_mmhg_param, FormatterUtil.intStr(config.getDbpUpperLimit()))
        viewBind.itemDbpLower.getTextView().text = getString(R.string.unit_mmhg_param, FormatterUtil.intStr(config.getDbpLowerLimit()))
    }

    /**
     * Check if there is a conflict with [FcBloodPressureConfig] sbp value
     * @return True for has conflict，false for not
     */
    private fun checkSbpConflict(): Boolean {
        if (deviceManager.configFeature.getDeviceInfo().isSupportFeature(FcDeviceInfo.Feature.BLOOD_PRESSURE_ALARM)) {
            return false
        }
        val bloodPressureConfig = deviceManager.configFeature.getBloodPressureConfig()
        if (!bloodPressureConfig.isEnabled()) {
            return false
        }
        val sbp = bloodPressureConfig.getSbp()
        if (config.getSbpUpperLimit() <= sbp * 1.1f || config.getSbpLowerLimit() >= sbp * 0.9f) {
            BloodPressureConflictDialogFragment().show(childFragmentManager, null)
            return true
        }
        return false
    }

    /**
     * Check if there is a conflict with [FcBloodPressureConfig] dbp value
     * @return True for has conflict，false for not
     */
    private fun checkDbpConflict(): Boolean {
        if (deviceManager.configFeature.getDeviceInfo().isSupportFeature(FcDeviceInfo.Feature.BLOOD_PRESSURE_ALARM)) {
            return false
        }
        val bloodPressureConfig = deviceManager.configFeature.getBloodPressureConfig()
        if (!bloodPressureConfig.isEnabled()) {
            return false
        }
        val dbp = bloodPressureConfig.getDbp()
        if (config.getDbpUpperLimit() <= dbp * 1.1f || config.getDbpLowerLimit() >= dbp * 0.9f) {
            BloodPressureConflictDialogFragment().show(childFragmentManager, null)
            return true
        }
        return false
    }

    companion object {
        private const val DEFAULT_SBP_UPPER = 140
        private const val DEFAULT_SBP_LOWER = 90
        private const val DEFAULT_DBP_UPPER = 90
        private const val DEFAULT_DBP_LOWER = 60
    }
}