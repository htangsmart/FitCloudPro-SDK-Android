package com.topstep.fitcloud.sample2.ui.device.config

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentHrvConfigBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.dialog.*
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcHRVConfig
import com.topstep.fitcloud.sdk.v2.model.config.toBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import timber.log.Timber

/**
 * Display and modify [FcHRVConfig].
 */
class HRVConfigFragment : BaseFragment(R.layout.fragment_hrv_config), CompoundButton.OnCheckedChangeListener,
    TimePickerDialogFragment.Listener, SelectIntDialogFragment.Listener {

    private val viewBind: FragmentHrvConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private var config: FcHRVConfig? = null

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
                if (config == null) {
                    loadConfig()
                }
            }
        }

        viewBind.itemIsEnabled.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemRemindEnabled.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemStartTime.clickTrigger(block = blockClick)
        viewBind.itemEndTime.clickTrigger(block = blockClick)
        viewBind.itemIntervalTime.clickTrigger(block = blockClick)
    }

    private suspend fun loadConfig() {
        try {
            val loaded = deviceManager.configFeature.getHRVConfig().await()
            config = loaded
            Timber.tag(TAG).i("getHRVConfig success: %s", formatHrvConfig(loaded))
            updateUI()
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "getHRVConfig failed")
            promptToast.showFailed(e)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (!buttonView.isPressed) return
        val current = config ?: return
        when (buttonView) {
            viewBind.itemIsEnabled.getSwitchView() -> {
                current.toBuilder().setEnabled(isChecked).create().saveConfig()
            }
            viewBind.itemRemindEnabled.getSwitchView() -> {
                current.toBuilder().setRemindEnabled(isChecked).create().saveConfig()
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        val current = config
        if (current != null) {
            when (view) {
                viewBind.itemStartTime -> showStartTimeDialog(current.getStart())
                viewBind.itemEndTime -> showEndTimeDialog(current.getEnd())
                viewBind.itemIntervalTime -> showIntervalDialog(current.getInterval(), 5, 720)
            }
        }
    }

    override fun onDialogTimePicker(tag: String?, timeMinute: Int) {
        val current = config ?: return
        when (tag) {
            DIALOG_START_TIME -> current.toBuilder().setStart(timeMinute).create().saveConfig()
            DIALOG_END_TIME -> current.toBuilder().setEnd(timeMinute).create().saveConfig()
        }
    }

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        val current = config ?: return
        if (DIALOG_INTERVAL_TIME == tag) {
            current.toBuilder().setInterval(selectValue).create().saveConfig()
        }
    }

    private fun FcHRVConfig.saveConfig() {
        applicationScope.launchWithLog {
            deviceManager.configFeature.setHRVConfig(this@saveConfig).await()
            Timber.tag(TAG).i("setHRVConfig success: %s", formatHrvConfig(this@saveConfig))
        }
        config = this
        updateUI()
    }

    private fun updateUI() {
        val current = config ?: return
        val isLayoutEnabled = viewBind.layoutContent.isEnabled

        viewBind.itemIsEnabled.getSwitchView().isChecked = current.isEnabled()
        viewBind.itemRemindEnabled.getSwitchView().isChecked = current.isRemindEnabled()
        if (isLayoutEnabled) {
            viewBind.layoutDetail.setAllChildEnabled(current.isEnabled())
        }

        viewBind.itemStartTime.getTextView().text = FormatterUtil.minute2Hmm(current.getStart())
        viewBind.itemEndTime.getTextView().text = FormatterUtil.minute2Hmm(current.getEnd())
        viewBind.itemIntervalTime.getTextView().text = getString(R.string.unit_minute_param, current.getInterval())
    }

    private fun formatHrvConfig(config: FcHRVConfig): String {
        return "enabled=${config.isEnabled()}, remind=${config.isRemindEnabled()}, " +
                "interval=${config.getInterval()}min, start=${config.getStart()}min, end=${config.getEnd()}min"
    }

    companion object {
        private const val TAG = "HRVConfigFragment"
    }
}
