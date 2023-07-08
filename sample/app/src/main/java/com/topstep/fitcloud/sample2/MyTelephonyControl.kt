package com.topstep.fitcloud.sample2

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.topstep.fitcloud.sample2.ui.device.config.NotificationConfigFragment
import com.topstep.fitcloud.sample2.utils.PermissionHelper
import com.topstep.fitcloud.sdk.v2.FcConnector
import com.topstep.fitcloud.sdk.v2.model.config.FcNotificationConfig
import com.topstep.fitcloud.sdk.v2.utils.notification.AbsPhoneStateListener
import com.topstep.fitcloud.sdk.v2.utils.notification.AbsTelephonyControl
import com.topstep.fitcloud.sdk.v2.utils.notification.PhoneStateListenerFactory

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/07.Notification-and-Message#telephony
 *
 * ***Description**
 * Telephony notification function
 *
 * **Usage**
 * 1. [MyApplication]
 * Create and hold reference of [MyTelephonyControl]
 *
 * Check at [ProcessLifecycleOwner]
 *
 * 2.[PermissionHelper]
 * Check at [PermissionHelper.requestTelephony]
 *
 * 3.[NotificationConfigFragment]
 * Turn on/off [FcNotificationConfig.Flag.TELEPHONY]
 *
 * Request runtime permissions
 *
 * 4. AndroidManifest.xml
 * Declare permissions
 */
class MyTelephonyControl(
    context: Context,
    connector: FcConnector,
    factory: PhoneStateListenerFactory<MyPhoneStateListener>,
) : AbsTelephonyControl<MyPhoneStateListener>(context, connector, factory)

class MyPhoneStateListener(
    context: Context,
    connector: FcConnector,
) : AbsPhoneStateListener(context, connector) {

    override fun isTelephonyEnabled(context: Context): Boolean {
        return connector.configFeature().getNotificationConfig().isFlagEnabled(FcNotificationConfig.Flag.TELEPHONY)
    }

}