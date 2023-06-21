package com.topstep.fitcloud.sample2.ui.combine.wh

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.DeviceManager
import com.topstep.fitcloud.sample2.databinding.FragmentWhHomePageBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.wh.WomenHealthConfig
import com.topstep.fitcloud.sample2.model.wh.WomenHealthMode
import com.topstep.fitcloud.sample2.ui.CombineFragment
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import kotlinx.coroutines.launch

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/4.Device-info-and-configs#fcwomenhealthconfig
 *
 * ***Description**
 * Due to historical legacy issues, when reading this config from the device, only partial data may be returned.
 * Therefore, it is recommended not to read this config from the device, but always follow the config in your APP.
 *
 * The function of Women Health Sample does not depend on the config on the device, and all data are saved in the local storage(Room database) of the APP.
 * Send this config to the device only when it changes.
 *
 * **Usage**
 * 1. [CombineFragment]
 * Display the Women Health function entrance
 *
 * 2.[WhHomePageFragment]
 * Women Health home page fragment to select which mode to use
 *
 * 3.[WhSettingsFragment]
 * Set the config of a certain mode
 *
 * 4.[WhDetailFragment]
 * Display and modify detail info of a certain mode
 *
 * 5.[DeviceManager]
 * Apply the config to device if [FcDeviceInfo.Feature.WOMEN_HEALTH] is supported
 */
class WhHomePageFragment : BaseFragment(R.layout.fragment_wh_home_page) {

    private val viewBind: FragmentWhHomePageBinding by viewBinding()

    private val configRepository = Injector.getWomenHealthRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.itemModeNone.getImageView().setImageResource(R.drawable.ic_baseline_done_24)
        viewBind.itemModeMenstruation.getImageView().setImageResource(R.drawable.ic_baseline_done_24)
        viewBind.itemModePregnancyPrepare.getImageView().setImageResource(R.drawable.ic_baseline_done_24)
        viewBind.itemModePregnancy.getImageView().setImageResource(R.drawable.ic_baseline_done_24)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                configRepository.flowCurrent.collect {
                    updateUI(it)
                }
            }
        }
        viewBind.itemModeNone.clickTrigger {
            lifecycleScope.launch {
                configRepository.setConfig(null)
            }
        }
        viewBind.itemModeMenstruation.clickTrigger {
            findNavController().navigate(WhHomePageFragmentDirections.toWhSettings(WomenHealthMode.MENSTRUATION))
        }
        viewBind.itemModePregnancyPrepare.clickTrigger {
            findNavController().navigate(WhHomePageFragmentDirections.toWhSettings(WomenHealthMode.PREGNANCY_PREPARE))
        }
        viewBind.itemModePregnancy.clickTrigger {
            findNavController().navigate(WhHomePageFragmentDirections.toWhSettings(WomenHealthMode.PREGNANCY))
        }
    }

    private fun hideAllImages() {
        viewBind.itemModeNone.getImageView().visibility = View.INVISIBLE
        viewBind.itemModeMenstruation.getImageView().visibility = View.INVISIBLE
        viewBind.itemModePregnancyPrepare.getImageView().visibility = View.INVISIBLE
        viewBind.itemModePregnancy.getImageView().visibility = View.INVISIBLE
    }

    private fun updateUI(config: WomenHealthConfig?) {
        hideAllImages()
        when (config?.mode) {
            WomenHealthMode.MENSTRUATION -> {
                viewBind.itemModeMenstruation.getImageView().visibility = View.VISIBLE
            }
            WomenHealthMode.PREGNANCY_PREPARE -> {
                viewBind.itemModePregnancyPrepare.getImageView().visibility = View.VISIBLE
            }
            WomenHealthMode.PREGNANCY -> {
                viewBind.itemModePregnancy.getImageView().visibility = View.VISIBLE
            }
            else -> {
                viewBind.itemModeNone.getImageView().visibility = View.VISIBLE
            }
        }
    }
}