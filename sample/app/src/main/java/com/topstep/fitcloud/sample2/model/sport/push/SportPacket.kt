package com.topstep.fitcloud.sample2.model.sport.push

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class SportPacket(
    val sportUiType: Int,
    val iconUrl: String? = null,
    val binUrl: String,
    val sportUiName: String,
    val sportUiNameCn: String,
    val createTime: Long = 0,
    val binSize: Long = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(sportUiType)
        parcel.writeString(iconUrl)
        parcel.writeString(binUrl)
        parcel.writeString(sportUiName)
        parcel.writeString(sportUiNameCn)
        parcel.writeLong(createTime)
        parcel.writeLong(binSize)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SportPacket> {
        override fun createFromParcel(parcel: Parcel): SportPacket {
            return SportPacket(parcel)
        }

        override fun newArray(size: Int): Array<SportPacket?> {
            return arrayOfNulls(size)
        }
    }

}