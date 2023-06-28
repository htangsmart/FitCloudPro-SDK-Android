package com.topstep.fitcloud.sample2.data.device

import android.util.SparseArray
import com.topstep.fitcloud.sample2.data.net.ApiService
import com.topstep.fitcloud.sample2.model.sport.push.SportPacket
import com.topstep.fitcloud.sample2.model.sport.push.SportPushParams
import com.topstep.fitcloud.sdk.v2.model.settings.sport.FcSportSpace
import kotlinx.coroutines.rx3.await

interface SportPushRepository {
    suspend fun getSportPushParams(): SportPushParams
}

internal class SportPushRepositoryImpl constructor(
    private val deviceManager: DeviceManager,
    private val apiService: ApiService,
) : SportPushRepository {

    private var remoteCacheKey: String? = null
    private var remoteCache: List<SportPacket>? = null

    private suspend fun getRemoteSportPacket(hardwareInfo: String): List<SportPacket> {
        val cache = remoteCache
        return if (cache != null && hardwareInfo == remoteCacheKey) {
            cache
        } else {
            (apiService.listSportPacket(hardwareInfo).data ?: emptyList())
                .also {
                    remoteCacheKey = hardwareInfo
                    remoteCache = it
                }
        }
    }

    private fun List<SportPacket>.toSparseArray(): SparseArray<SportPacket> {
        val array = SparseArray<SportPacket>()
        for (item in this) {
            array[item.sportUiType] = item
        }
        return array
    }

    override suspend fun getSportPushParams(): SportPushParams {
        val hardwareInfo = deviceManager.configFeature.getDeviceInfo().toString()
        val supportSportUiTypes = deviceManager.settingsFeature.requestSupportSportUiTypes().await()
        val sportSpaces = deviceManager.settingsFeature.requestSportPushInfo().await()
        val packets = getRemoteSportPacket(hardwareInfo).toSparseArray()

        val supportPackets = ArrayList<SportPacket>(packets.size())
        for (sportUiType in supportSportUiTypes) {
            //查找是否在服务器中有对应的，避免出错。如果为null，那么就跳过
            val packet = packets.get(sportUiType) ?: continue
            supportPackets.add(packet)
        }

        val pushableSpaces = ArrayList<FcSportSpace>(sportSpaces.size)
        for (space in sportSpaces) {
            if (space.isPushable) {
                pushableSpaces.add(space)
            }
        }

        return SportPushParams(supportPackets, pushableSpaces)
    }

}