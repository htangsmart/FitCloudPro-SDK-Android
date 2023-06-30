package com.topstep.fitcloud.sample2.ui.device.config

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDialogFragment
import com.github.kilnn.tool.widget.item.PreferenceItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentNotificationConfigBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.FcNotificationConfig
import com.topstep.fitcloud.sdk.v2.model.config.toBuilder
import com.topstep.fitcloud.sdk.v2.utils.notification.NotificationListenerServiceUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcnotificationconfig
 *
 * ***Description**
 * Display and modify the config of Notification
 *
 * **Usage**
 * 1.[NotificationConfigFragment]
 * Display and modify
 */
class NotificationConfigFragment : BaseFragment(R.layout.fragment_notification_config), CompoundButton.OnCheckedChangeListener {

    private val viewBind: FragmentNotificationConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var config: FcNotificationConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = deviceManager.configFeature.getNotificationConfig()
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
                deviceManager.configFeature.observerNotificationConfig().asFlow().collect {
                    if (config != it) {
                        config = it
                        updateUI()
                    }
                }
            }
        }

        viewBind.itemTelephony.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemSms.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemQq.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemWechat.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemFacebook.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemTwitter.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemInstagram.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemWhatsapp.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemLine.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemMessenger.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemKakaoTalk.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemSkype.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemEmail.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemOthers.getSwitchView().setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            when (buttonView) {
                viewBind.itemTelephony.getSwitchView() -> {
                    if (isChecked) {
                        PermissionHelper.requestTelephony(this) {
                            if (PermissionHelper.hasReceiveTelephony(requireContext())) {
                                changeFlags(true, FcNotificationConfig.Flag.TELEPHONY)
                            } else {
                                buttonView.isChecked = false
                            }
                        }
                    } else {
                        changeFlags(false, FcNotificationConfig.Flag.TELEPHONY)
                    }
                }
                viewBind.itemSms.getSwitchView() -> {
                    if (isChecked) {
                        PermissionHelper.requestSms(this) {
                            if (PermissionHelper.hasReceiveSms(requireContext())) {
                                changeFlags(true, FcNotificationConfig.Flag.SMS)
                            } else {
                                buttonView.isChecked = false
                            }
                        }
                    } else {
                        changeFlags(false, FcNotificationConfig.Flag.SMS)
                    }
                }
                viewBind.itemQq.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.QQ)
                    }
                }
                viewBind.itemWechat.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.WECHAT)
                    }
                }
                viewBind.itemFacebook.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.FACEBOOK)
                    }
                }
                viewBind.itemTwitter.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.TWITTER)
                    }
                }
                viewBind.itemInstagram.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.INSTAGRAM)
                    }
                }
                viewBind.itemWhatsapp.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.WHATSAPP)
                    }
                }
                viewBind.itemLine.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.LINE)
                    }
                }
                viewBind.itemMessenger.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.FACEBOOK_MESSENGER)
                    }
                }
                viewBind.itemKakaoTalk.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.KAKAO)
                    }
                }
                viewBind.itemSkype.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.SKYPE)
                    }
                }
                viewBind.itemEmail.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.EMAIL)
                    }
                }
                viewBind.itemOthers.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                        changeFlags(isChecked, FcNotificationConfig.Flag.OTHERS_APP)
                    }
                }
            }
        }
    }

    private fun FcNotificationConfig.saveConfig() {
        applicationScope.launchWithLog {
            deviceManager.configFeature.setNotificationConfig(this@saveConfig).await()
        }
        this@NotificationConfigFragment.config = this
        updateUI()
    }

    private fun changeFlags(isChecked: Boolean, flag: Int) {
        config.toBuilder().setFlagEnabled(flag, isChecked).create().saveConfig()
    }

    private fun checkNotificationPermission(compoundButton: CompoundButton): Boolean {
        if (NotificationListenerServiceUtil.isEnabled(requireContext())) return true
        CaptureNotificationDialogFragment().show(childFragmentManager, null)
        compoundButton.isChecked = false
        return false
    }

    private fun updateUI() {
        viewBind.itemTelephony.getSwitchView().isChecked = config.isFlagEnabled(FcNotificationConfig.Flag.TELEPHONY) && PermissionHelper.hasReceiveTelephony(requireContext())
        viewBind.itemSms.getSwitchView().isChecked = config.isFlagEnabled(FcNotificationConfig.Flag.SMS) && PermissionHelper.hasReceiveSms(requireContext())

        val isNLSEnabled = NotificationListenerServiceUtil.isEnabled(requireContext())
        val deviceInfo = deviceManager.configFeature.getDeviceInfo()

        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemQq, FcNotificationConfig.Flag.QQ)
        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemWechat, FcNotificationConfig.Flag.WECHAT)
        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemFacebook, FcNotificationConfig.Flag.FACEBOOK)
        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemTwitter, FcNotificationConfig.Flag.TWITTER)
        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemInstagram, FcNotificationConfig.Flag.INSTAGRAM)
        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemWhatsapp, FcNotificationConfig.Flag.WHATSAPP)
        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemLine, FcNotificationConfig.Flag.LINE)
        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemMessenger, FcNotificationConfig.Flag.FACEBOOK_MESSENGER)
        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemKakaoTalk, FcNotificationConfig.Flag.KAKAO)
        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemSkype, FcNotificationConfig.Flag.SKYPE)
        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemEmail, FcNotificationConfig.Flag.EMAIL)
        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemOthers, FcNotificationConfig.Flag.OTHERS_APP)
    }

    private fun updateThirdApp(deviceInfo: FcDeviceInfo, isNLSEnabled: Boolean, item: PreferenceItem, flag: Int) {
        if (deviceInfo.isSupportNotification(flag)) {
            item.visibility = View.VISIBLE
            item.getSwitchView().isChecked = isNLSEnabled && config.isFlagEnabled(flag)
        } else {
            item.visibility = View.GONE
        }
    }

}

class CaptureNotificationDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.tip_prompt)
            .setMessage(R.string.ds_capture_notice_msg)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                NotificationListenerServiceUtil.toSettings(requireContext())
            }
            .create()
    }
}