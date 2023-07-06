package com.topstep.fitcloud.sample2.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.topstep.fitcloud.sample2.R

object NotificationHelper {

    private const val CORE_CHANNEL_ID = "Core"
    private const val DEVICE_CHANNEL_ID = "Device"
    private const val SPORT_CHANNEL_ID = "Sport"

    const val FIND_PHONE_NOTIFICATION_ID = 10001
    const val DEVICE_SERVICE_NOTIFICATION_ID = 10002
    const val SPORT_SERVICE_NOTIFICATION_ID = 10003
    const val CAMERA_NOTIFICATION_ID = 10004

    fun createCoreChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CORE_CHANNEL_ID, context.getString(R.string.notification_channel_core_name), NotificationManager.IMPORTANCE_HIGH)
            channel.description = context.getString(R.string.notification_channel_core_des)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notificationCoreChannel(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CORE_CHANNEL_ID)
    }

    fun createDeviceChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(DEVICE_CHANNEL_ID, context.getString(R.string.device_module), NotificationManager.IMPORTANCE_HIGH)
            channel.description = context.getString(R.string.notification_channel_device_des)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notificationDeviceChannel(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, DEVICE_CHANNEL_ID)
    }

    fun createSportChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(SPORT_CHANNEL_ID, context.getString(R.string.module_sport), NotificationManager.IMPORTANCE_HIGH)
            channel.description = context.getString(R.string.notification_channel_sport_des)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notificationSportChannel(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, SPORT_CHANNEL_ID)
    }
}