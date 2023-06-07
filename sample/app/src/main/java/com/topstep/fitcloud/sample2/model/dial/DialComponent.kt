package com.topstep.fitcloud.sample2.model.dial

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
data class DialComponent(

    /**
     * The width of this component on the device
     */
    val width: Int,

    /**
     * The height of this component on the device
     */
    val height: Int,

    /**
     * The x-coordinate of this component on the device
     */
    val positionX: Int,

    /**
     * The y-coordinate of this component on the device
     */
    val positionY: Int,

    /**
     * The current style of this component
     */
    private val _styleCurrent: Int,

    /**
     * How many styles are there for this component
     */
    val styleCount: Int,

    /**
     * Image URLs for each style
     */
    val styleUrls: List<String>? = null
) : Parcelable {

    /**
     * To prevent mismatches between [styleUrls] and [_styleCurrent], rewrite this field
     */
    val styleCurrent: Int
        get() {
            return if (styleUrls == null || styleUrls.size < _styleCurrent) {
                0
            } else {
                _styleCurrent
            }
        }

    val styleCurrentUrl get() = styleUrls?.getOrNull(styleCurrent)

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.createStringArrayList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeInt(positionX)
        parcel.writeInt(positionY)
        parcel.writeInt(_styleCurrent)
        parcel.writeInt(styleCount)
        parcel.writeStringList(styleUrls)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DialComponent> {
        override fun createFromParcel(parcel: Parcel): DialComponent {
            return DialComponent(parcel)
        }

        override fun newArray(size: Int): Array<DialComponent?> {
            return arrayOfNulls(size)
        }
    }

}