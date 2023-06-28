package com.topstep.fitcloud.sample2.ui.device.sport.push

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.DialogSportDfuBinding
import com.topstep.fitcloud.sample2.model.sport.push.SportPacket
import com.topstep.fitcloud.sample2.ui.device.DfuViewModel
import com.topstep.fitcloud.sample2.ui.device.showDfuFail
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SportDfuDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val EXTRA_NAME = "name"
        private const val EXTRA_PACKET = "packet"
        private const val EXTRA_BIN_FLAG = "bin_flag"

        fun newInstance(name: String, packet: SportPacket, binFlag: Byte): SportDfuDialogFragment {
            val fragment = SportDfuDialogFragment()
            fragment.arguments = Bundle().apply {
                putString(EXTRA_NAME, name)
                putParcelable(EXTRA_PACKET, packet)
                putByte(EXTRA_BIN_FLAG, binFlag)
            }
            return fragment
        }
    }

    private lateinit var name: String
    private lateinit var packet: SportPacket
    private var binFlag: Byte = 0

    private var _viewBind: DialogSportDfuBinding? = null
    private val viewBind get() = _viewBind!!

    private val dfuViewModel: DfuViewModel by viewModels({ requireParentFragment() })
    private val toast by promptToast()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            name = it.getString(EXTRA_NAME)!!
            packet = it.getParcelableCompat(EXTRA_PACKET)!!
            binFlag = it.getByte(EXTRA_BIN_FLAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogSportDfuBinding.inflate(LayoutInflater.from(context))

        viewBind.tvName.text = name

        glideShowImage(viewBind.imgView, packet.iconUrl, false)

        viewBind.stateView.text = getString(R.string.ds_push_start, fileSizeStr(packet.binSize))

        viewBind.stateView.clickTrigger {
            if (!dfuViewModel.isDfuIng()) {
                PermissionHelper.requestBle(this) { granted ->
                    if (granted) {
                        dfuViewModel.startDfu(FcDfuManager.DfuType.SPORT, Uri.parse(packet.binUrl), binFlag)
                    }
                }
            }
        }

        lifecycle.launchRepeatOnStarted {
            launch {
                dfuViewModel.flowDfuStateProgress().collect {
                    when (it.state) {
                        FcDfuManager.DfuState.NONE, FcDfuManager.DfuState.DFU_FAIL, FcDfuManager.DfuState.DFU_SUCCESS -> {
                            viewBind.stateView.text = getString(R.string.ds_push_start, fileSizeStr(packet.binSize))
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
                            toast.showSuccess(R.string.ds_push_success, intercept = true)
                            lifecycleScope.launchWhenStarted {
                                delay(2000)
                                dismiss()
                            }
                        }
                        is DfuViewModel.DfuEvent.OnFail -> {
                            toast.showDfuFail(requireContext(), it.error)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }
}