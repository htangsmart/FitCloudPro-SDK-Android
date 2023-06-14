package com.topstep.fitcloud.sample2.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDialogFragment
import com.github.kilnn.wheellayout.OneWheelLayout
import com.github.kilnn.wheellayout.WheelIntConfig
import com.github.kilnn.wheellayout.WheelIntFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.utils.PARCEL_ARGS
import com.topstep.fitcloud.sample2.utils.getParcelableCompat

class SelectIntDialogFragment : AppCompatDialogFragment() {

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as Listener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args: Arguments = requireArguments().getParcelableCompat(PARCEL_ARGS)!!

        val layout = OneWheelLayout(requireContext())
        layout.setConfig(WheelIntConfig(args.min, args.max, false, args.des, object : WheelIntFormatter {
            override fun format(index: Int, value: Int): String {
                return listener?.dialogSelectIntFormat(tag, value * args.multiples) ?: FormatterUtil.intStr(value * args.multiples)
            }
        }))
        layout.setValue(args.value / args.multiples)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(args.title)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener?.onDialogSelectInt(tag, layout.getValue() * args.multiples)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    interface Listener {
        fun onDialogSelectInt(tag: String?, selectValue: Int)
        fun dialogSelectIntFormat(tag: String?, value: Int): String {
            return FormatterUtil.intStr(value)
        }
    }

    @Keep
    private class Arguments(
        val min: Int,
        val max: Int,
        val multiples: Int,
        val value: Int,
        val title: String?,
        val des: String?
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(min)
            parcel.writeInt(max)
            parcel.writeInt(multiples)
            parcel.writeInt(value)
            parcel.writeString(title)
            parcel.writeString(des)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Arguments> {
            override fun createFromParcel(parcel: Parcel): Arguments {
                return Arguments(parcel)
            }

            override fun newArray(size: Int): Array<Arguments?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        fun newInstance(
            min: Int, max: Int, multiples: Int = 1, value: Int, title: String?, des: String? = null
        ): SelectIntDialogFragment {
            val fragment = SelectIntDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(
                    PARCEL_ARGS, Arguments(
                        min = min,
                        max = max,
                        multiples = multiples,
                        value = value,
                        title = title,
                        des = des
                    )
                )
            }
            return fragment
        }
    }
}