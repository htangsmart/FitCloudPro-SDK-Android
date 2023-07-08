package com.topstep.fitcloud.sample2

import android.content.Context
import android.service.notification.StatusBarNotification
import com.topstep.fitcloud.sample2.ui.device.config.NotificationConfigFragment
import com.topstep.fitcloud.sdk.v2.FcSDK
import com.topstep.fitcloud.sdk.v2.model.config.FcNotificationConfig
import com.topstep.fitcloud.sdk.v2.model.message.FcNotificationType
import com.topstep.fitcloud.sdk.v2.utils.notification.AbsNotificationListenerService
import timber.log.Timber

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/07.Notification-and-Message#third-party-app-notification
 *
 * ***Description**
 * Third-app notification function
 *
 * **Usage**
 * 1.[NotificationConfigFragment]
 * Turn on/off third-app flags
 *
 * Turn on NotificationListenerService
 *
 * 2. AndroidManifest.xml
 * Register [NLSService]
 */
class NLSService : AbsNotificationListenerService() {

    private val configs = HashMap<String, PackageConfig>()

    override fun getFcSDK(context: Context): FcSDK {
        return context.fcSDK
    }

    override fun getNotificationType(context: Context, sbn: StatusBarNotification): Int? {
        val packageName = sbn.packageName
        val config = configs[packageName]

        val fcSDK = getFcSDK(this)
        val notificationConfig = fcSDK.connector.configFeature().getNotificationConfig()

        if (config != null) {
            if (!notificationConfig.isFlagEnabled(config.flag)) {
                Timber.tag(TAG).i("flag ${config.flag} disabled")
                return null
            }
        } else {
            if (!notificationConfig.isFlagEnabled(FcNotificationConfig.Flag.OTHERS_APP)) {
                Timber.tag(TAG).i("flag ${FcNotificationConfig.Flag.OTHERS_APP} disabled")
                return null
            }
        }

        return config?.type ?: FcNotificationType.OTHERS_APP
    }

    override fun onCreate() {
        super.onCreate()
        configs[PACKAGE_QQ] = PackageConfig(FcNotificationConfig.Flag.QQ, FcNotificationType.QQ)
        configs[PACKAGE_WECHAT] = PackageConfig(FcNotificationConfig.Flag.WECHAT, FcNotificationType.WECHAT)
        configs[PACKAGE_FACEBOOK] = PackageConfig(FcNotificationConfig.Flag.FACEBOOK, FcNotificationType.FACEBOOK)
        configs[PACKAGE_TWITTER] = PackageConfig(FcNotificationConfig.Flag.TWITTER, FcNotificationType.TWITTER)

        configs[PACKAGE_INSTAGRAM] = PackageConfig(FcNotificationConfig.Flag.INSTAGRAM, FcNotificationType.INSTAGRAM)

        configs[PACKAGE_WHATS_APP] = PackageConfig(FcNotificationConfig.Flag.WHATSAPP, FcNotificationType.WHATSAPP)
        configs[PACKAGE_LINE] = PackageConfig(FcNotificationConfig.Flag.LINE, FcNotificationType.LINE)
        configs[PACKAGE_FACEBOOK_MESSENGER] = PackageConfig(FcNotificationConfig.Flag.FACEBOOK_MESSENGER, FcNotificationType.FACEBOOK_MESSENGER)
        configs[PACKAGE_KAKAO] = PackageConfig(FcNotificationConfig.Flag.KAKAO, FcNotificationType.KAKAO)
        val skypeConfig = PackageConfig(FcNotificationConfig.Flag.SKYPE, FcNotificationType.SKYPE)
        configs[PACKAGE_SKYPE] = skypeConfig
        configs[PACKAGE_SKYPE_EXTRA] = skypeConfig
        val emailConfig = PackageConfig(FcNotificationConfig.Flag.EMAIL, FcNotificationType.EMAIL)
        getAppSupportEmails()?.forEach {
            configs[it] = emailConfig
        }
    }

    private fun getAppSupportEmails(): MutableSet<String>? {
        return mutableSetOf(
            PACKAGE_EMAIL_1,
            PACKAGE_EMAIL_2,
            PACKAGE_EMAIL_3,
            PACKAGE_EMAIL_4,
            PACKAGE_EMAIL_5,
            PACKAGE_EMAIL_6,
            PACKAGE_EMAIL_7,
            PACKAGE_EMAIL_8,
            PACKAGE_EMAIL_9,
            PACKAGE_EMAIL_10,
            PACKAGE_EMAIL_11,
            PACKAGE_EMAIL_12,
            PACKAGE_EMAIL_13,
            PACKAGE_EMAIL_14,
            PACKAGE_EMAIL_15,
            PACKAGE_EMAIL_16,
            PACKAGE_EMAIL_17,
            PACKAGE_EMAIL_18,
            PACKAGE_EMAIL_19,
        )
    }

    private data class PackageConfig(
        @FcNotificationConfig.Flag val flag: Int,
        @FcNotificationType val type: Int,
    )
}