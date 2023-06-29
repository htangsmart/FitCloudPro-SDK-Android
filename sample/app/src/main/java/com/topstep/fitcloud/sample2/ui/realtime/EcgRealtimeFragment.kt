package com.topstep.fitcloud.sample2.ui.realtime

import android.os.Bundle
import android.view.View
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.isConnected
import com.topstep.fitcloud.sample2.data.entity.EcgRecordEntity
import com.topstep.fitcloud.sample2.databinding.FragmentEcgRealtimeBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.widget.EcgView
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import java.text.SimpleDateFormat
import java.util.*

class EcgRealtimeFragment : BaseFragment(R.layout.fragment_ecg_realtime) {

    private val viewBind: FragmentEcgRealtimeBinding by viewBinding()
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    private val deviceManager = Injector.getDeviceManager()
    private val authedUserId = Injector.requireAuthedUserId()
    private lateinit var ecgRealtimeHelper: EcgRealtimeHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ecgRealtimeHelper = EcgRealtimeHelper(requireContext(), authedUserId, deviceManager, listener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.btnStart.clickTrigger {
            toggleTesting()
        }
        viewBind.btnStop.clickTrigger {
            toggleTesting()
        }
    }

    private fun updateEcgDetailUI(record: EcgRecordEntity?) {
        viewBind.ecgView.clearData()
        if (record == null) {
            viewBind.tvTime.text = null
            viewBind.tvSpeed.text = null
            viewBind.tvAmplitude.text = null
            return
        }
        viewBind.ecgView.mode = EcgView.MODE_NORMAL
        viewBind.ecgView.samplingRate = record.samplingRate
        viewBind.ecgView.setDataType(record.type)
        viewBind.ecgView.addDataAndScrollToLast(record.getIntArrays())
        viewBind.tvTime.text = formatter.format(record.time)
        if (record.type == EcgRecordEntity.Type.TI) {
            viewBind.tvSpeed.visibility = View.VISIBLE
            viewBind.tvAmplitude.visibility = View.VISIBLE
            viewBind.tvSpeed.text = getString(R.string.ecg_speed, viewBind.ecgView.speed)
            viewBind.tvAmplitude.text = getString(R.string.ecg_amplitude, viewBind.ecgView.amplitude)
        } else {
            viewBind.tvSpeed.visibility = View.GONE
            viewBind.tvAmplitude.visibility = View.GONE
        }
    }

    private fun toggleTesting() {
        if (ecgRealtimeHelper.isStart()) {
            ecgRealtimeHelper.stop()
        } else {
            if (!deviceManager.isConnected()) {
                promptToast.showInfo(R.string.device_state_disconnected)
                return
            } else if (deviceManager.dataFeature.isSyncing()) {
                promptToast.showInfo(R.string.sync_data_ongoing)
                return
            }
            ecgRealtimeHelper.start()
        }
    }

    private val listener: EcgRealtimeHelper.Listener = object : EcgRealtimeHelper.Listener {
        override fun onEcgMeasurePrepare() {
            viewBind.btnStart.visibility = View.GONE
            viewBind.btnStop.visibility = View.VISIBLE
            viewBind.btnStop.text = null
            viewBind.progressBar.visibility = View.VISIBLE

            viewBind.tvTime.text = null
            viewBind.tvSpeed.visibility = View.GONE
            viewBind.tvAmplitude.visibility = View.GONE
        }

        override fun onEcgMeasureStart(time: Date, type: Int, samplingRate: Int) {
            viewBind.progressBar.visibility = View.GONE
            viewBind.btnStop.text = getString(R.string.health_stop_time, 60)

            viewBind.tvTime.text = formatter.format(time)
            viewBind.ecgView.clearData()
            viewBind.ecgView.mode = EcgView.MODE_REALTIME
            viewBind.ecgView.setDataType(type)
            viewBind.ecgView.samplingRate = samplingRate

            if (type == EcgRecordEntity.Type.TI) {
                viewBind.tvSpeed.visibility = View.VISIBLE
                viewBind.tvAmplitude.visibility = View.VISIBLE
                viewBind.tvSpeed.text = getString(R.string.ecg_speed, viewBind.ecgView.speed)
                viewBind.tvAmplitude.text = getString(R.string.ecg_amplitude, viewBind.ecgView.amplitude)
            }
        }

        override fun onEcgMeasureSeconds(seconds: Int) {
            viewBind.btnStop.text = getString(R.string.health_stop_time, seconds)
        }

        override fun onEcgMeasureAddData(data: IntArray) {
            viewBind.ecgView.addData(data)
        }

        override fun onEcgMeasureStop(record: EcgRecordEntity?) {
            viewBind.ecgView.mode = EcgView.MODE_NORMAL
            viewBind.btnStart.visibility = View.VISIBLE
            viewBind.btnStop.visibility = View.GONE
            viewBind.progressBar.visibility = View.GONE
            if (record != null) {
                updateEcgDetailUI(record)
            }
        }

        override fun onEcgMeasureError(throwable: Throwable) {
            promptToast.showFailed(throwable)
        }
    }

    override fun onPause() {
        super.onPause()
        ecgRealtimeHelper.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        ecgRealtimeHelper.release()
    }
}