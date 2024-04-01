package com.topstep.fitcloud.sample2.ui.device.contacts

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import timber.log.Timber

class RandomContactsDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val limit = requireArguments().getInt(EXTRA_LIMIT)
        val edit = EditText(requireContext())
        edit.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED
        edit.setText("$limit")
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.ds_contacts_random_tips)
            .setView(edit)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                try {
                    val size = edit.text.trim().toString().toInt()
                    (parentFragment as? Listener)?.onDialogRandom(size.coerceAtMost(limit))
                } catch (e: Exception) {
                    Timber.w(e)
                }
            }
            .create()

    }

    companion object {
        private const val EXTRA_LIMIT = "limit"
        fun newInstance(limit: Int): RandomContactsDialogFragment {
            val fragment = RandomContactsDialogFragment()
            fragment.arguments = Bundle().apply {
                putInt(EXTRA_LIMIT, limit)
            }
            return fragment
        }
    }

    interface Listener {
        fun onDialogRandom(size: Int)
    }
}