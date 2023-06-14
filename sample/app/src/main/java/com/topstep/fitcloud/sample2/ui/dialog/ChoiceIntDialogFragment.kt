package com.topstep.fitcloud.sample2.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.utils.PARCEL_ARGS
import com.topstep.fitcloud.sample2.utils.getParcelableCompat

class ChoiceIntDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args: Arguments = requireArguments().getParcelableCompat(PARCEL_ARGS)!!
        var checkItem = 0
        if (args.values == null) {
            checkItem = args.selectValue
        } else {
            for (i in args.items.indices) {
                if (args.selectValue == args.values[i]) {
                    checkItem = i
                }
            }
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(args.title)
            .setSingleChoiceItems(args.items, checkItem) { dialog, position ->
                (parentFragment as? Listener)?.onDialogChoiceInt(
                    tag, if (args.values == null) {
                        position
                    } else {
                        args.values[position]
                    }
                )
                dialog.dismiss()
            }
            .create()
    }

    interface Listener {
        fun onDialogChoiceInt(tag: String?, selectValue: Int)
    }

    @Keep
    private class Arguments(
        /**
         * 显示的item
         */
        val items: Array<String>,
        /**
         * item对应的值，
         * 必须为null或者和[items]长度一样。
         * 当为null时，表示[selectValue]为选择的position
         */
        val values: IntArray? = null,
        val selectValue: Int,
        val title: String?
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.createStringArray()!!,
            parcel.createIntArray(),
            parcel.readInt(),
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeStringArray(items)
            parcel.writeIntArray(values)
            parcel.writeInt(selectValue)
            parcel.writeString(title)
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
        fun newInstance(items: Array<String>, values: IntArray? = null, selectValue: Int, title: String?): ChoiceIntDialogFragment {
            val fragment = ChoiceIntDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(PARCEL_ARGS, Arguments(items, values, selectValue, title))
            }
            return fragment
        }
    }
}