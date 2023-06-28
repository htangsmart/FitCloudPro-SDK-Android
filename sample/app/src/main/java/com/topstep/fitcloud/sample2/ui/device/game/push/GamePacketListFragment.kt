package com.topstep.fitcloud.sample2.ui.device.game.push

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.github.kilnn.tool.ui.DisplayUtil
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentGamePacketListBinding
import com.topstep.fitcloud.sample2.model.game.push.GamePacket
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.widget.GridSpacingItemDecoration
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.utils.getGridSpanCount
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

class GamePacketListFragment : BaseFragment(R.layout.fragment_game_packet_list) {

    private val viewBind: FragmentGamePacketListBinding by viewBinding()

    private val isLocal: Boolean by lazy {
        requireArguments().getBoolean(EXTRA_IS_LOCAL)
    }

    private val viewModel: GamePushViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: GamePacketListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = GamePacketListAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spanCount = getGridSpanCount(requireContext(), 4)
        viewBind.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        viewBind.recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, DisplayUtil.dip2px(requireContext(), 8F), true))

        adapter.listener = object : GamePacketListAdapter.Listener {
            override fun onItemClick(packet: GamePacket) {
                requireParentFragment().findNavController()
                    .navigate(GamePacketParentFragmentDirections.toDetail(packet.type))
            }
        }
        viewBind.recyclerView.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.refresh()
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    when (val async = it.async) {
                        Uninitialized, is Loading -> {
                            viewBind.loadingView.showLoading()
                            adapter.items = null
                            adapter.notifyDataSetChanged()
                        }
                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }
                        is Success -> {
                            val result = async()
                            if (isLocal) {
                                viewBind.tvTips.visibility = View.VISIBLE
                                adapter.items = result.localGamePackets
                            } else {
                                viewBind.tvTips.visibility = View.GONE
                                adapter.items = result.remoteGamePackets
                            }
                            adapter.notifyDataSetChanged()
                            if (adapter.itemCount <= 0) {
                                viewBind.loadingView.showError(R.string.tip_current_no_data)
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                            }
                        }
                    }
                }
            }
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
        }
    }

    companion object {
        private const val EXTRA_IS_LOCAL = "is_local"
        fun newInstance(local: Boolean): GamePacketListFragment {
            val fragment = GamePacketListFragment()
            fragment.arguments = Bundle().apply { putBoolean(EXTRA_IS_LOCAL, local) }
            return fragment
        }
    }

}