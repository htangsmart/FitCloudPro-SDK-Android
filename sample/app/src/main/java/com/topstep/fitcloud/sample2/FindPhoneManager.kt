package com.topstep.fitcloud.sample2

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.topstep.fitcloud.sample2.data.device.DeviceManager
import com.topstep.fitcloud.sample2.utils.MediaPlayerHelper
import com.topstep.fitcloud.sample2.utils.NotificationHelper
import com.topstep.fitcloud.sdk.v2.features.FcMessageFeature
import com.topstep.fitcloud.sdk.v2.model.message.FcMessageType
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/07.Notification-and-Message#find-phone
 *
 * ***Description**
 * Implement find phone function
 *
 * **Usage**
 * 1. [MyApplication]
 * Observer [FcMessageType.FIND_PHONE] message, start find phone
 * Observer [FcMessageType.STOP_FIND_PHONE] message, stop find phone
 *
 * 2.[FindPhoneManager]
 * [FcMessageFeature.replayFindPhone] when app start responding
 * [FcMessageFeature.stopFindPhone] when app stop responding
 */
class FindPhoneManager constructor(
    val context: Context,
    val applicationScope: CoroutineScope,
    private val deviceManager: DeviceManager,
) {

    private var job: Job? = null
    private var mediaPlayerHelper: MediaPlayerHelper? = null
    private var restoreVolume: Int? = null

    fun start() {
        deviceManager.messageFeature.replayFindPhone().onErrorComplete().subscribe()
        if (job?.isActive == true) {
            Timber.tag(TAG).i("already find phone")
            return
        }
        Timber.tag(TAG).i("start find phone")
        job = applicationScope.launch {
            doStart()
            launch {
                suspendCancellableCoroutine {
                    //Registering multiple phone buttons and other broadcasts, which trigger, indicates that the phone is being manipulated by humans,
                    //indicating that the phone has been found. Therefore, stop searching at this time
                    val filter = IntentFilter()
                    filter.addAction(Intent.ACTION_SCREEN_ON)
                    filter.addAction(Intent.ACTION_SCREEN_OFF)
                    filter.addAction("android.media.VOLUME_CHANGED_ACTION") //AudioManager.VOLUME_CHANGED_ACTION
                    filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                    filter.addAction(ACTION_STOP_FIND_PHONE)
                    val broadcastReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            Timber.tag(TAG).i("onReceive:%s", intent?.action)
                            try {
                                it.cancel()
                            } catch (e: Exception) {
                                Timber.tag(TAG).w(e)
                            }
                        }
                    }
                    context.registerReceiver(broadcastReceiver, filter)
                    Timber.tag(TAG).i("registerReceiver")
                    it.invokeOnCancellation {
                        Timber.tag(TAG).i("unregisterReceiver")
                        context.unregisterReceiver(broadcastReceiver)
                    }
                }
            }.invokeOnCompletion {
                Timber.tag(TAG).i("cancel the receiver")
                cancel()
            }
            launch {
                delay(10 * 1000)
            }.invokeOnCompletion {
                Timber.tag(TAG).i("cancel the timer")
                cancel()
            }
        }.also {
            it.invokeOnCompletion {
                doStop()
                Timber.tag(TAG).i("find phone finished")
            }
        }
    }

    fun stop() {
        Timber.tag(TAG).i("stop find phone")
        job?.cancel()
        job = null
    }

    private fun doStart() {
        Timber.tag(TAG).i("doStart")

        //Sound
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        //Adjust the volume of playing audio
        val maxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume: Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (currentVolume < maxVolume - 2) {
            restoreVolume = currentVolume
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume - 2, 0)
        } else {
            restoreVolume = null
        }

        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
            audioManager.isSpeakerphoneOn = true
        } catch (e: Exception) {
            Timber.tag(TAG).w(e)
        }

        mediaPlayerHelper = MediaPlayerHelper().apply {
            startPlay(context, "find_phone.mp3")
        }

        //Vibration
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 300, 500, 300)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_ALARM) //key
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 2)
            vibrator.vibrate(effect, audioAttributes)
        } else {
            vibrator.vibrate(pattern, 2, audioAttributes)
        }

        //Notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationHelper.createCoreChannel(context, notificationManager)

        val intent = Intent(ACTION_STOP_FIND_PHONE)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val builder = NotificationHelper.notificationCoreChannel(context)
            .setContentTitle(context.getString(R.string.ds_find_phone_found))
            .setContentText(context.getString(R.string.ds_find_phone_stop))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(pendingIntent)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notificationManager.notify(NotificationHelper.FIND_PHONE_NOTIFICATION_ID, builder.build())
    }

    private fun doStop() {
        Timber.tag(TAG).i("doStop")
        //Sound
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        //Restore the original volume
        restoreVolume?.let {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it, 0)
        }
        restoreVolume = null

        mediaPlayerHelper?.stopPlay()
        mediaPlayerHelper = null

        //Cancel vibration
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()

        //Cancel Notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NotificationHelper.FIND_PHONE_NOTIFICATION_ID)

        deviceManager.messageFeature.stopFindPhone().onErrorComplete().subscribe()
    }

    companion object {
        private const val TAG = "FindPhone"
        private const val ACTION_STOP_FIND_PHONE = BuildConfig.APPLICATION_ID + ".action.StopFindPhone"
    }
}