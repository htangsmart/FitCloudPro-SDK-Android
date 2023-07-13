package com.topstep.fitcloud.sample2.ui.device

import android.os.Bundle
import android.view.View
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentOtherFeaturesBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.viewLifecycleScope
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

class OtherFeaturesFragment : BaseFragment(R.layout.fragment_other_features) {

    private val viewBind: FragmentOtherFeaturesBinding by viewBinding()
    private val deviceManager = Injector.getDeviceManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemFindDevice.clickTrigger {
            viewLifecycleScope.launch {
                deviceManager.messageFeature.findDevice().onErrorComplete().subscribe()
            }
        }

        viewBind.itemDeviceReboot.clickTrigger {
            viewLifecycleScope.launch {
                deviceManager.settingsFeature.deviceReboot().onErrorComplete().subscribe()
            }
        }

        viewBind.itemDeviceShutdown.clickTrigger {
            viewLifecycleScope.launch {
                deviceManager.settingsFeature.deviceShutdown().onErrorComplete().subscribe()
            }
        }

        viewBind.itemDeviceReset.clickTrigger {
            viewLifecycleScope.launch {
                deviceManager.settingsFeature.deviceReset().onErrorComplete().subscribe()
            }
        }
    }

}