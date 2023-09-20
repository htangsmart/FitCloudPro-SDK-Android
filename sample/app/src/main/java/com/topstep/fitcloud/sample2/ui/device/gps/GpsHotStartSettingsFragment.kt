package com.topstep.fitcloud.sample2.ui.device.gps

import android.os.Bundle
import android.view.View
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentGpsHotStartSettingsBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.PermissionHelper
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewLifecycleScope
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sample2.worker.GpsHotStartWorker
import kotlinx.coroutines.launch

class GpsHotStartSettingsFragment : BaseFragment(R.layout.fragment_gps_hot_start_settings) {

    private val viewBind: FragmentGpsHotStartSettingsBinding by viewBinding()
    private val gpsHotStartRepository = Injector.getGpsHotStartRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycle.launchRepeatOnStarted {
            launch {
                gpsHotStartRepository.flowAutoUpdateGps().collect {
                    viewBind.itemAuto.getSwitchView().isChecked = it
                }
            }
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