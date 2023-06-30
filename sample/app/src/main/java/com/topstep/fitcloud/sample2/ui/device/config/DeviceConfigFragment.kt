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
        viewBind.itemNotification.clickTrigger(block = blockClick)
        viewBind.itemFunction.clickTrigger(block = blockClick)
        viewBind.itemHealthMonitor.clickTrigger(block = blockClick)
        viewBind.itemSedentary.clickTrigger(block = blockClick)
        viewBind.itemDrinkWater.clickTrigger(block = blockClick)
        viewBind.itemBloodPressure.clickTrigger(block = blockClick)
        viewBind.itemTurnWristLighting.clickTrigger(block = blockClick)
        viewBind.itemDnd.clickTrigger(block = blockClick)
        viewBind.itemScreenVibrate.clickTrigger(block = blockClick)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowState.collect {
                    viewBind.layoutContent.setAllChildEnabled(it == ConnectorState.CONNECTED)
                }
            }
            launch {
                deviceManager.configFeature.observerDeviceInfo().startWithItem(
                    deviceManager.configFeature.getDeviceInfo()
                ).asFlow().collect {
                    viewBind.itemPage.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.SETTING_PAGE_CONFIG)
                    viewBind.itemBloodPressure.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.BLOOD_PRESSURE) and !it.isSupportFeature(FcDeviceInfo.Feature.BLOOD_PRESSURE_AIR_PUMP)
                    viewBind.itemDnd.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.DND)
                    viewBind.itemScreenVibrate.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.SCREEN_VIBRATE)
                }
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemPage -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toPageConfig())
            }
            viewBind.itemNotification -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toNotificationConfig())
            }
            viewBind.itemFunction -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toFunctionConfig())
            }
            viewBind.itemHealthMonitor -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toHealthMonitorConfig())
            }
            viewBind.itemSedentary -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toSedentaryConfig())
            }
            viewBind.itemDrinkWater -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toDrinkWaterConfig())
            }
            viewBind.itemBloodPressure -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toBpConfig())
            }
            viewBind.itemTurnWristLighting -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toTurnWristLightingConfig())
            }
            viewBind.itemDnd -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toDndConfig())
            }
            viewBind.itemScreenVibrate -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toScreenVibrateConfig())
            }
        }
    }

}