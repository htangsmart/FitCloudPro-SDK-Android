package com.topstep.fitcloud.sample2.model.game.push

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class GameSkin(
    /**
     * 游戏皮肤编号
     */
    val skinNum: Int,

    /**
     * Bin文件下载地址
     */
    val binUrl: String,

    /**
     * Bin文件大小
     */
    val binSize: Long = 0,

    /**
     * 皮肤图片的url
     */
    val imgUrl: String? = null,

    /**
     * 在本地是否存在
     */
    var existLocally: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(skinNum)
        parcel.writeString(binUrl)
        parcel.writeLong(binSize)
        parcel.writeString(imgUrl)
        parcel.writeByte(if (existLocally) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GameSkin> {
        override fun createFromParcel(parcel: Parcel): GameSkin {
            return GameSkin(parcel)
        }

        override fun newArray(size: Int): Array<GameSkin?> {
            return arrayOfNulls(size)
        }
    }

}