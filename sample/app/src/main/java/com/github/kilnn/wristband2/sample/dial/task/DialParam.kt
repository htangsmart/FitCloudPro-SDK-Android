package com.github.kilnn.wristband2.sample.dial.task


import android.os.Parcel
import android.os.Parcelable
import com.htsmart.wristband2.bean.DialSubBinInfo
import com.htsmart.wristband2.dial.DialDrawer

data class DialParam(

    /**
     * 硬件信息
     */
    val hardwareInfo: String,

    /**
     * 是否是GUI协议
     */
    val isGUI: Boolean,

    /**
     * 表盘的lcd，参考[DialDrawer.Shape]
     */
    val lcd: Int,

    /**
     * 打包此表盘的工具版本
     */
    val toolVersion: String,

    /**
     * 手环当前显示的表盘编号
     */
    val currentDialNum: Int,

    /**
     * 手环当前显示的表盘编号，对应在 [dialBinParams] 的位置
     */
    val currentDialPosition: Int,

    /**
     * 多表盘信息，如果手环支持的话。
     */
    val dialBinParams: List<DialBinParam>? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.createTypedArrayList(DialBinParam)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(hardwareInfo)
        parcel.writeByte(if (isGUI) 1 else 0)
        parcel.writeInt(lcd)
        parcel.writeString(toolVersion)
        parcel.writeInt(currentDialNum)
        parcel.writeInt(currentDialPosition)
        parcel.writeTypedList(dialBinParams)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DialParam> {
        override fun createFromParcel(parcel: Parcel): DialParam {
            return DialParam(parcel)
        }

        override fun newArray(size: Int): Array<DialParam?> {
            return arrayOfNulls(size)
        }
    }

    /**
     * dialBinParams中存放的是所有多表盘的信息，但是当表盘类型是 [DialSubBinInfo.TYPE_NONE] 时，是不可以被升级覆盖的，
     * 也就是说不可以出现在“选择升级位置”的对话框中。
     * 这里就判断是否可以被选择
     */
    fun isSelectableDialBinParams(): Boolean {
        return dialBinParams?.count {
            it.dialType != DialSubBinInfo.TYPE_NONE
        } ?: 0 > 0
    }

    /**
     * 如果 [isSelectableDialBinParams] 为true，使用这个方法可以获取到所有可选择的表盘
     */
    fun filterSelectableDialBinParams(): List<DialBinParam> {
        return dialBinParams?.filter {
            it.dialType != DialSubBinInfo.TYPE_NONE
        } ?: emptyList()
    }
}