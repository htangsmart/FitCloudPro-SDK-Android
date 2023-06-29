package com.topstep.fitcloud.sample2.ui.device.dial.component

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.github.kilnn.tool.ui.DisplayUtil
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.UnSupportDialLcdException
import com.topstep.fitcloud.sample2.databinding.FragmentDialComponentListBinding
import com.topstep.fitcloud.sample2.model.dial.DialSpacePacket
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.widget.GridSpacingItemDecoration
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

class DialComponentListFragment : BaseFragment(R.layout.fragment_dial_component_list) {

    private val viewBind: FragmentDialComponentListBinding by viewBinding()
    private val viewModel: DialComponentViewModel by viewModels({ requireParentFragment() })

    private lateinit var adapter: DialComponentListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = DialComponentListAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.ds_dial_component)

        val spanCount = getGridSpanCount(requireContext())
        viewBind.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        viewBind.recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, DisplayUtil.dip2px(requireContext(), 8F), true))

        adapter.listener = object : DialComponentListAdapter.Listener {
            override fun onItemClick(position: Int, item: DialSpacePacket) {
                viewModel.setComponents(position, null)
            }

            override fun onEditClick(position: Int, item: DialSpacePacket) {
                viewLifecycleScope.launchWhenStarted {
                    viewModel.clearSetComponents()
                    findNavController().navigate(DialComponentListFragmentDirections.toEdit(position))
                }
            }
        }
        viewBind.recyclerView.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.getParams()
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowEvent.collect {
                    if (it is AsyncEvent.OnFail) {
                        promptToast.showFailed(it.error)
                    }
                }
            }
            launch {
                viewModel.flowState.collect {
                    when (val async = it.getParams) {
                        Uninitialized, is Loading -> {
                            viewBind.loadingView.showLoading()
                            adapter.items = null
                            adapter.notifyDataSetChanged()
                        }
                        is Fail -> {
                            if (async.error is UnSupportDialLcdException) {
                                viewBind.loadingView.showError(R.string.ds_dial_error_shape)
                            } else {
                                viewBind.loadingView.showError(R.string.tip_load_error)
                            }
                        }
                        is Success -> {
                            val params = async()
                            adapter.shape = params.shape
                            adapter.items = params.dialSpacePackets?.toMutableList()
                            adapter.selectPosition = params.currentPosition
                            adapter.notifyDataSetChanged()
                            if (adapter.itemCount <= 0) {
                                viewBind.loadingView.showInfo(R.string.tip_current_no_data)
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

}