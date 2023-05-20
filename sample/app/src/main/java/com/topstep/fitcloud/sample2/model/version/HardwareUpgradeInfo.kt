package com.topstep.fitcloud.sample2.model.version

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
data class HardwareUpgradeInfo(
    /**
     * New version of hardwareInfo
     */
    val hardwareInfo: String,

    /**
     * Update information
     */
    val remark: String?,

    /**
     * Download url
     */
    val url: String,

    /**
     * Hardware upgrade type
     */
    val type: HardwareType,

    /**
     * [url] file size(unit bytes)。
     */
    val size: Long,

    /**
     * Whether to force upgrade
     */
    val isForce: Boolean,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()!!,
        HardwareType.fromCode(parcel.readString())!!,
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(hardwareInfo)
        parcel.writeString(remark)
        parcel.writeString(url)
        parcel.writeString(type.code)
        parcel.writeLong(size)
        parcel.writeByte(if (isForce) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HardwareUpgradeInfo> {
        override fun createFromParcel(parcel: Parcel): HardwareUpgradeInfo {
            return HardwareUpgradeInfo(parcel)
        }

        override fun newArray(size: Int): Array<HardwareUpgradeInfo?> {
            return arrayOfNulls(size)
        }
    }

}