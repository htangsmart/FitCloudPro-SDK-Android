package com.topstep.fitcloud.sample2.ui.combine

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.utils.ShareHelper
import java.io.File

class LogShareDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val files = requireArguments().getStringArray(EXTRA_ARGS)!!
        val names = files.map {
            File(it).name
        }.toTypedArray()
        return MaterialAlertDialogBuilder(requireContext())
            .setItems(names) { _, which ->
                ShareHelper.shareFile(requireContext(), File(files[which]), "*/*")
            }
            .create()
    }

    companion object {
        private const val EXTRA_ARGS = "extraArgs"
        fun newInstance(files: List<File>): LogShareDialogFragment {
            val arguments = Bundle()
            arguments.putStringArray(EXTRA_ARGS, files.map {
                it.path
            }.toTypedArray())
            val fragment = LogShareDialogFragment()
            fragment.arguments = arguments
            return fragment
        }
    }
}