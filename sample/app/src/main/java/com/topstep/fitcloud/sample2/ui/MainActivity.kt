package com.topstep.fitcloud.sample2.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.navigation.NavHostController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseActivity
import com.topstep.fitcloud.sample2.utils.findNavControllerInNavHost
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import kotlinx.coroutines.rx3.collect
import timber.log.Timber

class MainActivity : BaseActivity() {

    private lateinit var navController: NavHostController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var bottomNavigationView: BottomNavigationView
    private val deviceManager = Injector.getDeviceManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController = findNavControllerInNavHost(R.id.nav_host)
        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.deviceFragment, R.id.syncFragment, R.id.realtimeFragment, R.id.combineFragment
        ).build()
        bottomNavigationView = findViewById(R.id.bottom_nav_view)
        bottomNavigationView.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val id = destination.id
            val shouldVisible = id == R.id.deviceFragment || id == R.id.syncFragment || id == R.id.realtimeFragment || id == R.id.combineFragment
            bottomNavigationView.isVisible = shouldVisible
        }
        lifecycle.launchRepeatOnStarted {
            deviceManager.settingsFeature.observerGpsHotStartUpdateState().collect {
                Timber.w("observerGpsHotStartUpdateState:%d", it)
                if (it >= 0) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - checkGpsHotStartTime in 0..1000) {
                        return@collect
                    }
                    checkGpsHotStartTime = currentTime
                    showGpsHotStartUpdateDialogFragment()
                }
            }
        }
    }

    private var checkGpsHotStartTime = 0L

    private fun showGpsHotStartUpdateDialogFragment() {
        val tag = "GpsHotStart"
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment?.isAdded == true) {
            return
        }
        GpsHotStartUpdateDialogFragment().showNow(supportFragmentManager, tag)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}