package com.topstep.fitcloud.sample2.data.wh

import com.topstep.fitcloud.sample2.data.db.AppDatabase
import com.topstep.fitcloud.sample2.data.storage.InternalStorage
import com.topstep.fitcloud.sample2.data.wh.menstruation.MenstruationHelper
import com.topstep.fitcloud.sample2.model.wh.MenstruationResult
import com.topstep.fitcloud.sample2.model.wh.WomenHealthConfig
import com.topstep.fitcloud.sample2.model.wh.WomenHealthMode
import com.topstep.fitcloud.sdk.v2.model.config.FcWomenHealthConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*

interface WomenHealthRepository {

    /**
     * Flow current config or null if disabled
     */
    val flowCurrent: StateFlow<WomenHealthConfig?>

    /**
     * Obtain the previous config of a certain mode
     */
    suspend fun getConfigByMode(@WomenHealthMode mode: Int): WomenHealthConfig

    /**
     * Set config.
     * Null represents turn off women health function
     */
    suspend fun setConfig(config: WomenHealthConfig?)

    suspend fun changeMenstruationEndDate(calendar: Calendar, select: Date, addOrDelete: Boolean)

    suspend fun getMenstruationResult(calendar: Calendar, select: Date): MenstruationResult?

    fun clearCache()

    suspend fun getConfigForDevice(config: WomenHealthConfig?): FcWomenHealthConfig?
}

internal class WomenHealthRepositoryImpl constructor(
    applicationScope: CoroutineScope,
    private val internalStorage: InternalStorage,
    private val appDatabase: AppDatabase,
) : WomenHealthRepository {

    override val flowCurrent: StateFlow<WomenHealthConfig?> = internalStorage.flowAuthedUserId.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(null)
        } else {
            appDatabase.womenHealthDao().flowWomenHealthConfig(userId)
        }
    }.stateIn(applicationScope, SharingStarted.Eagerly, null)

    override suspend fun getConfigByMode(@WomenHealthMode mode: Int): WomenHealthConfig {
        val current = flowCurrent.value
        if (current != null) {
            if (current.mode == mode) {
                return current
            }
            /**
             * [WomenHealthMode.MENSTRUATION] [WomenHealthMode.PREGNANCY_PREPARE] has same config
             */
            if (mode != WomenHealthMode.PREGNANCY && current.mode != WomenHealthMode.PREGNANCY) {
                return current
            }
        }
        var config: WomenHealthConfig? = null
        val userId = internalStorage.flowAuthedUserId.value
        if (userId != null) {
            try {
                config = appDatabase.womenHealthDao().getWomenHealthConfigByMode(userId, mode)
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
        if (config == null) {
            config = WomenHealthConfig(mode = mode)
        }
        return config
    }

    override suspend fun setConfig(config: WomenHealthConfig?) {
        val userId = internalStorage.flowAuthedUserId.value ?: return
        if (appDatabase.womenHealthDao().setWomenHealthConfig(userId, config)) {
            clearCache()
        }
    }

    private var helper: MenstruationHelper? = null

    private fun getMenstruationHelper(userId: Long): MenstruationHelper {
        var helper = this.helper
        if (helper == null || helper.userId != userId) {
            helper = MenstruationHelper(userId, appDatabase.womenHealthDao()).also {
                this.helper = it
            }
        }
        return helper
    }

    override suspend fun changeMenstruationEndDate(calendar: Calendar, select: Date, addOrDelete: Boolean) {
        val userId = internalStorage.flowAuthedUserId.value ?: return
        val helper = getMenstruationHelper(userId)
        helper.changeMenstruationEndDate(calendar, select, addOrDelete)
    }

    override suspend fun getMenstruationResult(calendar: Calendar, select: Date): MenstruationResult? {
        val userId = internalStorage.flowAuthedUserId.value ?: return null
        val helper = getMenstruationHelper(userId)
        val remindAdvance = flowCurrent.value?.remindAdvance ?: 3
        return try {
            helper.getMenstruationResult(calendar, select, remindAdvance)
        } catch (e: Exception) {
            Timber.w(e)
            null
        }
    }

    override fun clearCache() {
        val userId = internalStorage.flowAuthedUserId.value ?: return
        val helper = this.helper
        if (helper != null && helper.userId == userId) {
            helper.clearCache()
        }
    }

    override suspend fun getConfigForDevice(config: WomenHealthConfig?): FcWomenHealthConfig? {
        val userId = internalStorage.flowAuthedUserId.value ?: return null
        val helper = getMenstruationHelper(userId)
        return try {
            helper.getConfigForDevice(config)
        } catch (e: Exception) {
            Timber.w(e)
            null
        }
    }
}