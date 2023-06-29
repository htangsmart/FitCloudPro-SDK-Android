package com.topstep.fitcloud.sample2.ui.device.dial.library

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.DialogDialLibraryDfuBinding
import com.topstep.fitcloud.sample2.model.dial.DialPacket
import com.topstep.fitcloud.sample2.model.dial.DialPushParams
import com.topstep.fitcloud.sample2.ui.device.DfuViewModel
import com.topstep.fitcloud.sample2.ui.device.dial.DialSpacePacketAdapter
import com.topstep.fitcloud.sample2.ui.device.showDfuFail
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DialLibraryDfuDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val EXTRA_DIAL_PACKET = "dial_packet"
        private const val EXTRA_PUSH_PARAMS = "push_params"

        fun newInstance(dialPacket: DialPacket, pushParams: DialPushParams): DialLibraryDfuDialogFragment {
            val fragment = DialLibraryDfuDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(EXTRA_DIAL_PACKET, dialPacket)
                putParcelable(EXTRA_PUSH_PARAMS, pushParams)
            }
            return fragment
        }
    }

    private lateinit var dialPacket: DialPacket
    private lateinit var pushParams: DialPushParams
    private var adapter: DialSpacePacketAdapter? = null

    private var _viewBind: DialogDialLibraryDfuBinding? = null
    private val viewBind get() = _viewBind!!

    private val dfuViewModel: DfuViewModel by viewModels({ requireParentFragment() })
    private val promptToast by promptToast()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            dialPacket = it.getParcelableCompat(EXTRA_DIAL_PACKET)!!
            pushParams = it.getParcelableCompat(EXTRA_PUSH_PARAMS)!!
        }
        val pushableSpacePackets = pushParams.filterPushableSpacePackets()
        if (pushableSpacePackets.isNotEmpty()) {
            adapter = DialSpacePacketAdapter(pushableSpacePackets, dialPacket.binSize, pushParams.shape)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogDialLibraryDfuBinding.inflate(LayoutInflater.from(context))

        viewBind.tvName.text = dialPacket.name

        glideShowImage(viewBind.imgView, dialPacket.deviceImgUrl, false)

        if (adapter == null) {
            viewBind.layoutSelect.visibility = View.GONE
        } else {
            viewBind.layoutSelect.visibility = View.VISIBLE
            viewBind.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            viewBind.recyclerView.adapter = adapter
        }

        resetStateView()

        viewBind.stateView.clickTrigger {
            if (!dfuViewModel.isDfuIng()) {
                PermissionHelper.requestBle(this) { granted ->
                    if (granted) {
                        val binFlag = adapter?.getSelectedItem()?.binFlag ?: 0
                        dfuViewModel.startDfu(FcDfuManager.DfuType.DIAL, Uri.parse(dialPacket.binUrl), binFlag)
                    }
                }
            }
        }

        lifecycle.launchRepeatOnStarted {
            launch {
                dfuViewModel.flowDfuStateProgress().collect {
                    when (it.state) {
                        FcDfuManager.DfuState.NONE, FcDfuManager.DfuState.DFU_FAIL, FcDfuManager.DfuState.DFU_SUCCESS -> {
                            resetStateView()
                            isCancelable = true
                        }
                        FcDfuManager.DfuState.DOWNLOAD_FILE -> {
                            viewBind.stateView.setText(R.string.ds_dfu_downloading)
                            isCancelable = false
                        }
                        FcDfuManager.DfuState.PREPARE_FILE, FcDfuManager.DfuState.PREPARE_DFU -> {
                            viewBind.stateView.setText(R.string.ds_dfu_preparing)
                            isCancelable = false
                        }
                        FcDfuManager.DfuState.DFU_ING -> {
                            viewBind.stateView.setText(R.string.ds_dfu_pushing)
                            isCancelable = false
                        }
                    }
                    viewBind.stateView.progress = it.progress
                }
            }
            launch {
                dfuViewModel.flowDfuEvent.collect {
                    when (it) {
                        is DfuViewModel.DfuEvent.OnSuccess -> {
                            promptToast.showSuccess(R.string.ds_push_success, intercept = true)
                            lifecycleScope.launchWhenStarted {
                                delay(2000)
                                dismiss()
                            }
                        }
                        is DfuViewModel.DfuEvent.OnFail -> {
                            promptToast.showDfuFail(requireContext(), it.error)
                        }
                    }
                }
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(viewBind.root)
            .setCancelable(true)
            .create()
    }

    private fun resetStateView() {
        viewBind.stateView.isEnabled = adapter?.hasSelectedItem() ?: true
        viewBind.stateView.text = getString(R.string.ds_push_start, fileSizeStr(dialPacket.binSize))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

}