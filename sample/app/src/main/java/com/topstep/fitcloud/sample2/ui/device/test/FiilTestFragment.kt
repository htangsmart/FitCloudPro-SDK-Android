package com.topstep.fitcloud.sample2.ui.device.test

import android.os.Bundle
import android.view.View
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentFiilTestBinding
import com.topstep.fitcloud.sample2.fcSDK
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewLifecycleScope
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.FcConnector
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await

class FiilTestFragment : BaseFragment(R.layout.fragment_fiil_test) {
    private lateinit var connector: FcConnector

    private val viewBind: FragmentFiilTestBinding by viewBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connector = requireContext().fcSDK.connector

        lifecycle.launchRepeatOnStarted {
            launch {
                try {
                    val list = connector.settingsFeature().requestLyricColor().doOnError {
                        promptToast.showFailed(it)
                    }.await()
                    viewBind.tvRgb.text = "[${list.joinToString(",")}]"
                } catch (e: Exception) {
                    //nothing
                }
            }
            launch {
                try {
                    val isChargingLight = connector.settingsFeature().requestChargingLight().doOnError {
                        promptToast.showFailed(it)
                    }.await()
                    viewBind.itemChargingLight.getSwitchView().isChecked = isChargingLight
                } catch (e: Exception) {
                    //nothing
                }
            }
        }

        viewBind.btnRgb.setOnClickListener {
            val r = asInt(viewBind.etR.text.toString())
            val g = asInt(viewBind.etG.text.toString())
            val b = asInt(viewBind.etB.text.toString())

            viewLifecycleScope.launch {
                connector.settingsFeature().setLyricColor(r, g, b)
                    .doOnError {
                        promptToast.showFailed(it)
                    }.onErrorComplete().await()

                try {
                    val list = connector.settingsFeature().requestLyricColor().doOnError {
                        promptToast.showFailed(it)
                    }.await()
                    viewBind.tvRgb.text = "[${list.joinToString(",")}]"
                } catch (e: Exception) {
                    //nothing
                }

            }
        }

        viewBind.itemChargingLight.getSwitchView().setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                viewLifecycleScope.launch {
                    connector.settingsFeature().setChargingLight(isChecked).doOnError {
                        promptToast.showFailed(it)
                    }.onErrorComplete().await()
                }
            }
        }

    }

    private fun asInt(string: String?): Int {
        if (string.isNullOrBlank()) return 0
        return string.toInt()
    }


}