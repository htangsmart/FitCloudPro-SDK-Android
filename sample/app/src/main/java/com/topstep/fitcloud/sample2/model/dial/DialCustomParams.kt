package com.topstep.fitcloud.sample2.model.dial

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.topstep.fitcloud.sample2.utils.readParcelableCompat

data class DialCustomParams(
    /**
     * Default background image Uri
     */
    val defaultBackgroundUri: Uri,

    /**
     * Style List
     */
    val styles: List<Style>
) {

    data class Style(
        val styleIndex: Int,
        val styleUri: Uri,
        /**
         * The style is designed based on what width.
         * It is used to know the zoom ratio of the style image so that it can be displayed on the preview in a reasonable size
         */
        val styleBaseOnWidth: Int,
        val binUrl: String,
        val binSize: Long
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readParcelableCompat(Uri::class.java.classLoader)!!,
            parcel.readInt(),
            parcel.readString()!!,
            parcel.readLong()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(styleIndex)
            parcel.writeParcelable(styleUri, flags)
            parcel.writeInt(styleBaseOnWidth)
            parcel.writeString(binUrl)
            parcel.writeLong(binSize)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Style> {
            override fun createFromParcel(parcel: Parcel): Style {
                return Style(parcel)
            }

            override fun newArray(size: Int): Array<Style?> {
                return arrayOfNulls(size)
            }
        }

    }
}