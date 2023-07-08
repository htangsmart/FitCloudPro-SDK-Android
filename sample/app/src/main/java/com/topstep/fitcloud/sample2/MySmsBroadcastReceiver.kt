package com.topstep.fitcloud.sample2

import android.content.Context
import com.topstep.fitcloud.sample2.ui.device.config.NotificationConfigFragment
import com.topstep.fitcloud.sdk.v2.FcSDK
import com.topstep.fitcloud.sdk.v2.model.config.FcNotificationConfig
import com.topstep.fitcloud.sdk.v2.utils.notification.AbsSmsBroadcastReceiver

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/07.Notification-and-Message#sms
 *
 * ***Description**
 * SMS notification function
 *
 * **Usage**
 * 1.[NotificationConfigFragment]
 * Turn on/off [FcNotificationConfig.Flag.SMS]
 *
 * Request runtime permissions
 *
 * 2. AndroidManifest.xml
 * Declare permissions
 *
 * Register [MySmsBroadcastReceiver]
 */
class MySmsBroadcastReceiver : AbsSmsBroadcastReceiver() {

    override fun getFcSDK(context: Context): FcSDK {
        return context.fcSDK
    }

    override fun isSmsEnabled(context: Context): Boolean {
        return getFcSDK(context).connector.configFeature().getNotificationConfig().isFlagEnabled(FcNotificationConfig.Flag.SMS)
    }

}