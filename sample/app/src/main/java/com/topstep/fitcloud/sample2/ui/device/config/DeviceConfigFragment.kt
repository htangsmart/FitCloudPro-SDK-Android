package com.topstep.fitcloud.sample2.ui.device.config

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentDeviceConfigBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.device.ConnectorState
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.setAllChildEnabled
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow

class DeviceConfigFragment : BaseFragment(R.layout.fragment_device_config) {

    private val viewBind: FragmentDeviceConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemPage.clickTrigger(block = blockClick)
        viewBind.itemDnd.clickTrigger(block = blockClick)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowState.collect {
                    viewBind.layoutContent.setAllChildEnabled(it == ConnectorState.CONNECTED)
                }
            }
            launch {
                deviceManager.configFeature.observerDeviceInfo().asFlow().collect {
                    viewBind.itemPage.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.SETTING_PAGE_CONFIG)
                    viewBind.itemDnd.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.DND)
                }
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemPage -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toPageConfig())
            }
            viewBind.itemDnd -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toDndConfig())
            }
        }
    }

}