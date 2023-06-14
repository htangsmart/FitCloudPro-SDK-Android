package com.topstep.fitcloud.sample2.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDialogFragment
import com.github.kilnn.wheellayout.DateWheelLayout
import com.github.kilnn.wheellayout.toDate
import com.github.kilnn.wheellayout.toIntArray
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.utils.PARCEL_ARGS
import com.topstep.fitcloud.sample2.utils.getParcelableCompat
import java.util.*

class DatePickerDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args: Arguments = requireArguments().getParcelableCompat(PARCEL_ARGS)!!

        val layout = DateWheelLayout(requireContext())
        layout.setConfig(
            start = if (args.start != null) {
                Date(args.start)
            } else {
                null
            },
            end = if (args.end != null) {
                Date(args.end)
            } else {
                null
            },
            yearDes = getString(R.string.unit_year),
            monthDes = getString(R.string.unit_month),
            dayDes = getString(R.string.unit_day),
            formatter = FormatterUtil.getGenericWheelIntFormatter()
        )
        if (args.value != null) {
            val arrays = Date(args.value).toIntArray()
            layout.setDate(arrays[0], arrays[1], arrays[2])
        }
        if (args.hideDay) {
            layout.getChildAt(2).visibility = View.GONE
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(args.title)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                (parentFragment as? Listener)?.onDialogDatePicker(tag, layout.getDate().toDate())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    interface Listener {
        fun onDialogDatePicker(tag: String?, date: Date)
    }

    @Keep
    private class Arguments(
        val start: Long?,
        val end: Long?,
        val value: Long?,
        val title: String?,
        val hideDay: Boolean
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readString(),
            parcel.readByte() != 0.toByte()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeValue(start)
            parcel.writeValue(end)
            parcel.writeValue(value)
            parcel.writeString(title)
            parcel.writeByte(if (hideDay) 1 else 0)
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
        fun newInstance(start: Date?, end: Date?, value: Date?, title: String?, hideDay: Boolean = false): DatePickerDialogFragment {
            val fragment = DatePickerDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(
                    PARCEL_ARGS, Arguments(
                        start?.time,
                        end?.time,
                        value?.time,
                        title,
                        hideDay
                    )
                )
            }
            return fragment
        }
    }

}