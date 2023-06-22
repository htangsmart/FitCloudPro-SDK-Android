package com.topstep.fitcloud.sample2.ui.device.config

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDialogFragment
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentBloodPressureConfigBinding
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
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcbloodpressureconfig
 *
 * ***Description**
 * Display and modify the reference blood pressure values
 *
 * **Usage**
 * 1. [DeviceConfigFragment]
 * Only show the entrance when [FcDeviceInfo.Feature.BLOOD_PRESSURE] is supported and [FcDeviceInfo.Feature.BLOOD_PRESSURE_AIR_PUMP] isn't supported
 *
 * 2. [BloodPressureConfigFragment]
 * Display and modify
 *
 * 3. [BpAlarmConfigFragment]
 * Check conflict
 */
class BloodPressureConfigFragment : BaseFragment(R.layout.fragment_blood_pressure_config), CompoundButton.OnCheckedChangeListener,
    SelectIntDialogFragment.Listener {

    private val viewBind: FragmentBloodPressureConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var config: FcBloodPressureConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = deviceManager.configFeature.getBloodPressureConfig()
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
                deviceManager.configFeature.observerBloodPressureConfig().asFlow().collect {
                    if (config != it) {
                        config = it
                        updateUI()
                    }
                }
            }
        }

        viewBind.itemIsEnabled.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemDbp.clickTrigger(block = blockClick)
        viewBind.itemSbp.clickTrigger(block = blockClick)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            if (buttonView == viewBind.itemIsEnabled.getSwitchView()) {
                config.toBuilder().setEnabled(isChecked).apply {
                    if (isChecked) {
                        if (config.getSbp() == 0) {
                            setSbp(SBP_DEFAULT)
                        }
                        if (config.getDbp() == 0) {
                            setDbp(DBP_DEFAULT)
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
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemDbp -> {
                showDbpDialog(config.getDbp())
            }
            viewBind.itemSbp -> {
                showSbpDialog(config.getSbp())
            }
        }
    }

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        if (DIALOG_DBP == tag) {
            config.toBuilder().setDbp(selectValue).create().saveConfig()
            checkDbpConflict()
        } else if (DIALOG_SBP == tag) {
            config.toBuilder().setSbp(selectValue).create().saveConfig()
            checkSbpConflict()
        }
    }

    private fun FcBloodPressureConfig.saveConfig() {
        applicationScope.launchWithLog {
            deviceManager.configFeature.setBloodPressureConfig(this@saveConfig).await()
        }
        this@BloodPressureConfigFragment.config = this
        updateUI()
    }

    private fun updateUI() {
        val isConfigEnabled = viewBind.layoutContent.isEnabled

        viewBind.itemIsEnabled.getSwitchView().isChecked = config.isEnabled()
        if (isConfigEnabled) {//When device is disconnected, disabled the click event
            viewBind.layoutDetail.setAllChildEnabled(config.isEnabled())
        }

        viewBind.itemDbp.getTextView().text = FormatterUtil.intStr(config.getDbp())
        viewBind.itemSbp.getTextView().text = FormatterUtil.intStr(config.getSbp())
    }

    /**
     * Check if there is a conflict with [FcBloodPressureAlarmConfig] sbp range
     * @return True for has conflict，false for not
     */
    private fun checkSbpConflict(): Boolean {
        val alarmConfig = deviceManager.configFeature.getBloodPressureAlarmConfig()
        if (!alarmConfig.isEnabled()) return false
        if (alarmConfig.getSbpUpperLimit() <= config.getSbp() * 1.1f
            || alarmConfig.getSbpLowerLimit() >= config.getSbp() * 0.9f
        ) {
            BloodPressureConflictDialogFragment().show(childFragmentManager, null)
            return true
        }
        return false
    }

    /**
     * Check if there is a conflict with [FcBloodPressureAlarmConfig] dbp range
     * @return True for has conflict，false for not
     */
    private fun checkDbpConflict(): Boolean {
        val alarmConfig = deviceManager.configFeature.getBloodPressureAlarmConfig()
        if (!alarmConfig.isEnabled()) return false
        if (alarmConfig.getDbpUpperLimit() <= config.getDbp() * 1.1f
            || alarmConfig.getDbpLowerLimit() >= config.getDbp() * 0.9f
        ) {
            BloodPressureConflictDialogFragment().show(childFragmentManager, null)
            return true
        }
        return false
    }
}

class BloodPressureConflictDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.tip_prompt)
            .setMessage(R.string.ds_blood_pressure_conflict_msg)
            .setPositiveButton(R.string.tip_i_know, null)
            .create()
    }
}