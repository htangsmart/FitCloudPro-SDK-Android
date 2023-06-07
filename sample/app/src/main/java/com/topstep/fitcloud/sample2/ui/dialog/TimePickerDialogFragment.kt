package com.topstep.fitcloud.sample2.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDialogFragment
import com.github.kilnn.wheellayout.TwoWheelLayout
import com.github.kilnn.wheellayout.WheelIntConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.utils.PARCEL_ARGS
import com.topstep.fitcloud.sample2.utils.getParcelableCompat

class TimePickerDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args: Arguments = requireArguments().getParcelableCompat(PARCEL_ARGS)!!

        val layout = TwoWheelLayout(requireContext())
        layout.setConfig(
            WheelIntConfig(0, 23, true, null, null),
            WheelIntConfig(0, 59, true, null, null),
        )
        layout.setValue(args.timeMinute / 60, args.timeMinute % 60)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(args.title)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val values = layout.getValue()
                (parentFragment as? Listener)?.onDialogTimePicker(tag, values[0] * 60 + values[1])
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    interface Listener {
        fun onDialogTimePicker(tag: String?, timeMinute: Int)
    }

    @Keep
    private class Arguments(
        val timeMinute: Int,
        val title: String?
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(timeMinute)
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
        fun newInstance(timeMinute: Int, title: String?): TimePickerDialogFragment {
            val fragment = TimePickerDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(PARCEL_ARGS, Arguments(timeMinute, title))
            }
            return fragment
        }
    }

}