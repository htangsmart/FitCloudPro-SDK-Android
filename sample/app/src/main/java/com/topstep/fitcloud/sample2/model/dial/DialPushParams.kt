package com.topstep.fitcloud.sample2.model.dial

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.topstep.fitcloud.sample2.utils.readParcelableCompat
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.settings.dial.FcDialSpace
import com.topstep.fitcloud.sdk.v2.model.settings.dial.FcShape

/**
 * Dial push required params
 */
@Keep
data class DialPushParams(

    val hardwareInfo: String,

    /**
     * Is [FcDeviceInfo.Feature.GUI] support
     */
    val isSupportGUI: Boolean,

    /**
     * The device lcd
     */
    val lcd: Int,

    /**
     * The device shape for [lcd].
     */
    val shape: FcShape,

    /**
     * The version of the dial tool supported by the device.
     * The newly pushed dial file needs to be the same or smaller than this version
     */
    val toolVersion: String,

    /**
     * The dial number currently displayed on the device
     */
    val currentDialNum: Int,

    /**
     * The dial number currently displayed on the device corresponds to the position of [dialSpacePackets]
     * If [dialSpacePackets] is null or empty, this field is meaningless.
     */
    val currentPosition: Int,

    /**
     * Multiple dial spaces on the device.
     * If is null or empty, it indicates that the device does not support multiple dials.
     */
    val dialSpacePackets: List<DialSpacePacket>? = null,
) : Parcelable {

    /**
     * Obtain all pushable dial space
     */
    fun filterPushableSpacePackets(): List<DialSpacePacket> {
        return dialSpacePackets?.filter {
            it.dialType != FcDialSpace.DIAL_TYPE_NONE
        } ?: emptyList()
    }

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readParcelableCompat(FcShape::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.createTypedArrayList(DialSpacePacket)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(hardwareInfo)
        parcel.writeByte(if (isSupportGUI) 1 else 0)
        parcel.writeInt(lcd)
        parcel.writeParcelable(shape, flags)
        parcel.writeString(toolVersion)
        parcel.writeInt(currentDialNum)
        parcel.writeInt(currentPosition)
        parcel.writeTypedList(dialSpacePackets)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DialPushParams> {
        override fun createFromParcel(parcel: Parcel): DialPushParams {
            return DialPushParams(parcel)
        }

        override fun newArray(size: Int): Array<DialPushParams?> {
            return arrayOfNulls(size)
        }
    }

}