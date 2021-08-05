package com.github.kilnn.wristband2.sample.dial.task

import android.os.Parcel
import android.os.Parcelable
import com.htsmart.wristband2.bean.DialSubBinInfo
import com.htsmart.wristband2.bean.WristbandVersion
import com.htsmart.wristband2.dfu.DfuManager

data class DialBinParam(
    /**
     * 表盘类型
     * [DialSubBinInfo.TYPE_NONE] 表盘不可被覆盖
     * [DialSubBinInfo.TYPE_NORMAL] 正常的表盘
     * [DialSubBinInfo.TYPE_CUSTOM_STYLE_WHITE] 旧协议"white"样式的自定义表盘
     * [DialSubBinInfo.TYPE_CUSTOM_STYLE_BLACK] 旧协议"black"样式的自定义表盘
     * [DialSubBinInfo.TYPE_CUSTOM_STYLE_YELLOW] 旧协议"yellow"样式的自定义表盘
     * [DialSubBinInfo.TYPE_CUSTOM_STYLE_GREEN] 旧协议"green"样式的自定义表盘
     * [DialSubBinInfo.TYPE_CUSTOM_STYLE_GRAY] 旧协议"gray"样式的自定义表盘
     */
    val dialType: Byte,

    /**
     * 表盘编号
     */
    val dialNum: Int,

    /**
     * 表盘bin文件版本
     */
    val binVersion: Int,

    /**
     * 用于升级表盘 [DfuManager.upgradeDial]，只有当手环支持多表盘推送 [WristbandVersion.isExtDialMultiple] 时才有效
     */
    val binFlag: Byte,

    /**
     * 表盘空间，单位kb
     */
    val dialSpace: Int,

    /**
     * 表盘的展示图，通常为纯表面形式，不带实际手环外壳。
     */
    val imgUrl: String? = null,

    /**
     * 表盘的展示图，通常带实际手环外壳。
     */
    val deviceImgUrl: String? = null,

    /**
     * 表盘组件的预览图
     */
    val previewImgUrl: String? = null,
    /**
     * 表盘组件，只有当手环支持组件 [WristbandVersion.isExtDialComponent] 时才有效
     */
    val components: List<DialComponentParam>? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(DialComponentParam)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(dialType)
        parcel.writeInt(dialNum)
        parcel.writeInt(binVersion)
        parcel.writeByte(binFlag)
        parcel.writeInt(dialSpace)
        parcel.writeString(imgUrl)
        parcel.writeString(deviceImgUrl)
        parcel.writeString(previewImgUrl)
        parcel.writeTypedList(components)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DialBinParam> {
        override fun createFromParcel(parcel: Parcel): DialBinParam {
            return DialBinParam(parcel)
        }

        override fun newArray(size: Int): Array<DialBinParam?> {
            return arrayOfNulls(size)
        }
    }

}