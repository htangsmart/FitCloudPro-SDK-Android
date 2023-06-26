package com.topstep.fitcloud.sample2.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.topstep.fitcloud.sample2.DeviceService
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.auth.AuthActivity
import com.topstep.fitcloud.sample2.ui.base.BaseActivity

/**
 * Splash screen.
 * According to whether there are currently has authed user, choose to enter the [MainActivity] or the [AuthActivity]
 */
class LaunchActivity : BaseActivity() {

    private val viewModel by viewModels<LaunchViewMode>()

    private var launchNavigation: LaunchNavigation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, DeviceService::class.java))
        installSplashScreen()
        lifecycleScope.launchWhenStarted {
            launchNavigation = viewModel.getLaunchNavigation()
            when (launchNavigation) {
                LaunchNavigation.NavToSignIn -> {
                    AuthActivity.start(this@LaunchActivity)
                }
                LaunchNavigation.NavToMain -> {
                    MainActivity.start(this@LaunchActivity)
                }
                is LaunchNavigation.NavToSport -> {
                    TODO()
                }
                else -> {
                    throw IllegalStateException()
                }
            }
            finish()
        }
    }

}

sealed class LaunchNavigation {
    object NavToSignIn : LaunchNavigation()

    object NavToMain : LaunchNavigation()

    class NavToSport(val sportType: Int) : LaunchNavigation()
}

class LaunchViewMode : ViewModel() {
    suspend fun getLaunchNavigation(): LaunchNavigation {
        return if (Injector.getAuthManager().hasAuthedUser()) {
            LaunchNavigation.NavToMain
        } else {
            LaunchNavigation.NavToSignIn
        }
    }
}