package com.topstep.fitcloud.sample2.ui.device.bind

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.DeviceManager
import com.topstep.fitcloud.sample2.databinding.DialogDeviceConnectBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.device.ConnectorState
import com.topstep.fitcloud.sample2.ui.base.AsyncEvent
import com.topstep.fitcloud.sample2.ui.base.AsyncViewModel
import com.topstep.fitcloud.sample2.ui.base.Loading
import com.topstep.fitcloud.sample2.ui.base.SingleAsyncState
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sdk.connector.FcDisconnectedReason
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceConnectDialogFragment : AppCompatDialogFragment() {

    interface Listener {
        fun navToConnectHelp()
        fun navToBgRunSettings()
    }

    private val promptToast by promptToast()
    private val promptProgress by promptProgress()

    private val deviceManager: DeviceManager = Injector.getDeviceManager()

    private var _viewBind: DialogDeviceConnectBinding? = null
    private val viewBind get() = _viewBind!!
    private val viewModel by viewModels<DeviceConnectViewMode>()

    private var timberJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenResumed {
            //Required permissions
            PermissionHelper.requestBleConnect(this@DeviceConnectDialogFragment) { granted ->
                if (!granted) {
                    deviceManager.cancelBind()
                    dismiss()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogDeviceConnectBinding.inflate(layoutInflater)

        lifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    if (it.async is Loading) {
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
                            //Unbind success and dismiss
                            dismiss()
                        }
                    }
                }
            }
            launch {
                deviceManager.flowDevice.collect {
                    if (it != null) {
                        viewBind.tvName.text = it.name
                        viewBind.tvAddress.text = it.address
                        isCancelable = !it.isTryingBind
                        if (it.isTryingBind) {
                            viewBind.btnUnbind.setText(R.string.device_cancel_bind)
                            viewBind.btnUnbind.setOnClickListener {
                                //Cancel bind and exit
                                deviceManager.cancelBind()
                                dismissAllowingStateLoss()
                            }
                        } else {
                            viewBind.btnUnbind.setText(R.string.device_unbind)
                            viewBind.btnUnbind.setOnClickListener {
                                lifecycleScope.launchWhenResumed {
                                    UnbindConfirmDialogFragment().showNow(childFragmentManager, null)
                                }
                            }
                        }
                    }
                }
            }
            launch {
                deviceManager.flowState.collect {
                    timberJob?.cancel()
                    when (it) {
                        ConnectorState.NO_DEVICE -> {
                            dismiss()
                        }
                        ConnectorState.BT_DISABLED -> {
                            viewBind.tvState.setText(R.string.device_state_disconnected)
                            viewBind.progressDotView.setFailed()
                            showBtDisabled()
                        }
                        ConnectorState.DISCONNECTED -> {
                            viewBind.tvState.setText(R.string.device_state_disconnected)
                            viewBind.progressDotView.setFailed()
                            showDisconnectedReason()
                        }
                        ConnectorState.PRE_CONNECTING -> {
                            timberJob = lifecycleScope.launch {
                                val seconds = deviceManager.getNextRetrySeconds()
                                viewBind.tvState.text = "${seconds}s"
                                if (seconds > 0) {
                                    repeat(seconds) { times ->
                                        delay(1000)
                                        viewBind.tvState.text = "${seconds - times - 1}s"
                                    }
                                }
                            }
                            viewBind.progressDotView.setFailed()
                            showConnectingTips()
                        }
                        ConnectorState.CONNECTING -> {
                            viewBind.tvState.setText(R.string.device_state_connecting)
                            viewBind.progressDotView.setLoading()
                            showConnectingTips()
                        }
                        ConnectorState.CONNECTED -> {
                            viewBind.tvState.setText(R.string.device_state_connected)
                            viewBind.progressDotView.setSuccess()
                            showBgRunSettings()
                        }
                    }
                }
            }
        }
        extraNormalColor = viewBind.tvExtraMsg.textColors
        extraErrorColor = MaterialColors.getColor(viewBind.root, com.google.android.material.R.attr.colorError)
        viewBind.tvUnableToConnect.setOnClickListener {
            (parentFragment as? Listener)?.navToConnectHelp()
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setView(viewBind.root)
            .create()
    }

    private lateinit var extraNormalColor: ColorStateList
    private var extraErrorColor: Int = 0

    private fun showBtDisabled() {
        viewBind.layoutConnecting.isVisible = false
        viewBind.layoutAction.isVisible = true

        viewBind.tvExtraMsg.setTextColor(extraErrorColor)
        viewBind.tvExtraMsg.setText(R.string.device_state_bt_disabled)
        viewBind.btnAction.setText(R.string.action_turn_on)
        viewBind.btnAction.setOnClickListener {
            requireContext().startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun showDisconnectedReason() {
        viewBind.layoutConnecting.isVisible = false
        viewBind.layoutAction.isVisible = true

        when (val reason = deviceManager.getDisconnectedReason()) {
            FcDisconnectedReason.INIT_STATE, FcDisconnectedReason.ACTIVE_CLOSE -> {
                /**
                 * Impossible because [ConnectorState.NO_DEVICE] state will emit before it
                 */
                viewBind.layoutAction.isVisible = false
                Timber.tag(TAG).w("Error reason:%s", reason)
            }
            FcDisconnectedReason.BT_DISABLED -> {
                /**
                 * Impossible because [ConnectorState.BT_DISABLED] state will emit before it
                 */
                viewBind.layoutAction.isVisible = false
                Timber.tag(TAG).w("Error reason:%s", reason)
            }
            FcDisconnectedReason.DISCONNECT_TEMPORARY, FcDisconnectedReason.DISCONNECT_DFU -> {
                viewBind.tvExtraMsg.setTextColor(extraErrorColor)
                viewBind.tvExtraMsg.setText(R.string.device_state_disconnected)
                viewBind.btnAction.setText(R.string.device_reconnect)
                viewBind.btnAction.setOnClickListener {
                    deviceManager.reconnect()
                }
            }
            FcDisconnectedReason.AUTH_LOGIN_FAILED, FcDisconnectedReason.AUTH_BIND_FAILED -> {
                viewBind.tvExtraMsg.setTextColor(extraErrorColor)
                if (reason == FcDisconnectedReason.AUTH_LOGIN_FAILED) {
                    viewBind.tvExtraMsg.setText(R.string.device_connect_auth_login_failed)
                } else {
                    viewBind.tvExtraMsg.setText(R.string.device_connect_auth_bind_failed)
                }
                viewBind.btnAction.setText(R.string.device_rebind)
                viewBind.btnAction.setOnClickListener {
                    deviceManager.rebind()
                }
            }
            FcDisconnectedReason.ERROR_UNKNOWN -> {
                /**
                 * Almost impossible
                 */
                viewBind.tvExtraMsg.setTextColor(extraErrorColor)
                viewBind.tvExtraMsg.text = "unknown error"
                viewBind.btnAction.text = null
                viewBind.btnAction.setOnClickListener(null)
                Timber.tag(TAG).w("Error reason:%s", reason)
            }
        }
    }

    private fun showConnectingTips() {
        viewBind.layoutConnecting.isVisible = true
        viewBind.layoutAction.isVisible = false
    }

    private fun showBgRunSettings() {
        viewBind.layoutConnecting.isVisible = false
        viewBind.layoutAction.isVisible = true

        viewBind.tvExtraMsg.setTextColor(extraNormalColor)
        viewBind.tvExtraMsg.setText(R.string.device_connect_bg_run_settings)
        viewBind.btnAction.setText(R.string.action_to_set)
        viewBind.btnAction.setOnClickListener {
            (parentFragment as? Listener)?.navToBgRunSettings()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

    override fun onStop() {
        super.onStop()
        timberJob?.cancel()
    }

    companion object {
        private const val TAG = "DeviceConnectDialog"
    }
}

class DeviceConnectViewMode : AsyncViewModel<SingleAsyncState<Unit>>(SingleAsyncState()) {

    private val deviceManager: DeviceManager = Injector.getDeviceManager()

    fun unbind() {
        suspend {
            deviceManager.unbind()
        }.execute(SingleAsyncState<Unit>::async)
        {
            copy(async = it)
        }
    }

}

class UnbindConfirmDialogFragment : AppCompatDialogFragment() {

    private val viewModel by viewModels<DeviceConnectViewMode>({ requireParentFragment() })

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.tip_prompt)
            .setMessage(R.string.device_unbind_confirm_msg)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.unbind()
            }
        return builder.create()
    }
}