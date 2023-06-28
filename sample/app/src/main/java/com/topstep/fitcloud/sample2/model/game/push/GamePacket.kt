package com.topstep.fitcloud.sample2.model.game.push

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GamePacket(
    /**
     * 游戏类型
     */
    val type: Int,

    /**
     * 游戏名称（根据lang参数，返回中文或英文）
     */
    val name: String,

    /**
     * 游戏攻略（根据lang参数，返回中文或英文）
     */
    val description: String? = null,

    /**
     * 下载次数
     */
    val downloadCount: Int = 0,

    /**
     * 游戏图标
     */
    val imgUrl: String? = null,

    /**
     * 游戏皮肤
     */
    val gameSkins: List<GameSkin>? = null,
)