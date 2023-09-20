package com.topstep.fitcloud.sample2.data.gps

import android.net.Uri
import com.topstep.fitcloud.sample2.data.net.ApiService
import com.topstep.fitcloud.sample2.data.storage.InternalStorage
import com.topstep.fitcloud.sdk.v2.model.settings.gps.FcGpsEpoInfo
import com.topstep.fitcloud.sdk.v2.model.settings.gps.FcGpsLocationInfo
import com.topstep.fitcloud.sdk.v2.utils.Optional
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

interface GpsHotStartRepository {

    fun requestGpsLocationInfo(): Single<Optional<FcGpsLocationInfo>>

    fun requestGpsEpoInfo(): Single<Optional<FcGpsEpoInfo>>

    suspend fun setAutoUpdateGps(auto: Boolean)

    fun flowAutoUpdateGps(): Flow<Boolean>
}

internal class GpsHotStartRepositoryImpl(
    private val apiService: ApiService,
    private val internalStorage: InternalStorage
) : GpsHotStartRepository {

    override fun requestGpsLocationInfo(): Single<Optional<FcGpsLocationInfo>> {
        //TODO Using a mock location, actual app should request the location
        return Single.create {
            Thread.sleep(3000)
            it.onSuccess(Optional(FcGpsLocationInfo(lat = 22.525030, lng = 113.921000)))
        }.subscribeOn(Schedulers.io())
    }

    override fun requestGpsEpoInfo(): Single<Optional<FcGpsEpoInfo>> {
        return Single.create {
            val result = runBlocking { apiService.getEpoFiles() }
            val time = result.time?.toLongOrNull()
            if (time == null) {
                it.onSuccess(Optional(null))
            } else {
                val epoTime = parserUrlTime(result.data.url_1)
                it.onSuccess(
                    Optional(
                        FcGpsEpoInfo(
                            currentTime = time * 1000L, epoTime = epoTime,
                            epoUris = listOf(Uri.parse(result.data.url_1), Uri.parse(result.data.url_2))
                        )
                    )
                )
            }
        }
    }

    private fun parserUrlTime(url: String): Long? {
        //http://ssmartlink.com//downloads/ephemeris/2023/2023-07-23/20230723_ELPO_GR3_1.DAT
        //目前url是这个格式，可以解析出epo文件更新的时间
        val startIndex = url.lastIndexOf("/")
        if (startIndex == -1) return null
        val fileName = url.substring(startIndex + 1)
        val endIndex = fileName.indexOf("_")
        if (endIndex == -1) return null
        val timeStr = fileName.substring(0, endIndex)
        return try {
            SimpleDateFormat("yyyyMMdd", Locale.US).parse(timeStr)?.time
        } catch (e: Exception) {
            Timber.w(e)
            null
        }
    }

    override suspend fun setAutoUpdateGps(auto: Boolean) {
        internalStorage.setAutoUpdateGps(auto)
    }

    override fun flowAutoUpdateGps(): Flow<Boolean> {
        return internalStorage.flowAutoUpdateGps()
    }
}