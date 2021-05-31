package com.github.kilnn.wristband2.sample.dial.task

import android.os.Parcel
import android.os.Parcelable

data class DialComponentParam(

    /**
     * 这个组件在手环上的宽度
     */
    val width: Int,

    /**
     * 这个组件在手环上的高度
     */
    val height: Int,

    /**
     * 这个组件在手环上x坐标
     */
    val positionX: Int,

    /**
     * 这个组件在手环上的y坐标
     */
    val positionY: Int,

    /**
     * 这个组件当前的样式
     */
    private var _styleCurrent: Int,//这里设置为var，在编辑组件里会改变这个值

    /**
     * 这个组件一共有几个样式
     */
    val styleCount: Int,

    /**
     * 每个样式的图片url
     */
    val styleUrls: List<String>? = null
) : Parcelable {

    /**
     *   防止url和selects不匹配，所以重写这个字段
     */
    var styleCurrent: Int
        get() {
            if (styleUrls == null || styleUrls.size < _styleCurrent) {
                _styleCurrent = 0
            }
            return _styleCurrent
        }
        set(value) {
            _styleCurrent = value
        }

    fun getStyleCurrentUrl(): String? {
        return styleUrls?.getOrNull(styleCurrent)
    }

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

    companion object CREATOR : Parcelable.Creator<DialComponentParam> {
        override fun createFromParcel(parcel: Parcel): DialComponentParam {
            return DialComponentParam(parcel)
        }

        override fun newArray(size: Int): Array<DialComponentParam?> {
            return arrayOfNulls(size)
        }
    }
}