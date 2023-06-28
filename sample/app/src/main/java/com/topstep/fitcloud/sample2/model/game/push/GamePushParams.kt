package com.topstep.fitcloud.sample2.model.game.push

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

data class GamePushParams(
    val remoteGamePackets: List<GamePacket>,
    val localGamePackets: List<GamePacket>,
    val pushableSpaceSkins: ArrayList<GameSpaceSkin>
)

@Keep
data class GameSpaceSkin(
    val type: Int,
    val skinNum: Int,
    val name: String?,//游戏名称
    val imgUrl: String?,//皮肤图片的url
    val binFlag: Byte,
    val spaceSize: Int,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
        parcel.writeInt(skinNum)
        parcel.writeString(name)
        parcel.writeString(imgUrl)
        parcel.writeByte(binFlag)
        parcel.writeInt(spaceSize)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GameSpaceSkin> {
        override fun createFromParcel(parcel: Parcel): GameSpaceSkin {
            return GameSpaceSkin(parcel)
        }

        override fun newArray(size: Int): Array<GameSpaceSkin?> {
            return arrayOfNulls(size)
        }
    }

}