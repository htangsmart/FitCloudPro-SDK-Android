package com.topstep.fitcloud.sample2.ui.device.contacts

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.ui.base.Fail
import com.topstep.fitcloud.sample2.ui.base.Loading
import com.topstep.fitcloud.sample2.ui.base.Success
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted

class SetContactsDialogFragment : AppCompatDialogFragment() {

    private val viewModel: ContactsViewModel by viewModels({ requireParentFragment() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.ds_contacts)
            .setMessage(R.string.tip_setting_loading)
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                viewModel.setContactsAction.cancel()
            }
            .setPositiveButton(R.string.action_retry, null)
            .create()
        lifecycle.launchRepeatOnStarted {
            viewModel.setContactsAction.flowState.collect {
                when (it) {
                    is Loading -> {
                        dialog.setMessage(getString(R.string.tip_setting_loading))
                        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        positiveButton.isVisible = false
                    }
                    is Fail -> {
                        dialog.setMessage(getString(R.string.tip_setting_fail))
                        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        positiveButton.isVisible = true
                        positiveButton.setOnClickListener {
                            viewModel.setContactsAction.retry()
                        }
                    }
                    is Success -> {
                        dialog.setMessage(getString(R.string.tip_setting_success))
                        viewModel.sendNavigateUpEvent()
                    }
                    else -> {}
                }
            }
        }
        return dialog
    }
}