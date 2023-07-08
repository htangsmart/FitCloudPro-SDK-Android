package com.topstep.fitcloud.sample2

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.github.kilnn.tool.system.SystemUtil
import com.topstep.fitcloud.sample2.data.device.DeviceManager
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.device.ConnectorState
import com.topstep.fitcloud.sample2.ui.camera.CameraActivity
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.utils.NotificationHelper
import com.topstep.fitcloud.sample2.worker.WeatherWorker
import com.topstep.fitcloud.sdk.v2.FcConnector
import com.topstep.fitcloud.sdk.v2.model.message.FcMessageType
import com.topstep.fitcloud.sdk.v2.utils.notification.PhoneStateListenerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import kotlin.coroutines.resume

class MyApplication : MultiDexApplication() {

    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initAllProcess()
        if (SystemUtil.getProcessName(this) == packageName) {
            initMainProcess()
        }
    }

    private fun initAllProcess() {
        FormatterUtil.init(SystemUtil.getSystemLocal(this))
    }

    private lateinit var applicationScope: CoroutineScope
    private lateinit var deviceManager: DeviceManager
    private lateinit var findPhoneManager: FindPhoneManager
    private var requireWeather = false

    var myTelephonyControl: MyTelephonyControl? = null

    private fun initMainProcess() {
        fitCloudSDKInit(this)
        applicationScope = Injector.getApplicationScope()
        deviceManager = Injector.getDeviceManager()
        findPhoneManager = FindPhoneManager(this, applicationScope, deviceManager)

        myTelephonyControl = MyTelephonyControl(this, fcSDK.connector, object : PhoneStateListenerFactory<MyPhoneStateListener> {
            override fun createInstance(context: Context, connector: FcConnector): MyPhoneStateListener {
                return MyPhoneStateListener(context, connector)
            }
        })
        applicationScope.launch {
            deviceManager.flowWeatherRequire().collect {
                Timber.i("flowWeatherRequire:%b", it)
                requireWeather = it
                if (it) {
                    WeatherWorker.executePeriodic(this@MyApplication)
                    WeatherWorker.executeOnce(this@MyApplication)
                } else {
                    WeatherWorker.cancelAll(this@MyApplication)
                }
            }
        }
        applicationScope.launch {
            deviceManager.flowState.collect {
                if (it == ConnectorState.CONNECTED && requireWeather) {
                    WeatherWorker.executeOnce(this@MyApplication)
                }
            }
        }
        applicationScope.launch {
            deviceManager.messageFeature.observerMessage().asFlow().collect {
                Timber.i("receive msg:%d", it.type)
                when (it.type) {
                    FcMessageType.FIND_PHONE -> {
                        findPhoneManager.start()
                    }
                    FcMessageType.STOP_FIND_PHONE -> {
                        findPhoneManager.stop()
                    }
                    FcMessageType.CAMERA_WAKE_UP -> {
                        monitorCameraLaunch()
                        CameraActivity.start(this@MyApplication, true)
                    }
                }
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                myTelephonyControl?.checkInitialize()
            }

            override fun onStop(owner: LifecycleOwner) {

            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        FormatterUtil.init(SystemUtil.getSystemLocal(this))
    }

    /**
     * Send a notification when the CameraActivity launched failed on background.
     * Such as no SYSTEM_ALERT_WINDOW permission.
     */
    private fun monitorCameraLaunch() {
        val isForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (isForeground) return
        applicationScope.launch {
            val launchSuccess = try {
                withTimeout(750) { waitCameraLaunch() }
            } catch (e: Exception) {
                false
            }
            if (!launchSuccess) {
                sendCameraNotification()
            }
        }
    }

    private suspend fun waitCameraLaunch() = suspendCancellableCoroutine {
        val context = instance
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == CameraActivity.ACTION_CAMERA_LAUNCH) {
                    context.unregisterReceiver(this)
                    it.resume(true)
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(CameraActivity.ACTION_CAMERA_LAUNCH))
        it.invokeOnCancellation {
            context.unregisterReceiver(receiver)
        }
    }

    private fun sendCameraNotification() {
        Timber.w("send camera notification")
        val context = instance
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationHelper.createCoreChannel(context, notificationManager)

        val intent = Intent(context, CameraActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val deviceName = deviceManager.flowDevice.value?.name
        val builder = NotificationHelper.notificationCoreChannel(context)
            .setContentTitle(deviceName ?: context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.action_take_photo))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notificationManager.notify(NotificationHelper.CAMERA_NOTIFICATION_ID, builder.build())
    }
}
