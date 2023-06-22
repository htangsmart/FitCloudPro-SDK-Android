package com.topstep.fitcloud.sample2.ui.device.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.flowStateConnected
import com.topstep.fitcloud.sample2.databinding.FragmentPageConfigBinding
import com.topstep.fitcloud.sample2.databinding.ItemPageConfigCheckBoxBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.di.internal.CoroutinesInstance.applicationScope
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.launchWithLog
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.FcPageConfig
import com.topstep.fitcloud.sdk.v2.model.config.toBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcpageconfig
 *
 * ***Description**
 * Display and modify the page displayed on the device
 *
 * **Usage**
 * 1. [DeviceConfigFragment]
 * According to whether [FcDeviceInfo.Feature.SETTING_PAGE_CONFIG] supports, show or hide the entrance
 *
 * 2. [PageConfigFragment]
 * Display and modify
 */
class PageConfigFragment : BaseFragment(R.layout.fragment_page_config) {


    private val viewBind: FragmentPageConfigBinding by viewBinding()

    /**
     * All flags
     */
    private val pageFlags = intArrayOf(
        FcPageConfig.Flag.TIME,
        FcPageConfig.Flag.STEP,
        FcPageConfig.Flag.DISTANCE,
        FcPageConfig.Flag.CALORIES,
        FcPageConfig.Flag.SLEEP,
        FcPageConfig.Flag.HEART_RATE,
        FcPageConfig.Flag.OXYGEN,
        FcPageConfig.Flag.BLOOD_PRESSURE,
        FcPageConfig.Flag.WEATHER,
        FcPageConfig.Flag.FIND_PHONE,
        FcPageConfig.Flag.ID,
        FcPageConfig.Flag.STOP_WATCH
    )

    /**
     * Page name of [pageFlags]
     */
    private val pageNames by lazy {
        resources.getStringArray(R.array.ds_page_names)
    }

    private val deviceManager = Injector.getDeviceManager()

    private lateinit var config: FcPageConfig
    private lateinit var adapter: InnerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = deviceManager.configFeature.getPageConfig()
        adapter = InnerAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.recyclerView.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.VERTICAL, false
        )
        viewBind.recyclerView.adapter = adapter
        adapter.listener = object : InnerAdapter.Listener {
            override fun onItemChanged(item: PageConfigItem, isEnabled: Boolean) {
                //save changed
                config.toBuilder().setFlagEnabled(item.flag, isEnabled).create().saveConfig()
            }
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                    //Only allow modify when device connected
                    viewBind.recyclerView.isEnabled = it
                    adapter.isModifyEnabled = it
                    updateUI()
                }
            }
            launch {
                deviceManager.configFeature.observerPageConfig().asFlow().collect {
                    if (config != it) {//observer changed
                        config = it
                        updateUI()
                    }
                }
            }
        }
    }

    private fun FcPageConfig.saveConfig() {
        applicationScope.launchWithLog {
            deviceManager.configFeature.setPageConfig(this@saveConfig).await()
        }
        this@PageConfigFragment.config = this
        updateUI()
    }

    private fun updateUI() {
        val list = ArrayList<PageConfigItem>()
        val deviceInfo = deviceManager.configFeature.getDeviceInfo()
        for (i in pageFlags.indices) {
            val flag = pageFlags[i]
            if (deviceInfo.isSupportPage(flag)) {
                list.add(
                    PageConfigItem(
                        name = pageNames[i],
                        flag = flag,
                        isEnabled = config.isFlagEnabled(flag)
                    )
                )
            }
        }
        adapter.sources = list
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.listener = null
    }

    private data class PageConfigItem(
        val name: String,
        val flag: Int,
        var isEnabled: Boolean
    )

    private class InnerAdapter : RecyclerView.Adapter<InnerViewHolder>() {

        /**
         * Only allow modify when device connected
         */
        var isModifyEnabled = false
        var sources: List<PageConfigItem>? = null
        var listener: Listener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
            return InnerViewHolder(
                ItemPageConfigCheckBoxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
            val item = sources?.get(position) ?: return
            holder.viewBind.checkbox.text = item.name
            holder.viewBind.checkbox.setOnCheckedChangeListener(null)
            holder.viewBind.checkbox.isChecked = item.isEnabled
            holder.viewBind.checkbox.isEnabled = isModifyEnabled
            holder.viewBind.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    val actionPosition = holder.bindingAdapterPosition
                    if (actionPosition != RecyclerView.NO_POSITION) {
                        listener?.onItemChanged(item, isChecked)
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return sources?.size ?: 0
        }

        interface Listener {
            fun onItemChanged(item: PageConfigItem, isEnabled: Boolean)
        }
    }

    private class InnerViewHolder(val viewBind: ItemPageConfigCheckBoxBinding) : RecyclerView.ViewHolder(viewBind.root)
}