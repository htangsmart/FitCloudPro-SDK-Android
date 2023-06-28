package com.topstep.fitcloud.sample2

import android.content.res.Configuration
import androidx.multidex.MultiDexApplication
import com.github.kilnn.tool.system.SystemUtil
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.device.ConnectorState
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.worker.WeatherWorker
import kotlinx.coroutines.launch
import timber.log.Timber

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

    private var requireWeather = false

    private fun initMainProcess() {
        fitCloudSDKInit(this)
        val applicationScope = Injector.getApplicationScope()
        val deviceManager = Injector.getDeviceManager()
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
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        FormatterUtil.init(SystemUtil.getSystemLocal(this))
    }

}
