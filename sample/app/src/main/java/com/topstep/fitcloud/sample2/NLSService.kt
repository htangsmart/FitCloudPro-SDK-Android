package com.topstep.fitcloud.sample2

import android.content.Context
import android.service.notification.StatusBarNotification
import com.topstep.fitcloud.sdk.v2.FcSDK
import com.topstep.fitcloud.sdk.v2.utils.notification.AbsNotificationListenerService

class NLSService : AbsNotificationListenerService() {

    override fun getFcSDK(context: Context): FcSDK {
        return context.fcSDK
    }

    override fun getNotificationType(context: Context, sbn: StatusBarNotification): Int? {
        return null
    }

}