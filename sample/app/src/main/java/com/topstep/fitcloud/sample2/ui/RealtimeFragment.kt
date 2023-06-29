package com.topstep.fitcloud.sample2.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentRealtimeBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.data.FcHealthDataType
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow

class RealtimeFragment : BaseFragment(R.layout.fragment_realtime) {

    private val viewBind: FragmentRealtimeBinding by viewBinding()
    private val deviceManager = Injector.getDeviceManager()
    private var supportHealthType = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemHealthRealtime.clickTrigger(block = blockClick)
        viewBind.itemEcgRealtime.clickTrigger(block = blockClick)
        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.configFeature.observerDeviceInfo().startWithItem(
                    deviceManager.configFeature.getDeviceInfo()
                ).asFlow().collect {
                    supportHealthType = 0
                    if (it.isSupportFeature(FcDeviceInfo.Feature.HEART_RATE)) {
                        supportHealthType = supportHealthType or FcHealthDataType.HEART_RATE
                    }
                    if (it.isSupportFeature(FcDeviceInfo.Feature.OXYGEN)) {
                        supportHealthType = supportHealthType or FcHealthDataType.OXYGEN
                    }
                    if (it.isSupportFeature(FcDeviceInfo.Feature.BLOOD_PRESSURE)) {
                        supportHealthType = supportHealthType or FcHealthDataType.BLOOD_PRESSURE
                    }
                    if (it.isSupportFeature(FcDeviceInfo.Feature.TEMPERATURE)) {
                        supportHealthType = supportHealthType or FcHealthDataType.TEMPERATURE
                    }
                    if (it.isSupportFeature(FcDeviceInfo.Feature.PRESSURE)) {
                        supportHealthType = supportHealthType or FcHealthDataType.PRESSURE
                    }
                    viewBind.itemHealthRealtime.isVisible = supportHealthType != 0
                    viewBind.itemEcgRealtime.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.ECG)
                }
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemHealthRealtime -> {
                findNavController().navigate(RealtimeFragmentDirections.toHealthRealtime(supportHealthType))
            }
            viewBind.itemEcgRealtime -> {
                findNavController().navigate(RealtimeFragmentDirections.toEcgRealtime())
            }
        }
    }
}