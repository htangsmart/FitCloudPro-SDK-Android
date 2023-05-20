package com.topstep.fitcloud.sample2.ui.device.dial.library

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.ui.DisplayUtil
import com.polidea.rxandroidble3.exceptions.BleDisconnectedException
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.UnSupportDialLcdException
import com.topstep.fitcloud.sample2.data.device.isConnected
import com.topstep.fitcloud.sample2.databinding.FragmentDialLibraryBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.dial.DialPacket
import com.topstep.fitcloud.sample2.model.dial.DialPushParams
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.device.dial.DialPushViewModel
import com.topstep.fitcloud.sample2.ui.widget.GridSpacingItemDecoration
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

class DialLibraryFragment : BaseFragment(R.layout.fragment_dial_library) {

    private val viewBind: FragmentDialLibraryBinding by viewBinding()
    private val dialPushViewModel: DialPushViewModel by viewModels()
    private val viewModel: DialLibraryViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DialLibraryViewModel(dialPushViewModel) as T
            }
        }
    }

    private lateinit var adapter: DialLibraryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = DialLibraryAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        viewBind.recyclerView.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtil.dip2px(requireContext(), 8F), true))
        adapter.listener = object : DialLibraryAdapter.Listener {

            override fun onItemClick(packet: DialPacket) {
                if (dialPushViewModel.deviceManager.isConnected()) {
                    dialPushViewModel.state.async()?.let {
                        DialLibraryDfuDialogFragment.newInstance(packet, it)
                            .show(childFragmentManager, null)
                    }
                } else {
                    promptToast.showInfo(R.string.device_state_disconnected)
                }
            }
        }
        viewBind.recyclerView.adapter = adapter
        adapter.registerAdapterDataObserver(adapterDataObserver)

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
                            if (async.error is UnSupportDialLcdException) {
                                viewBind.loadingView.showError(R.string.ds_dial_error_shape)
                            } else {
                                viewBind.loadingView.showError(R.string.tip_load_error)
                            }
                        }
                        is Success -> {
                            val result = async()
                            adapter.shape = result.pushParams.shape
                            adapter.items = result.packets
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

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            if (adapter.itemCount <= 0) {
                viewBind.loadingView.showError(R.string.tip_current_no_data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
    }

}

data class PushParamsAndPackets(
    val pushParams: DialPushParams,
    val packets: List<DialPacket>
) {
    override fun toString(): String {
        return "pushParams:${pushParams} , packets size:${packets.size}"
    }
}

/**
 * Request and combine [DialPushParams] and [DialPacket] list
 */
class DialLibraryViewModel(
    private val dialPushViewModel: DialPushViewModel,
) : AsyncViewModel<SingleAsyncState<PushParamsAndPackets>>(SingleAsyncState()) {

    private val dialRepository = Injector.getDialRepository()

    init {
        viewModelScope.launch {
            dialPushViewModel.flowState.collect {
                when (val async = it.async) {
                    is Loading -> {
                        state.copy(async = Loading()).newState()
                    }
                    is Fail -> {
                        state.copy(async = Fail(async.error)).newState()
                        AsyncEvent.OnFail(SingleAsyncState<PushParamsAndPackets>::async, async.error).newEvent()
                    }
                    is Success -> {
                        refreshInternal(async())
                    }
                    else -> {}
                }
            }
        }
    }

    private fun refreshInternal(pushParams: DialPushParams) {
        suspend {
            val packets = dialRepository.getDialPacket(pushParams)
            PushParamsAndPackets(pushParams, packets)
        }.execute(SingleAsyncState<PushParamsAndPackets>::async) {
            copy(async = it)
        }
    }

    fun refresh() {
        when (val async = dialPushViewModel.state.async) {
            is Loading -> {
                //Requesting, so there is no need to process the request again
            }
            is Fail -> {
                if (async.error is BleDisconnectedException) {
                    //Device disconnected without performing refresh operation
                } else {
                    //Previous request failed, request DialParam again
                    dialPushViewModel.refresh()
                }
            }
            is Success -> {
                refreshInternal(async())
            }
            else -> {}
        }
    }
}