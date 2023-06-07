package com.topstep.fitcloud.sample2.model.dial

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class DialPacket(
    val dialNum: Int,

    val lcd: Int,

    val toolVersion: String,

    val binVersion: Int,

    /**
     * Dial image url
     */
    val imgUrl: String? = null,

    /**
     * Image url of dial with device casing
     */
    val deviceImgUrl: String? = null,

    /**
     * Bin file download url
     */
    val binUrl: String,

    /**
     * Dial name
     */
    val name: String? = null,

    /**
     * File size of [binUrl]
     */
    val binSize: Long = 0,

    /**
     * Download count
     */
    val downloadCount: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readLong(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(dialNum)
        parcel.writeInt(lcd)
        parcel.writeString(toolVersion)
        parcel.writeInt(binVersion)
        parcel.writeString(imgUrl)
        parcel.writeString(deviceImgUrl)
        parcel.writeString(binUrl)
        parcel.writeString(name)
        parcel.writeLong(binSize)
        parcel.writeInt(downloadCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DialPacket> {
        override fun createFromParcel(parcel: Parcel): DialPacket {
            return DialPacket(parcel)
        }

        override fun newArray(size: Int): Array<DialPacket?> {
            return arrayOfNulls(size)
        }
    }

}