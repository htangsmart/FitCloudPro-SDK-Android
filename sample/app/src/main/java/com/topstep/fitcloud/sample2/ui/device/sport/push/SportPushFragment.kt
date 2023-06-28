package com.topstep.fitcloud.sample2.ui.device.sport.push

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kilnn.tool.ui.DisplayUtil
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.isConnected
import com.topstep.fitcloud.sample2.databinding.FragmentSportPushBinding
import com.topstep.fitcloud.sample2.model.sport.push.SportPacket
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.widget.GridSpacingItemDecoration
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

class SportPushFragment : BaseFragment(R.layout.fragment_sport_push) {

    private val viewBind: FragmentSportPushBinding by viewBinding()
    private val viewModel: SportPushViewModel by viewModels()

    private val sportUiHelper = SportUiHelper()
    private val categoryAdapter = SportUiCategoryAdapter(sportUiHelper)
    private val itemAdapter = SportUiItemAdapter(sportUiHelper)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.recyclerViewCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewBind.recyclerViewCategory.adapter = categoryAdapter
        categoryAdapter.listener = object : SportUiCategoryAdapter.Listener {

            override fun onItemSelect(category: Int) {
                itemAdapter.category = category
                itemAdapter.notifyDataSetChanged()
            }
        }

        val spanCount = getGridSpanCount(requireContext())
        viewBind.recyclerViewItem.layoutManager = GridLayoutManager(requireContext(), spanCount)
        viewBind.recyclerViewItem.addItemDecoration(GridSpacingItemDecoration(spanCount, DisplayUtil.dip2px(requireContext(), 8F), true))
        viewBind.recyclerViewItem.adapter = itemAdapter
        itemAdapter.listener = object : SportUiItemAdapter.Listener {
            override fun onItemSelect(packet: SportPacket) {
                if (viewModel.deviceManager.isConnected()) {
                    viewLifecycleScope.launchWhenStarted {
                        viewModel.flowState.value.async()?.let {
                            val binFlag = it.pushableSpaces.lastOrNull()?.binFlag
                            if (binFlag == null) {
                                promptToast.showInfo(R.string.ds_sport_push_error_position)
                            } else {
                                SportDfuDialogFragment.newInstance(
                                    sportUiHelper.getTypeName(requireContext(), packet),
                                    packet,
                                    binFlag
                                ).show(childFragmentManager, null)
                            }
                        }
                    }
                } else {
                    promptToast.showInfo(R.string.device_state_disconnected)
                }
            }
        }

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.refresh()
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowEvent.collect {
                    when (it) {
                        is AsyncEvent.OnFail -> {
                            promptToast.showFailed(it.error)
                        }
                        else -> {}
                    }
                }
            }
            launch {
                viewModel.flowState.collect {
                    when (val async = it.async) {
                        Uninitialized, is Loading -> {
                            viewBind.loadingView.showLoading()
                        }
                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }
                        is Success -> {
                            val params = async()
                            categoryAdapter.selectPosition = 0
                            categoryAdapter.notifyDataSetChanged()

                            itemAdapter.items = params.packets
                            itemAdapter.category = categoryAdapter.getSelectCategory()
                            itemAdapter.notifyDataSetChanged()

                            if (itemAdapter.itemCount <= 0) {
                                viewBind.loadingView.showInfo(R.string.tip_current_no_data)
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            viewLifecycle.launchRepeatOnStarted {
                launch {
                    flowLocationServiceState(requireContext()).collect { isEnabled ->
                        viewBind.layoutLocationService.isVisible = !isEnabled
                    }
                }
            }
        } else {
            viewBind.layoutLocationService.isVisible = false
        }
    }

}