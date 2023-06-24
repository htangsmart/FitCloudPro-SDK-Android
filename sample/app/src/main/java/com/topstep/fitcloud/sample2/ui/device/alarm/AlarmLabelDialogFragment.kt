package com.topstep.fitcloud.sample2.ui.device.alarm

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import kotlinx.coroutines.delay

/**
 * Set alarm label and limit 32 bytes
 */
class AlarmLabelDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val label = (parentFragment as? Listener)?.dialogGetAlarmLabel()

        val layout = LayoutInflater.from(requireContext()).inflate(R.layout.edit_text_alert_dialog, null)
        val editText: EditText = layout.findViewById(R.id.edit)
        editText.setText(label)

        val filter: InputFilter = object : InputFilter {
            private val maxLen = 32
            override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
                var keep = maxLen - (dest.toString().toByteArray().size - dest.subSequence(dstart, dend).toString().toByteArray().size)
                return if (keep <= 0) {
                    ""
                } else if (keep >= source.subSequence(start, end).toString().toByteArray().size) {
                    null // keep original
                } else {
                    val tempChars = CharArray(1)
                    for (i in start until end) {
                        tempChars[0] = source[i]
                        keep -= String(tempChars).toByteArray().size
                        if (keep <= 0) {
                            return if (keep == 0) {
                                source.subSequence(start, i + 1)
                            } else {
                                if (i == start) {
                                    ""
                                } else {
                                    source.subSequence(start, i)
                                }
                            }
                        }
                    }
                    null
                }
            }
        }
        editText.filters = arrayOf(filter)

        lifecycleScope.launchWhenResumed {
            editText.requestFocus()
            delay(250)
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editText, 0)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.ds_alarm_label)
            .setView(layout)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                (parentFragment as? Listener)?.dialogSetAlarmLabel(editText.text.toString())
            }
            .create()
    }

    interface Listener {
        fun dialogGetAlarmLabel(): String?
        fun dialogSetAlarmLabel(label: String?)
    }
}