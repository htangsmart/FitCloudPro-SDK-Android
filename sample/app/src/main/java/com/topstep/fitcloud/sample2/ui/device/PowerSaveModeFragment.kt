package com.topstep.fitcloud.sample2.ui.device

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.dialog.prompt.PromptDialogFragment
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentPowerSaveModeBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.DeviceFragment
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.dialog.*
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.settings.FcPowerSaveMode
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import timber.log.Timber

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/10.Other-Features#power-save-mode
 *
 * ***Description**
 * Display and modify the power save mode
 *
 * **Usage**
 * 1. [DeviceFragment]
 * According to whether [FcDeviceInfo.Feature.POWER_SAVE_MODE] supports, show or hide the entrance
 *
 * 2.[PowerSaveModeFragment]
 * Display and modify
 *
 */
class PowerSaveModeFragment : BaseFragment(R.layout.fragment_power_save_mode),
    CompoundButton.OnCheckedChangeListener, TimePickerDialogFragment.Listener, PromptDialogFragment.OnPromptListener {

    private val deviceManager = Injector.getDeviceManager()
    private val viewBind: FragmentPowerSaveModeBinding by viewBinding()
    private val viewModel: PowerSaveModeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.requestPowerSaveMode()
        }
        viewBind.itemStartTime.clickTrigger(block = blockClick)
        viewBind.itemEndTime.clickTrigger(block = blockClick)
        viewBind.btnSave.clickTrigger(block = blockClick)
        viewBind.itemIsEnabled.getSwitchView().setOnCheckedChangeListener(this)
        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    when (val async = it.asyncRequest) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }
                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }
                        is Success -> {
                            viewBind.loadingView.visibility = View.GONE
                            updateUI(async())
                        }
                        else -> {}
                    }

                    if (it.asyncSet is Loading) {
                        promptProgress.showProgress(R.string.tip_please_wait)
                    } else {
                        promptProgress.dismiss()
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect {
                    when (it) {
                        is AsyncEvent.OnFail -> promptToast.showFailed(it.error)
                        is AsyncEvent.OnSuccess<*> -> {
                            if (it.property == State::asyncSet) {
                                promptToast.showSuccess(R.string.tip_save_success, intercept = true, promptId = 1)
                            }
                        }
                    }
                }
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemStartTime -> {
                showStartTimeDialog(viewModel.getMode().start)
            }
            viewBind.itemEndTime -> {
                showEndTimeDialog(viewModel.getMode().end)
            }
            viewBind.btnSave -> {
                viewModel.savePowerSaveMode()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView == null || !buttonView.isPressed) return
        viewModel.setIsEnabled(isChecked)
    }

    override fun onDialogTimePicker(tag: String?, timeMinute: Int) {
        if (DIALOG_START_TIME == tag) {
            viewModel.setStartTime(timeMinute)
        } else if (DIALOG_END_TIME == tag) {
            viewModel.setEndTime(timeMinute)
        }
    }

    private fun updateUI(mode: FcPowerSaveMode) {
        viewBind.itemIsEnabled.getSwitchView().isChecked = mode.isEnabled
        if (deviceManager.configFeature.getDeviceInfo().isSupportFeature(FcDeviceInfo.Feature.POWER_SAVE_PERIOD)) {
            viewBind.itemStartTime.visibility = View.VISIBLE
            viewBind.itemEndTime.visibility = View.VISIBLE
            viewBind.itemStartTime.getTextView().text = FormatterUtil.minute2Hmm(mode.start)
            viewBind.itemEndTime.getTextView().text = FormatterUtil.minute2Hmm(mode.end)
        } else {
            viewBind.itemStartTime.visibility = View.GONE
            viewBind.itemEndTime.visibility = View.GONE
        }
    }

    override fun onPromptCancel(promptId: Int, cancelReason: Int, tag: String?) {
        if (promptId == 1) {
            findNavController().popBackStack()
        }
    }

    data class State(
        val asyncRequest: Async<FcPowerSaveMode> = Uninitialized,
        val asyncSet: Async<Unit> = Uninitialized
    )

}

class PowerSaveModeViewModel : AsyncViewModel<PowerSaveModeFragment.State>(PowerSaveModeFragment.State()) {

    private val deviceManager = Injector.getDeviceManager()

    init {
        requestPowerSaveMode()
    }

    fun setStartTime(time: Int) {
        viewModelScope.launch {
            state.copy(asyncRequest = Success(getMode().copy(start = time))).newState()
        }
    }

    fun setEndTime(time: Int) {
        viewModelScope.launch {
            state.copy(asyncRequest = Success(getMode().copy(end = time))).newState()
        }
    }

    fun setIsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            state.copy(asyncRequest = Success(getMode().copy(isEnabled = isEnabled))).newState()
        }
    }

    fun savePowerSaveMode() {
        suspend {
            val mode = getMode()
            Timber.i("mode:%s", mode)
            deviceManager.settingsFeature.setPowerSaveMode(mode).await()
        }.execute(PowerSaveModeFragment.State::asyncSet) {
            copy(asyncSet = it)
        }
    }

    fun getMode(): FcPowerSaveMode {
        return state.asyncRequest() ?: FcPowerSaveMode(false, 0, 0)
    }

    fun requestPowerSaveMode() {
        suspend {
            deviceManager.settingsFeature.requestPowerSaveMode().await()
        }.execute(PowerSaveModeFragment.State::asyncRequest) {
            copy(asyncRequest = it)
        }
    }
}