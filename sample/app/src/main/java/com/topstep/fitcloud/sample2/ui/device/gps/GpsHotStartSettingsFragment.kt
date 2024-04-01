package com.topstep.fitcloud.sample2.ui.device.gps

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentGpsHotStartSettingsBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sample2.worker.GpsHotStartWorker
import com.topstep.fitcloud.sdk.v2.model.settings.gps.FcGpsTimeInfo
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GpsHotStartSettingsFragment : BaseFragment(R.layout.fragment_gps_hot_start_settings) {

    private val toast by promptToast()
    private val viewBind: FragmentGpsHotStartSettingsBinding by viewBinding()
    private val gpsHotStartRepository = Injector.getGpsHotStartRepository()
    private val deviceManager = Injector.getDeviceManager()
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycle.launchRepeatOnStarted {
            launch {
                gpsHotStartRepository.flowAutoUpdateGps().collect {
                    viewBind.itemAuto.getSwitchView().isChecked = it
                }
            }
        }
        viewLifecycleScope.launch {
            deviceManager.settingsFeature.requestGpsHotStartTimeInfo()
                .onErrorReturnItem(FcGpsTimeInfo())
                .subscribe({
                    if (this@GpsHotStartSettingsFragment.view != null) {
                        viewBind.tvDeviceTime.text = getString(R.string.gps_hot_start_device_time) + ":" + format.format(Date(it.deviceValidTime))
                        viewBind.tvLastTime.text = getString(R.string.gps_hot_start_last_time) + ":" + format.format(Date(it.lastUpdateTime))
                    }
                }, {
                    //do nothing
                })
        }
        viewBind.itemAuto.getSwitchView().setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                PermissionHelper.requestLocation(this) { granted ->
                    if (granted) {
                        viewLifecycleScope.launch {
                            gpsHotStartRepository.setAutoUpdateGps(isChecked)
                        }
                    } else {
                        buttonView.isChecked = false
                    }
                }
            }
        }

        viewBind.btnClear.clickTrigger {
            lifecycleScope.launch {
                deviceManager.settingsFeature.setGpsEpoUpgradeMode(false).onErrorComplete().subscribe()
                lifecycleScope.launchWhenStarted {
                    toast.showSuccess("Success")
                }
            }
        }

        viewBind.btnOverwrite.clickTrigger {
            lifecycleScope.launch {
                deviceManager.settingsFeature.setGpsEpoUpgradeMode(true).onErrorComplete().subscribe()
                lifecycleScope.launchWhenStarted {
                    toast.showSuccess("Success")
                }
            }
        }

        viewBind.itemTestImmediately.clickTrigger {
            GpsHotStartWorker.executeTestImmediately(requireContext())
        }

        viewBind.itemTestDelay1.clickTrigger {
            GpsHotStartWorker.executeTestDelay1(requireContext())
            promptToast.showInfo("Will execute after 1 minute")
        }

        viewBind.itemTestDelay3.clickTrigger {
            GpsHotStartWorker.executeTestDelay3(requireContext())
            promptToast.showInfo("Will execute after 3 minute")
        }

        viewBind.itemTestDelay10.clickTrigger {
            GpsHotStartWorker.executeTestDelay10(requireContext())
            promptToast.showInfo("Will execute after 10 minute")
        }
    }

}