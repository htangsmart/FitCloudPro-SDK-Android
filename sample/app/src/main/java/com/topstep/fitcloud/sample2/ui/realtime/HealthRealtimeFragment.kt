package com.topstep.fitcloud.sample2.ui.realtime

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.isConnected
import com.topstep.fitcloud.sample2.databinding.FragmentHealthRealtimeBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.utils.celsius2Fahrenheit
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.exception.FcTemperatureRealTimeException
import com.topstep.fitcloud.sdk.util.FlagUtil
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.FcFunctionConfig
import com.topstep.fitcloud.sdk.v2.model.data.FcHealthDataResult
import com.topstep.fitcloud.sdk.v2.model.data.FcHealthDataType
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.max

class HealthRealtimeFragment : BaseFragment(R.layout.fragment_health_realtime) {

    private val viewBind: FragmentHealthRealtimeBinding by viewBinding()
    private val args: HealthRealtimeFragmentArgs by navArgs()
    private val deviceManager = Injector.getDeviceManager()

    private var measureDisposable: Disposable? = null
    private var timerDisposable: Disposable? = null

    private var heartRate: HealthRealtimeValueHolder? = null
    private var oxygen: HealthRealtimeValueHolder? = null
    private var bloodPressure: HealthRealtimeValueHolder? = null
    private var temperature: HealthRealtimeValueHolder? = null
    private var pressure: HealthRealtimeValueHolder? = null

    private val isAirPumpBloodPressure = deviceManager.configFeature.getDeviceInfo().isSupportFeature(FcDeviceInfo.Feature.BLOOD_PRESSURE_AIR_PUMP)
    private val isTemperatureCentigrade = !deviceManager.configFeature.getFunctionConfig().isFlagEnabled(FcFunctionConfig.Flag.TEMPERATURE_UNIT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.btnStart.clickTrigger {
            toggleMeasure(args.healthType)
        }
        viewBind.btnStop.clickTrigger {
            toggleMeasure(args.healthType)
        }
    }

