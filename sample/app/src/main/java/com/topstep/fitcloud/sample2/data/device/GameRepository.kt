package com.topstep.fitcloud.sample2.data.device

import android.content.Context
import com.github.kilnn.tool.util.LocalUtil
import com.topstep.fitcloud.sample2.data.net.ApiService
import com.topstep.fitcloud.sample2.model.game.push.GamePacket
import com.topstep.fitcloud.sample2.model.game.push.GamePushParams
import com.topstep.fitcloud.sample2.model.game.push.GameSpaceSkin
import com.topstep.fitcloud.sdk.v2.model.settings.game.FcGameSpace
import kotlinx.coroutines.rx3.await

interface GameRepository {
    suspend fun getGamePushParams(): GamePushParams
}

internal class GameRepositoryImpl constructor(
    private val context: Context,
    private val deviceManager: DeviceManager,
    private val apiService: ApiService,
) : GameRepository {

    private var remoteCacheKey: RemoteCacheKey? = null
    private var remoteCache: List<GamePacket>? = null

    override suspend fun getGamePushParams(): GamePushParams {
        //硬件信息
        val hardwareInfo = deviceManager.configFeature.getDeviceInfo().toString()

        val remoteGamePackets = requestGamePackets(hardwareInfo)

        val gameSpaces = deviceManager.settingsFeature.requestGamePushInfo().await()

        val localGamePackets = ArrayList<GamePacket>(gameSpaces.size)
        val pushableSpaceSkins = ArrayList<GameSpaceSkin>(gameSpaces.size)

        for (space in gameSpaces) {
            var matchedName: String? = null
            var matchedImgUrl: String? = null
            for (packet in remoteGamePackets) {
                if (space.gameType == packet.type) {
                    localGamePackets.add(packet)//TODO 同类型游戏是否不重复添加
                    matchedName = packet.name

                    val gameSkins = packet.gameSkins
                    if (!gameSkins.isNullOrEmpty()) {
                        for (skin in gameSkins) {
                            if (space.skinNum == skin.skinNum) {
                                skin.existLocally = true
                                matchedImgUrl = skin.imgUrl
                                break
                            }
                        }
                    }

                    if (matchedImgUrl.isNullOrEmpty()) {
                        matchedImgUrl = packet.imgUrl
                    }
                    break
                }
            }
            pushableSpaceSkins.add(
                space.toGameSpaceSkin(
                    name = matchedName,
                    imgUrl = matchedImgUrl
                )
            )
        }

        return GamePushParams(remoteGamePackets, localGamePackets, pushableSpaceSkins)
    }

    private fun FcGameSpace.toGameSpaceSkin(name: String? = null, imgUrl: String? = null): GameSpaceSkin {
        return GameSpaceSkin(
            type = gameType,
            skinNum = skinNum,
            name = name,
            imgUrl = imgUrl,
            binFlag = binFlag,
            spaceSize = spaceSize
        )
    }

    private suspend fun requestGamePackets(hardwareInfo: String): List<GamePacket> {
        val lang = if (LocalUtil.isZhrCN(context)) "cn" else "en"
        val key = RemoteCacheKey(hardwareInfo, lang)
        val cache = remoteCache
        return if (cache != null && key == remoteCacheKey) {
            cache
        } else {
            (apiService.listGamePacket(hardwareInfo, lang).data ?: emptyList())
                .also {
                    remoteCacheKey = key
                    remoteCache = it
                }
        }
    }

    private data class RemoteCacheKey(
        val hardwareInfo: String,
        val lang: String,
    )

}