package com.topstep.fitcloud.sample2.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.DeviceManager
import com.topstep.fitcloud.sample2.data.device.SyncDataRepository
import com.topstep.fitcloud.sample2.databinding.FragmentSyncBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.sync.*
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.features.FcDataFeature
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.data.FcSyncState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/05.Sync-Data
 *
 * ***Description**
 * Show how to sync data, observer sync state, save sync data
 *
 * **Usage**
 * 1. [SyncFragment]
 * Observer sync state and display available data types
 *
 * 2. [DeviceManager]
 * Execute [FcDataFeature.syncData] and emit [FcDataFeature.observerSyncState]
 *
 * 3. [SyncDataRepository]
 * Save sync data
 *
 * 4. [StepFragment]
 * Display step data
 *
 * 5. [SleepFragment]
 * Display sleep data
 *
 * 6. [HeartRateFragment]
 * Display heart rate data
 *
 * 7. [OxygenFragment]
 * Display oxygen data
 *
 * 8. [BloodPressureFragment]
 * Display blood pressure data
 *
 * 9. [TemperatureFragment]
 * Display temperature data
 *
 * 10. [PressureFragment]
 * Display pressure data
 *
 * 11. [EcgFragment]
 * Display ECG data
 *
 * 12. [SportFragment]
 * Display sport data
 *
 * 13. [GameFragment]
 * Display game data
 */
class SyncFragment : BaseFragment(R.layout.fragment_sync) {

    private val viewBind: FragmentSyncBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.refreshLayout.setOnRefreshListener {
            deviceManager.syncData()
        }
        viewBind.itemStep.clickTrigger(block = blockClick)
        viewBind.itemSleep.clickTrigger(block = blockClick)
        viewBind.itemHeartRate.clickTrigger(block = blockClick)
        viewBind.itemOxygen.clickTrigger(block = blockClick)
        viewBind.itemBloodPressure.clickTrigger(block = blockClick)
        viewBind.itemTemperature.clickTrigger(block = blockClick)
        viewBind.itemPressure.clickTrigger(block = blockClick)
        viewBind.itemEcg.clickTrigger(block = blockClick)
        viewBind.itemSport.clickTrigger(block = blockClick)
        viewBind.itemGame.clickTrigger(block = blockClick)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.configFeature.observerDeviceInfo().startWithItem(
                    deviceManager.configFeature.getDeviceInfo()
                ).asFlow().collect {
                    viewBind.itemHeartRate.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.HEART_RATE)
                    viewBind.itemOxygen.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.OXYGEN)
                    viewBind.itemBloodPressure.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.BLOOD_PRESSURE)
                    viewBind.itemTemperature.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.TEMPERATURE)
                    viewBind.itemPressure.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.PRESSURE)
                    viewBind.itemEcg.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.ECG)
                    viewBind.itemSport.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.SPORT)
                    viewBind.itemGame.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.GAME)
                }
            }
            launch {
                deviceManager.flowSyncState.collect { state ->
                    if (state == null || state == FcSyncState.SUCCESS) {//refresh none or success
                        viewBind.refreshLayout.isRefreshing = false
                        viewBind.tvRefreshState.setText(R.string.sync_state_idle)
                    } else if (state < 0) {//refresh fail
                        viewBind.refreshLayout.isRefreshing = false
                        viewBind.tvRefreshState.setText(R.string.sync_state_idle)
                    } else {//refresh progress
                        viewBind.refreshLayout.isRefreshing = true
                        viewBind.tvRefreshState.text = getString(R.string.sync_state_process, state)
                    }
                }
            }
            launch {
                deviceManager.flowSyncEvent.collectLatest {
                    when (it) {
                        DeviceManager.SyncEvent.SUCCESS -> {
                            promptToast.showSuccess(R.string.sync_data_success)
                        }
                        DeviceManager.SyncEvent.FAIL_DISCONNECT -> {
                            promptToast.showFailed(R.string.device_state_disconnected)
                        }
                        DeviceManager.SyncEvent.FAIL -> {
                            promptToast.showFailed(R.string.sync_data_failed)
                        }
                        else -> {
                            promptToast.dismiss()
                        }
                    }
                }
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemStep -> {
                findNavController().navigate(SyncFragmentDirections.toStep())
            }
            viewBind.itemSleep -> {
                findNavController().navigate(SyncFragmentDirections.toSleep())
            }
            viewBind.itemHeartRate -> {
                findNavController().navigate(SyncFragmentDirections.toHeartRate())
            }
            viewBind.itemOxygen -> {
                findNavController().navigate(SyncFragmentDirections.toOxygen())
            }
            viewBind.itemBloodPressure -> {
                findNavController().navigate(SyncFragmentDirections.toBloodPressure())
            }
            viewBind.itemTemperature -> {
                findNavController().navigate(SyncFragmentDirections.toTemperature())
            }
            viewBind.itemPressure -> {
                findNavController().navigate(SyncFragmentDirections.toPressure())
            }
            viewBind.itemEcg -> {
                findNavController().navigate(SyncFragmentDirections.toEcg())
            }
            viewBind.itemSport -> {
                findNavController().navigate(SyncFragmentDirections.toSport())
            }
            viewBind.itemGame -> {
                findNavController().navigate(SyncFragmentDirections.toGame())
            }
        }
    }
}