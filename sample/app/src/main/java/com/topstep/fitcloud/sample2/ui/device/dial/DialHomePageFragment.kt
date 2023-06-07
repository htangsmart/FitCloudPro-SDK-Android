package com.topstep.fitcloud.sample2.ui.device.dial

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentDialHomePageBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo

class DialHomePageFragment : BaseFragment(R.layout.fragment_dial_home_page) {

    private val viewBind: FragmentDialHomePageBinding by viewBinding()
    private val deviceManager = Injector.getDeviceManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Check which dial feature the device supports
        val deviceInfo = deviceManager.configFeature.getDeviceInfo()

        viewBind.layoutDialLibrary.isVisible = deviceInfo.isSupportFeature(FcDeviceInfo.Feature.DIAL_PUSH)

        viewBind.layoutDialCustom.isVisible = deviceInfo.isSupportFeature(FcDeviceInfo.Feature.DIAL_PUSH)
                && deviceInfo.isSupportFeature(FcDeviceInfo.Feature.DIAL_CUSTOM)

        viewBind.layoutDialComponent.isVisible = deviceInfo.isSupportFeature(FcDeviceInfo.Feature.DIAL_COMPONENT)
                && deviceInfo.isSupportFeature(FcDeviceInfo.Feature.SETTING_DIAL_COMPONENT)

        viewBind.tvNoneFeature.isVisible = !(viewBind.layoutDialLibrary.isVisible || viewBind.layoutDialCustom.isVisible || viewBind.layoutDialComponent.isVisible)

        viewBind.btnDialLibrary.clickTrigger {
            findNavController().navigate(DialHomePageFragmentDirections.toLibrary())
        }
        viewBind.btnDialCustom.clickTrigger {
            findNavController().navigate(DialHomePageFragmentDirections.toCustom())
        }
        viewBind.btnDialComponent.clickTrigger {
            findNavController().navigate(DialHomePageFragmentDirections.toComponent())
        }
    }

}