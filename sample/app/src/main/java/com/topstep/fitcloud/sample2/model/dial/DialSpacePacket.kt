package com.topstep.fitcloud.sample2.model.dial

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import com.topstep.fitcloud.sdk.v2.model.settings.dial.FcDialSpace

/**
 * Combine [DialPacket] and [FcDialSpace]
 */
@Keep
data class DialSpacePacket(
    val spaceIndex: Int,
    /**
     * Dial Type
     * [FcDialSpace.DIAL_TYPE_NONE]
     * [FcDialSpace.DIAL_TYPE_NORMAL]
     * [FcDialSpace.DIAL_TYPE_CUSTOM_STYLE_WHITE]
     * [FcDialSpace.DIAL_TYPE_CUSTOM_STYLE_BLACK]
     * [FcDialSpace.DIAL_TYPE_CUSTOM_STYLE_YELLOW]
     * [FcDialSpace.DIAL_TYPE_CUSTOM_STYLE_GREEN]
     * [FcDialSpace.DIAL_TYPE_CUSTOM_STYLE_GRAY]
     */
    val dialType: Byte,

    /**
     * Dial number
     */
    val dialNum: Int,

    /**
     * Dial bin version
     */
    val dialBinVersion: Int,

    /**
     * Use for dfu process
     *
     * See also [FcDfuManager.start]
     */
    val binFlag: Byte,

    /**
     * The space size(unit KB), the newly pushed bin file cannot exceed it.
     */
    val spaceSize: Int,

    /**
     * Dial image url
     */
    val imgUrl: String? = null,

    /**
     * Image url of dial with device casing
     */
    val deviceImgUrl: String? = null,

    /**
     * Component preview background image
     */
    val previewImgUrl: String? = null,

    /**
     * The dial component is only effective when the device supports the component
     */
    val components: List<DialComponent>? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(DialComponent)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(spaceIndex)
        parcel.writeByte(dialType)
        parcel.writeInt(dialNum)
        parcel.writeInt(dialBinVersion)
        parcel.writeByte(binFlag)
        parcel.writeInt(spaceSize)
        parcel.writeString(imgUrl)
        parcel.writeString(deviceImgUrl)
        parcel.writeString(previewImgUrl)
        parcel.writeTypedList(components)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DialSpacePacket> {
        override fun createFromParcel(parcel: Parcel): DialSpacePacket {
            return DialSpacePacket(parcel)
        }

        override fun newArray(size: Int): Array<DialSpacePacket?> {
            return arrayOfNulls(size)
        }
    }

}
