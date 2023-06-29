package com.topstep.fitcloud.sample2.ui.realtime

import android.content.Context
import android.media.SoundPool
import androidx.annotation.MainThread
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.DeviceManager
import com.topstep.fitcloud.sample2.data.entity.EcgRecordEntity
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.data.FcEcgData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class EcgRealtimeHelper(
    context: Context,
    private val userId: Long,
    private val deviceManager: DeviceManager,
    private val listener: Listener
) {

    private var testingDisposable: Disposable? = null
    private var timerDisposable: Disposable? = null
    private var playerDisposable: Disposable? = null

    @Volatile
    private var isStarted = false

    @Volatile
    private var ecgRecord: EcgRecordEntity? = null

    private val soundPool = SoundPool.Builder().setMaxStreams(1).build()
    private var soundId = soundPool.load(context, R.raw.ecg, 1)

    @MainThread
    fun start() {
        if (isStarted) {
            Timber.tag(TAG).w("start again!!!")
        } else {
            Timber.tag(TAG).i("start")
        }
        //reset all
        clearDisposable()
        isStarted = true
        ecgRecord = null
        val isTiEcg = deviceManager.configFeature.getDeviceInfo().isSupportFeature(FcDeviceInfo.Feature.TI_ECG)
        testingDisposable = deviceManager.dataFeature.openEcgRealTimeData()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                listener.onEcgMeasurePrepare()
            }
            .doAfterTerminate {
                stop()
            }
            .subscribe({ array ->
                val ecgRecord = this.ecgRecord
                if (ecgRecord == null) {
                    val isSamplingRatePacket = array.size == 1 && array[0] > 0//It is a packet with a sampling rate
                    val record = EcgRecordEntity(
                        userId = userId,
                        ecgId = UUID.randomUUID(),
                        time = Date(),
                        type = if (isTiEcg) EcgRecordEntity.Type.TI else EcgRecordEntity.Type.NORMAL,
                        samplingRate = if (isSamplingRatePacket) array[0] else FcEcgData.DEFAULT_SAMPLING_RATE,
                        detail = ArrayList(1000),
                    ).also { this.ecgRecord = it }
                    listener.onEcgMeasureStart(record.time, record.type, record.samplingRate)
                    startTestEffect()
                    if (!isSamplingRatePacket) {
                        (record.detail as ArrayList).addAll(array.toList())
                        listener.onEcgMeasureAddData(array)
                    }
                } else {
                    (ecgRecord.detail as ArrayList).addAll(array.toList())
                    listener.onEcgMeasureAddData(array)
                }
            }, {
                listener.onEcgMeasureError(it)
                Timber.tag(TAG).w(it)
            })
    }

    @MainThread
    fun stop() {
        if (!isStarted) return
        Timber.tag(TAG).i("stop")
        val record = this.ecgRecord
        //reset all
        clearDisposable()
        isStarted = false
        this.ecgRecord = null
        if (record?.detail?.isNotEmpty() == true) {
            listener.onEcgMeasureStop(record)
        } else {
            listener.onEcgMeasureStop(null)
        }
    }

    @MainThread
    fun isStart(): Boolean {
        return isStarted
    }

    private fun startTestEffect() {
        timerDisposable = Observable.intervalRange(0, (TIME_TESTING + 1).toLong(), 0, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val value = TIME_TESTING - it.toInt()
                listener.onEcgMeasureSeconds(value)
            }, {
                Timber.tag(TAG).w(it)
            }, {
                stop()
            })
        playerDisposable = Observable.intervalRange(0, 10000, 0, 750, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
            }, {
                Timber.tag(TAG).w(it)
            })
    }

    private fun clearDisposable() {
        testingDisposable?.dispose()
        timerDisposable?.dispose()
        playerDisposable?.dispose()
    }

    fun release() {
        soundPool.release()
    }

    interface Listener {
        fun onEcgMeasurePrepare()
        fun onEcgMeasureStart(time: Date, @EcgRecordEntity.Type type: Int, samplingRate: Int)
        fun onEcgMeasureSeconds(seconds: Int)
        fun onEcgMeasureAddData(data: IntArray)
        fun onEcgMeasureStop(record: EcgRecordEntity?)
        fun onEcgMeasureError(throwable: Throwable)
    }

    companion object {
        private const val TAG = "EcgTestHelper"
        private const val TIME_TESTING = 30 //30s
    }
}