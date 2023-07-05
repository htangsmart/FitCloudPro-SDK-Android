package com.topstep.fitcloud.sample2.ui.camera

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.topstep.fitcloud.sample2.BuildConfig
import com.topstep.fitcloud.sample2.MyApplication
import com.topstep.fitcloud.sample2.databinding.ActivityCameraBinding
import com.topstep.fitcloud.sample2.ui.base.BaseActivity
import com.topstep.fitcloud.sdk.v2.features.FcMessageFeature
import com.topstep.fitcloud.sdk.v2.model.message.FcMessageType
import timber.log.Timber


/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/07.Notification-and-Message#camera-control
 *
 * ***Description**
 * Implement camera control function
 *
 * **Usage**
 * 1. [MyApplication]
 * Observer [FcMessageType.CAMERA_WAKE_UP] message, wake up camera activity
 *
 * 2. [PermissionFragment]
 * Request camera permission
 *
 * 3.[CameraFragment]
 * Observer [FcMessageType.CAMERA_EXIT] message, exit the camera activity
 * Observer [FcMessageType.CAMERA_TAKE_PHOTO] message, take photo
 * [FcMessageFeature.setCameraStatus] in onPause and onResume
 */
class CameraActivity : BaseActivity() {

    private lateinit var viewBind: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBind = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBind.root)
        sendBroadcast(Intent(ACTION_CAMERA_LAUNCH))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        sendBroadcast(Intent(ACTION_CAMERA_LAUNCH))
    }

    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        viewBind.fragmentContainer.postDelayed({
            hideSystemUI()
        }, 500L)
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, viewBind.fragmentContainer).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    companion object {
        const val ACTION_CAMERA_LAUNCH = BuildConfig.APPLICATION_ID + ".action.CameraLaunch"

        fun start(context: Context, newTask: Boolean = false) {
            val intent = Intent(context, CameraActivity::class.java)
            if (newTask) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
    }
}