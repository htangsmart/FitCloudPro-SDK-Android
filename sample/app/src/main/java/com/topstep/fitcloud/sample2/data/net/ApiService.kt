package com.topstep.fitcloud.sample2.data.net

import androidx.annotation.VisibleForTesting
import com.squareup.moshi.Moshi
import com.topstep.fitcloud.sample2.data.bean.DialCustomStyleBean
import com.topstep.fitcloud.sample2.data.bean.DialPacketComplexBean
import com.topstep.fitcloud.sample2.data.bean.VersionBean
import com.topstep.fitcloud.sample2.model.dial.DialPacket
import com.topstep.fitcloud.sample2.model.game.push.GamePacket
import com.topstep.fitcloud.sample2.model.sport.push.SportPacket
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    companion object {
        const val URL_CHECK_VERSION = "/public/checkVersion/v2" //检查更新新接口
        const val URL_DIAL_PACKET_LIST = "/public/dial/list" //查询符合的表盘列表
        const val URL_DIAL_PACKET_FIND = "/public/dial/get" //查询符合的表盘列表
        const val URL_DIAL_CUSTOM = "/public/dial/custom" //获取自定义表盘列表
        const val URL_DIAL_CUSTOM_GUI = "/public/dial/customgui" //获取GUI自定义表盘列表
        const val URL_GAME_PACKET_LIST = "/public/game/list"
        const val URL_SPORT_PACKET_LIST = "/public/sportbin/list" //获取运动推送
    }

    @POST(URL_CHECK_VERSION)
    @FormUrlEncoded
    suspend fun checkVersion(
        @Field("hardwareInfo") hardwareInfo: String,
        @Field("uiVersion") uiVersion: String?
    ): ObjectNullable<VersionBean>

    @POST(URL_GAME_PACKET_LIST)
    @FormUrlEncoded
    suspend fun listGamePacket(
        @Field("hardwareInfo") hardwareInfo: String,
        @Field("lang") lang: String
    ): ListNullable<GamePacket>

    @POST(URL_SPORT_PACKET_LIST)
    @FormUrlEncoded
    suspend fun listSportPacket(
        @Field("hardwareInfo") hardwareInfo: String,
    ): ListNullable<SportPacket>

    @POST(URL_DIAL_PACKET_LIST)
    @FormUrlEncoded
    suspend fun listDialPacket(
        @Field("hardwareInfo") hardwareInfo: String,
        @Field("lcd") lcd: Int,
        @Field("toolVersion") toolVersion: String
    ): ListNullable<DialPacket>

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @POST(URL_DIAL_PACKET_FIND)
    @FormUrlEncoded
    suspend fun findDialPacket(
        @Field("data") data: String
    ): ListNullable<DialPacketComplexBean>

    @POST(URL_DIAL_CUSTOM)
    @FormUrlEncoded
    suspend fun dialCustom(
        @Field("lcd") lcd: Int,
        @Field("toolVersion") toolVersion: String
    ): ListNullable<DialCustomStyleBean>

    @POST(URL_DIAL_CUSTOM_GUI)
    @FormUrlEncoded
    suspend fun dialCustomGUI(
        @Field("lcd") lcd: Int,
        @Field("toolVersion") toolVersion: String
    ): ListNullable<DialPacketComplexBean>
}

suspend fun ApiService.findDialPacket(moshi: Moshi, dialNumbers: IntArray): ListNullable<DialPacketComplexBean> {
    return findDialPacket(moshi.adapter(IntArray::class.java).toJson(dialNumbers))
}