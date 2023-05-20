package com.topstep.fitcloud.sample2.ui.device.logo

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.DialogLogoDfuBinding
import com.topstep.fitcloud.sample2.ui.device.DfuViewModel
import com.topstep.fitcloud.sample2.ui.device.showDfuFail
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.promptToast
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class LogoDfuDialogFragment : AppCompatDialogFragment() {

    private val dfuViewModel: DfuViewModel by viewModels({ requireParentFragment() })
    private val promptToast by promptToast()

    private var _viewBind: DialogLogoDfuBinding? = null
    private val viewBind get() = _viewBind!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogLogoDfuBinding.inflate(LayoutInflater.from(context))

        lifecycle.launchRepeatOnStarted {
            launch {
                dfuViewModel.flowDfuStateProgress().collect {
                    when (it.state) {
                        FcDfuManager.DfuState.NONE, FcDfuManager.DfuState.DFU_FAIL, FcDfuManager.DfuState.DFU_SUCCESS -> {
                            isCancelable = true
                        }
                        FcDfuManager.DfuState.DOWNLOAD_FILE -> {
                            viewBind.tvTitle.setText(R.string.ds_dfu_downloading)
                            isCancelable = false
                        }
                        FcDfuManager.DfuState.PREPARE_FILE, FcDfuManager.DfuState.PREPARE_DFU -> {
                            viewBind.tvTitle.setText(R.string.ds_dfu_preparing)
                            isCancelable = false
                        }
                        FcDfuManager.DfuState.DFU_ING -> {
                            viewBind.tvTitle.setText(R.string.ds_dfu_pushing)
                            isCancelable = false
                        }
                    }
                    viewBind.progressBar.progress = it.progress
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
                            lifecycleScope.launchWhenStarted {
                                delay(2000)
                                dismiss()
                            }
                        }
                    }
                }
            }
        }

        val file = File(requireArguments().getString(EXTRA_FILE)!!)
        dfuViewModel.startDfu(
            FcDfuManager.DfuType.UI,
            Uri.fromFile(file),
            0
        )

        return MaterialAlertDialogBuilder(requireContext())
            .setView(viewBind.root)
            .setCancelable(true)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

    companion object {

        private const val EXTRA_FILE = "file"

        fun newInstance(file: File): LogoDfuDialogFragment {
            val arguments = Bundle()
            arguments.putString(EXTRA_FILE, file.path)
            val fragment = LogoDfuDialogFragment()
            fragment.arguments = arguments
            return fragment
        }

    }
}