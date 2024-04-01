package com.topstep.fitcloud.sample2.ui.device.card

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sdk.v2.model.settings.FcCustomCard

class CustomCardAddDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val card: FcCustomCard? = arguments?.getParcelable(EXTRA_CARD)

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_card_add, null)
        val editTitle = view.findViewById<EditText>(R.id.edit_title)
        val editContent = view.findViewById<EditText>(R.id.edit_content)

        if (card != null) {
            editTitle.setText(card.title)
            editContent.setText(card.content)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .apply {
                if (card == null) {
                    setTitle("Add card")
                } else {
                    setTitle("Edit card")
                }
            }
            .setView(view)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val title = editTitle.text.trim().toString()
                val content = editContent.text.trim().toString()

                if (card == null) {
                    (parentFragment as? Listener)?.onDialogAdd(title, content)
                } else {
                    (parentFragment as? Listener)?.onDialogEdit(
                        FcCustomCard(
                            id = card.id,
                            title = title,
                            content = content,
                        ),
                        requireArguments().getInt(EXTRA_POSITION)
                    )
                }
            }
            .create()
    }

    interface Listener {
        fun onDialogAdd(title: String, content: String)
        fun onDialogEdit(card: FcCustomCard, position: Int)
    }

    companion object {
        private const val EXTRA_CARD = "card"
        private const val EXTRA_POSITION = "position"

        /**
         * @param card null新增，非null为编辑
         */
        fun newInstance(card: FcCustomCard? = null, position: Int = 0): CustomCardAddDialogFragment {
            val fragment = CustomCardAddDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(EXTRA_CARD, card)
                putInt(EXTRA_POSITION, position)
            }
            return fragment
        }
    }

}