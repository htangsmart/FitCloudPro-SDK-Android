package com.topstep.fitcloud.sample2.ui.device.config

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentNotificationConfigBinding
import com.topstep.fitcloud.sample2.databinding.ItemNotificationConfigBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.PermissionHelper
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.launchWithLog
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.apis.ability.base.FcNotificationAbility
import com.topstep.fitcloud.sdk.v2.model.config.FcNotificationConfig
import com.topstep.fitcloud.sdk.v2.model.config.toBuilder
import com.topstep.fitcloud.sdk.v2.model.message.FcAppType
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
class NotificationConfigFragment : BaseFragment(R.layout.fragment_notification_config) {

    private val viewBind: FragmentNotificationConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var adapter: NotificationConfigAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = deviceManager.configFeature.getNotificationConfig()
        val items = deviceManager.fcSDK.notificationAbility.getSupportItems(getAllItemsName(requireContext()))
        adapter = NotificationConfigAdapter(requireContext(), items, config)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.btnSend.clickTrigger {
            showItemsDialog()
        }

        viewBind.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        viewBind.recyclerView.adapter = adapter
        adapter.listener = listener

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                    adapter.isConnected = it
                    adapter.isNLSEnabled = NotificationListenerServiceUtil.isEnabled(requireContext())
                    adapter.notifyDataSetChanged()
                }
            }
            launch {
                deviceManager.configFeature.observerNotificationConfig().asFlow().collect {
                    if (adapter.config != it) {
                        adapter.config = it
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun showItemsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setItems(getAllItemsName(requireContext())) { _, which ->
                sendAppMockMessage(which)
            }
            .show()
    }

    private fun sendAppMockMessage(flag: Int) {
        val type = when (flag) {
            FcNotificationConfig.Flag.TELEPHONY -> {
                promptToast.showInfo("Please use real telephony feature to test")
                return
            }
            FcNotificationConfig.Flag.SMS -> FcAppType.SMS
            FcNotificationConfig.Flag.QQ -> FcAppType.QQ
            FcNotificationConfig.Flag.WECHAT -> FcAppType.WECHAT
            FcNotificationConfig.Flag.FACEBOOK -> FcAppType.FACEBOOK
            FcNotificationConfig.Flag.TWITTER -> FcAppType.TWITTER
            FcNotificationConfig.Flag.LINKEDIN -> FcAppType.LINKEDIN
            FcNotificationConfig.Flag.INSTAGRAM -> FcAppType.INSTAGRAM
            FcNotificationConfig.Flag.PINTEREST -> FcAppType.PINTEREST
            FcNotificationConfig.Flag.WHATSAPP -> FcAppType.WHATSAPP
            FcNotificationConfig.Flag.LINE -> FcAppType.LINE
            FcNotificationConfig.Flag.FACEBOOK_MESSENGER -> FcAppType.FACEBOOK_MESSENGER
            FcNotificationConfig.Flag.KAKAO -> FcAppType.KAKAO
            FcNotificationConfig.Flag.SKYPE -> FcAppType.SKYPE
            FcNotificationConfig.Flag.EMAIL -> FcAppType.EMAIL
            FcNotificationConfig.Flag.TELEGRAM -> FcAppType.TELEGRAM
            FcNotificationConfig.Flag.VIBER -> FcAppType.VIBER
            FcNotificationConfig.Flag.CALENDAR -> FcAppType.CALENDAR
            FcNotificationConfig.Flag.SNAPCHAT -> FcAppType.SNAPCHAT
            FcNotificationConfig.Flag.HIKE -> FcAppType.HIKE
            FcNotificationConfig.Flag.YOUTUBE -> FcAppType.YOUTUBE
            FcNotificationConfig.Flag.APPLE_MUSIC -> FcAppType.APPLE_MUSIC
            FcNotificationConfig.Flag.ZOOM -> FcAppType.ZOOM
            FcNotificationConfig.Flag.TIKTOK -> FcAppType.TIKTOK
            FcNotificationConfig.Flag.GMAIL -> FcAppType.GMAIL
            FcNotificationConfig.Flag.OUTLOOK -> FcAppType.OUTLOOK
            FcNotificationConfig.Flag.WHATSAPP_BUSINESS -> FcAppType.WHATSAPP_BUSINESS
            FcNotificationConfig.Flag.FASTRACK -> FcAppType.FASTRACK
            FcNotificationConfig.Flag.TITAN -> FcAppType.TITAN
            FcNotificationConfig.Flag.GPAY -> FcAppType.GPAY
            FcNotificationConfig.Flag.AMAZON -> FcAppType.AMAZON
            FcNotificationConfig.Flag.PHONE_PE -> FcAppType.PHONE_PE
            FcNotificationConfig.Flag.HINGE -> FcAppType.HINGE
            FcNotificationConfig.Flag.FLIPKART -> FcAppType.FLIPKART
            FcNotificationConfig.Flag.MY_NTRA -> FcAppType.MY_NTRA
            FcNotificationConfig.Flag.MEESHO -> FcAppType.MEESHO
            FcNotificationConfig.Flag.ZIVAME -> FcAppType.ZIVAME
            FcNotificationConfig.Flag.AJIO -> FcAppType.AJIO
            FcNotificationConfig.Flag.URBANIC -> FcAppType.URBANIC
            FcNotificationConfig.Flag.NYKAA -> FcAppType.NYKAA
            FcNotificationConfig.Flag.HEALTHIFY_ME -> FcAppType.HEALTHIFY_ME
            FcNotificationConfig.Flag.CULT_FIT -> FcAppType.CULT_FIT
            FcNotificationConfig.Flag.FLO -> FcAppType.FLO
            FcNotificationConfig.Flag.BUMBLE -> FcAppType.BUMBLE
            FcNotificationConfig.Flag.UBER -> FcAppType.UBER
            FcNotificationConfig.Flag.SWIGGY -> FcAppType.SWIGGY
            FcNotificationConfig.Flag.ZOMATO -> FcAppType.ZOMATO
            else -> FcAppType.OTHERS_APP
        }

        deviceManager.fcSDK.notificationAbility.sendAppNotification(
            type = type,
            title = "Title for type$type",
            content = "This is a test message content!!!",
            tickerText = null,
        ).onErrorComplete().subscribe()
    }

    private val listener = object : NotificationConfigAdapter.Listener {

        private fun applyConfig(flag: Int, isChecked: Boolean) {
            val config = adapter.config.toBuilder().setFlagEnabled(flag, isChecked).create()
            applicationScope.launchWithLog {
                deviceManager.configFeature.setNotificationConfig(config).await()
            }
            adapter.config = config
        }

        override fun onItemCheckedChange(item: NotificationNameFlag, buttonView: CompoundButton, isChecked: Boolean) {
            when (item.flag) {
                FcNotificationConfig.Flag.TELEPHONY -> {
                    if (isChecked) {
                        PermissionHelper.requestTelephony(this@NotificationConfigFragment) {
                            if (PermissionHelper.hasReceiveTelephony(requireContext())) {
                                applyConfig(FcNotificationConfig.Flag.TELEPHONY, true)
                            } else {
                                buttonView.isChecked = false
                            }
                        }
                    } else {
                        applyConfig(FcNotificationConfig.Flag.TELEPHONY, false)
                    }
                }
                FcNotificationConfig.Flag.SMS -> {
                    if (isChecked) {
                        PermissionHelper.requestSms(this@NotificationConfigFragment) {
                            if (PermissionHelper.hasReceiveSms(requireContext())) {
                                applyConfig(FcNotificationConfig.Flag.SMS, true)
                            } else {
                                buttonView.isChecked = false
                            }
                        }
                    } else {
                        applyConfig(FcNotificationConfig.Flag.SMS, false)
                    }
                }
                else -> {
                    if (NotificationListenerServiceUtil.isEnabled(requireContext())) {
                        applyConfig(item.flag, isChecked)
                    } else {
                        CaptureNotificationDialogFragment().show(childFragmentManager, null)
                        buttonView.isChecked = false
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.listener = null
    }

    companion object {
        private fun getAllItemsName(context: Context): Array<String> {
            //All items name match of FcNotificationConfig.Flag
            return arrayOf(
                context.getString(R.string.ds_notification_telephony),
                context.getString(R.string.ds_notification_sms),
                context.getString(R.string.ds_notification_qq),
                context.getString(R.string.ds_notification_wechat),
                context.getString(R.string.ds_notification_facebook),
                context.getString(R.string.ds_notification_twitter),
                "LinkedIn",
                context.getString(R.string.ds_notification_instagram),
                "Pinterest",
                context.getString(R.string.ds_notification_whatsapp),
                context.getString(R.string.ds_notification_line),
                "Facebook Messager",
                context.getString(R.string.ds_notification_kakao_talk),
                context.getString(R.string.ds_notification_skype),
                context.getString(R.string.ds_notification_email),
                "Telegram",
                "Viber",
                "Calendar",
                "Snapchat",
                "Hike",
                "Youtube",
                "Apple Music",
                "Zoom",
                "TikTok",
                "Gmail",
                "Outlook",
                "Whatsapp Business",
                "Fastrack",
                "Titan",
                "GPay",
                "Amazon",
                "Other Apps",
                "PhonePe",
                "Hinge",
                "Flipkart",
                "MyNTRA",
                "Meesho",
                "Zivame",
                "Ajio",
                "Urbanic",
                "Nykaa",
                "Healthify Me",
                "CultFit",
                "Flo",
                "Bumble",
                "Uber",
                "Swiggy",
                "Zomato",
            )
        }

        private fun FcNotificationAbility.getSupportItems(names: Array<String>): MutableList<NotificationNameFlag> {
            val supports = ArrayList<NotificationNameFlag>()
            for (i in names.indices) {
                //Index equals FcNotificationConfig.Flag
                if (isSupportNotification(i)) {
                    supports.add(
                        NotificationNameFlag(names[i], i)
                    )
                }
            }
            return supports
        }
    }

}

data class NotificationNameFlag(
    val name: String,
    @FcNotificationConfig.Flag val flag: Int,
)

class NotificationConfigAdapter(
    private val context: Context,
    private val sources: MutableList<NotificationNameFlag>,
    var config: FcNotificationConfig,
) : RecyclerView.Adapter<NotificationConfigAdapter.InnerViewHolder>() {

    var isConnected = false
    var isNLSEnabled = false
    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        return InnerViewHolder(
            ItemNotificationConfigBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        val item = sources[position]
        val isFlagEnabled = config.isFlagEnabled(item.flag)

        val titleView = holder.viewBind.item.getTitleView()
        val switchView = holder.viewBind.item.getSwitchView()
        titleView.text = item.name

        when (item.flag) {
            FcNotificationConfig.Flag.TELEPHONY -> {
                switchView.isChecked = PermissionHelper.hasReceiveTelephony(context) && isFlagEnabled
            }
            FcNotificationConfig.Flag.SMS -> {
                switchView.isChecked = PermissionHelper.hasReceiveSms(context) && isFlagEnabled
            }
            else -> {
                switchView.isChecked = isNLSEnabled && isFlagEnabled
            }
        }
        switchView.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                listener?.onItemCheckedChange(item, buttonView, isChecked)
            }
        }
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    interface Listener {
        fun onItemCheckedChange(item: NotificationNameFlag, buttonView: CompoundButton, isChecked: Boolean)
    }

    class InnerViewHolder(
        val viewBind: ItemNotificationConfigBinding,
    ) : RecyclerView.ViewHolder(viewBind.root)

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