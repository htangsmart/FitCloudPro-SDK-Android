package com.topstep.fitcloud.sample2.ui.device.game.push

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.github.kilnn.tool.ui.DisplayUtil
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.device.isConnected
import com.topstep.fitcloud.sample2.databinding.FragmentGameDetailBinding
import com.topstep.fitcloud.sample2.model.game.push.GamePacket
import com.topstep.fitcloud.sample2.model.game.push.GameSkin
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.widget.GridSpacingItemDecoration
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

class GameDetailFragment : BaseFragment(R.layout.fragment_game_detail) {

    private val viewBind: FragmentGameDetailBinding by viewBinding()
    private val args: GameDetailFragmentArgs by navArgs()
    private val viewModel: GamePushViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: GameSkinAdapter
    private var gamePacket: GamePacket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = GameSkinAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    findNavController().navigateUp()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)

        (requireActivity() as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.ds_game_detail)

        val spanCount = getGridSpanCount(requireContext(), 4)
        viewBind.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        viewBind.recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, DisplayUtil.dip2px(requireContext(), 8F), true))
        adapter.listener = object : GameSkinAdapter.Listener {
            override fun onItemClick(skin: GameSkin) {
                if (viewModel.deviceManager.isConnected()) {
                    val packet = gamePacket ?: return
                    viewModel.state.async()?.let {
                        GameDfuDialogFragment.newInstance(packet.name, skin, it.pushableSpaceSkins)
                            .show(childFragmentManager, null)
                    }
                } else {
                    promptToast.showInfo(R.string.device_state_disconnected)
                }
            }
        }
        viewBind.recyclerView.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.refresh()
        }

        lifecycle.launchRepeatOnStarted {
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
                            val result = async()
                            val packet = result.remoteGamePackets.find { packet ->
                                packet.type == args.type
                            }
                            if (packet == null) {
                                //没有这个数据，那么退出
                                findNavController().navigateUp()
                            } else {
                                updateUI(packet)
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
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                launch {
                    flowLocationServiceState(requireContext()).collect { isEnabled ->
                        viewBind.layoutLocationService.isVisible = !isEnabled
                    }
                }
            } else {
                viewBind.layoutLocationService.isVisible = false
            }
        }
    }

    private fun updateUI(packet: GamePacket) {
        gamePacket = packet
        glideShowImage(viewBind.imgGame, packet.imgUrl)
        viewBind.tvName.text = packet.name
        adapter.items = packet.gameSkins
        adapter.notifyDataSetChanged()
        viewBind.tvDescription.text = packet.description
        viewBind.loadingView.visibility = View.GONE
    }

}