    private fun toggleMeasure(healthType: Int) {
        val disposable = measureDisposable
        if (disposable != null && !disposable.isDisposed) {
            disposable.dispose()
            return
        }

        if (!deviceManager.isConnected()) {
            promptToast.showInfo(R.string.device_state_disconnected)
            return
        } else if (deviceManager.dataFeature.isSyncing()) {
            promptToast.showInfo(R.string.sync_data_ongoing)
            return
        }

        val showTimer = if (FlagUtil.isFlagEnabled(healthType, FcHealthDataType.BLOOD_PRESSURE)) {
            //If measure type contain BLOOD_PRESSURE
            //Only show timer when Feature.BLOOD_PRESSURE_AIR_PUMP is not support
            !isAirPumpBloodPressure
        } else {
            true
        }

        measureDisposable = deviceManager.dataFeature
            .openHealthRealTimeData(healthType, 1)//Measure time 1 minute
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                onMeasureStart(healthType, showTimer)
                if (showTimer) {
                    timerDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                        .map {
                            60 - it.toInt()//Countdown from 60 seconds
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            updateMeasureTime(max(it, 0))
                        }, {
                            Timber.w(it)
                        })
                }
            }
            .doOnTerminate {
                timerDisposable?.dispose()
                onMeasureStop()
            }
            .doOnDispose {
                timerDisposable?.dispose()
                onMeasureStop()
            }
            .subscribe({
                onMeasureResult(it)
            }, {
                Timber.w(it)
                if (it is FcTemperatureRealTimeException) {
                    when (it.reason) {
                        FcTemperatureRealTimeException.REASON_NOT_WORN -> {
                            promptToast.showFailed(R.string.temperature_test_error1)
                        }
                        FcTemperatureRealTimeException.REASON_HYPERTHERMIA -> {
                            promptToast.showFailed(R.string.temperature_test_error2)
                        }
                        FcTemperatureRealTimeException.REASON_HYPOTHERMIA -> {
                            promptToast.showFailed(R.string.temperature_test_error3)
                        }
                    }
                } else {
                    promptToast.showFailed(it)
                }
            })
    }

    override fun onPause() {
        super.onPause()
        measureDisposable?.dispose()
    }

    private fun onMeasureStart(healthType: Int, showTimer: Boolean) {
        if (FlagUtil.isFlagEnabled(healthType, FcHealthDataType.HEART_RATE)) {
            heartRate = HealthRealtimeValueHolder()
        }
        if (FlagUtil.isFlagEnabled(healthType, FcHealthDataType.OXYGEN)) {
            oxygen = HealthRealtimeValueHolder()
        }
        if (FlagUtil.isFlagEnabled(healthType, FcHealthDataType.BLOOD_PRESSURE)) {
            bloodPressure = HealthRealtimeValueHolder()
        }
        if (FlagUtil.isFlagEnabled(healthType, FcHealthDataType.TEMPERATURE)) {
            temperature = HealthRealtimeValueHolder()
        }
        if (FlagUtil.isFlagEnabled(healthType, FcHealthDataType.PRESSURE)) {
            pressure = HealthRealtimeValueHolder()
        }

        viewBind.tvHeartRate.text = null
        viewBind.tvOxygen.text = null
        viewBind.tvBloodPressure.text = null
        viewBind.tvTemperature.text = null
        viewBind.tvPressure.text = null

        viewBind.btnStop.isVisible = true
        viewBind.btnStart.isVisible = false

        if (showTimer) {
            viewBind.btnStop.text = getString(R.string.health_stop_time, 60)
        } else {
            viewBind.btnStop.setText(R.string.health_stop)
        }
    }

    private fun onMeasureStop() {

        heartRate?.let {
            if (it.count() > 0) {
                //TODO The measurement results have been generated, you can save it or display on UI
                val result = it.toHeartRate()
            }
        }
        oxygen?.let {
            if (it.count() > 0) {
                //TODO The measurement results have been generated, you can save it or display on UI
                val result = it.toOxygen()
            }
        }
        bloodPressure?.let {
            if (it.count() > 0) {
                //TODO The measurement results have been generated, you can save it or display on UI
                val result = it.toBloodPressure(isAirPumpBloodPressure)
            }
        }
        temperature?.let {
            if (it.count() > 0) {
                //TODO The measurement results have been generated, you can save it or display on UI
                val result = it.toTemperature()
            }
        }
        pressure?.let {
            if (it.count() > 0) {
                //TODO The measurement results have been generated, you can save it or display on UI
                val result = it.toPressure()
            }
        }

        viewBind.btnStop.isVisible = false
        viewBind.btnStart.isVisible = true
        viewBind.btnStart.setText(R.string.health_start)
    }

    private fun onMeasureResult(result: FcHealthDataResult) {
        heartRate?.let {
            val value: Int = result.heartRate
            if (value > 0) {
                it.addValue(value.toFloat())
                viewBind.tvHeartRate.text = getString(R.string.unit_bmp_unit, value)
            }
        }
        oxygen?.let {
            val value: Int = result.oxygen
            if (value > 0) {
                it.addValue(value.toFloat())
                viewBind.tvOxygen.text = getString(R.string.percent_param, value)
            }
        }
        bloodPressure?.let {
            val sbp: Int = result.systolicPressure
            val dbp: Int = result.diastolicPressure
            if (sbp > 0 && (isAirPumpBloodPressure || dbp > 0)) {
                it.addValue(sbp.toFloat(), dbp.toFloat())
                displayBloodPressureStr(sbp, dbp)
            }
        }
        temperature?.let {
            val body: Float = result.temperatureBody
            val wrist: Float = result.temperatureWrist
            if (wrist != 0f) { //Wrist temperature must have a valid value
                it.addValue(body, wrist)
                displayTemperatureStr(body, wrist)
            }
        }
        pressure?.let {
            val value: Int = result.pressure
            if (value > 0) {
                it.addValue(value.toFloat())
                viewBind.tvPressure.text = FormatterUtil.intStr(value)
            }
        }
    }

    private fun updateMeasureTime(time: Int) {
        viewBind.btnStop.text = getString(R.string.health_stop_time, time)
    }

    private fun displayBloodPressureStr(sbp: Int, dbp: Int) {
        if (dbp <= 0) {
            //If db<=0 , Indicates that isAirPumpBloodPressure is true, representing the value of the air pump
            viewBind.tvBloodPressure.text = String.format(FormatterUtil.systemLocale, "%d", sbp)
        } else {
            viewBind.tvBloodPressure.text = getString(
                R.string.unit_mmhg_param,
                String.format(FormatterUtil.systemLocale, "%d/%d", sbp, dbp)
            )
        }
    }

    private fun displayTemperatureStr(body: Float, wrist: Float) {
        val bodyStr = if (body <= 0) {
            "--"
        } else {
            if (isTemperatureCentigrade) {
                "$body"
            } else {
                "${body.celsius2Fahrenheit()}"
            }
        }
        if (isTemperatureCentigrade) {
            viewBind.tvTemperature.text = getString(
                R.string.unit_centigrade_param,
                "$bodyStr/$wrist"
            )
        } else {
            viewBind.tvTemperature.text = getString(
                R.string.unit_fahrenheit_param,
                "$bodyStr/${wrist.celsius2Fahrenheit()}"
            )
        }
    }

}