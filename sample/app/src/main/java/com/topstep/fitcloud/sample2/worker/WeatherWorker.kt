package com.topstep.fitcloud.sample2.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.*
import com.github.kilnn.tool.system.SystemUtil
import com.topstep.fitcloud.sample2.MyApplication
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.device.config.FunctionConfigFragment
import com.topstep.fitcloud.sdk.v2.features.FcSettingsFeature
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.FcFunctionConfig
import com.topstep.fitcloud.sdk.v2.model.settings.FcWeatherException
import com.topstep.fitcloud.sdk.v2.model.settings.FcWeatherForecast
import com.topstep.fitcloud.sdk.v2.model.settings.FcWeatherToday
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/10.Other-Features#setting-weather
 *
 * ***Description**
 * 1. [FcSettingsFeature.setWeatherException] only support when [FcDeviceInfo.Feature.SETTING_WEATHER_DISPLAY] is supported.
 * But usually there is no need to judge it, just handle the error situation well.
 *
 * 2. [FcFunctionConfig.Flag.WEATHER_DISPLAY] only take effect when [FcDeviceInfo.Feature.SETTING_WEATHER_DISPLAY] is supported.
 * Usually, there is no need to judge it, just use it directly because the weather page on the device does not change,
 * but [FcFunctionConfig] will save this flag.
 *
 * 3. [FcWeatherForecast] only support when [FcDeviceInfo.Feature.WEATHER_FORECAST] is supported.
 * You can ignore it, but the weather forecast will not be displayed on the device if it not support.
 *
 * **Usage**
 * 1. [MyApplication]
 * Does the monitoring device require weather function? If so, execute the weather worker.
 *
 * 2. [WeatherWorker]
 * Execute weather request and send it to device
 *
 * 3. [FunctionConfigFragment]
 * Change weather display on device
 */
class WeatherWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    val deviceManager = Injector.getDeviceManager()

    override suspend fun doWork(): Result {
        Timber.tag(TAG).i("doWork")
        //General weather function depends on location function

        val hasPermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            //The device may not support setWeatherException, so needs to use onErrorComplete to handle the failure
            deviceManager.settingsFeature.setWeatherException(FcWeatherException.LOCATION_NO_PERMISSION).onErrorComplete().subscribe()
            Timber.tag(TAG).i("location permission miss")
            return Result.success()
        }
        if (!SystemUtil.isLocationEnabled(applicationContext)) {
            //The device may not support setWeatherException, so needs to use onErrorComplete to handle the failure
            deviceManager.settingsFeature.setWeatherException(FcWeatherException.LOCATION_DISABLED).onErrorComplete().subscribe()
            Timber.tag(TAG).i("location service disabled")
            return Result.success()
        }

        // request location
        // Assuming the location request successful now

        // request weather
        // Assuming the weather request successful now

        //Send the weather to device
        //Use a mock weather
        val today = FcWeatherToday(
            lowTemperature = 23,
            highTemperature = 32,
            weatherCode = 0x01,
            currentTemperature = 27,
            pressure = 1000,//atmospheric pressure
            windForce = 3,//wind wind force
            visibility = 10,//visibility
        )

        val forecasts = listOf(
            FcWeatherForecast(
                lowTemperature = 22,
                highTemperature = 30,
                weatherCode = 0x02,
            ),
            FcWeatherForecast(
                lowTemperature = 22,
                highTemperature = 30,
                weatherCode = 0x02,
            ),
            FcWeatherForecast(
                lowTemperature = 22,
                highTemperature = 30,
                weatherCode = 0x02,
            ),
            FcWeatherForecast(
                lowTemperature = 22,
                highTemperature = 30,
                weatherCode = 0x02,
            ),
            FcWeatherForecast(
                lowTemperature = 22,
                highTemperature = 30,
                weatherCode = 0x02,
            ),
            FcWeatherForecast(
                lowTemperature = 22,
                highTemperature = 30,
                weatherCode = 0x02,
            ),
            FcWeatherForecast(
                lowTemperature = 22,
                highTemperature = 30,
                weatherCode = 0x02,
            ),
        )

        Timber.tag(TAG).i("set weather")

        deviceManager.settingsFeature.setWeather("ShenZhen", System.currentTimeMillis(), today, forecasts).onErrorComplete().subscribe()

        return Result.success()
    }

    companion object {
        private const val TAG = "WeatherWorker"
        private const val TAG_WEATHER_PERIODIC = "WeatherPeriodic"
        private const val TAG_WEATHER_ONCE = "WeatherOnce"

        fun cancelAll(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(TAG_WEATHER_PERIODIC)
            WorkManager.getInstance(context).cancelUniqueWork(TAG_WEATHER_ONCE)
        }

        fun executePeriodic(context: Context) {
            Timber.tag(TAG).i("executePeriodic")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build()
            val builder = PeriodicWorkRequest.Builder(WeatherWorker::class.java, 15, TimeUnit.MINUTES)
                .addTag(TAG_WEATHER_PERIODIC)
                .setInitialDelay(120, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 2, TimeUnit.MINUTES)
                .setConstraints(constraints)
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(TAG_WEATHER_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, builder.build())
        }

        fun executeOnce(context: Context) {
            Timber.tag(TAG).i("executeOnce")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build()
            val builder = OneTimeWorkRequest.Builder(WeatherWorker::class.java)
                .addTag(TAG_WEATHER_ONCE)
                .setInitialDelay(60, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 60, TimeUnit.SECONDS)
                .setConstraints(constraints)
            WorkManager.getInstance(context).enqueueUniqueWork(TAG_WEATHER_ONCE, ExistingWorkPolicy.REPLACE, builder.build())
        }
    }

}