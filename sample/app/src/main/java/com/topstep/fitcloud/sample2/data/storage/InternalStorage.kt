package com.topstep.fitcloud.sample2.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.topstep.fitcloud.sample2.data.storage.InternalStorageImpl.PreferencesKeys.AUTHED_USER_ID
import com.topstep.fitcloud.sample2.data.storage.InternalStorageImpl.PreferencesKeys.AUTO_UPDATE_GPS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

internal interface InternalStorage {

    val flowAuthedUserId: StateFlow<Long?>

    /**
     * Set current authed user id.
     * 0 or null means clear the currently authed user
     */
    suspend fun setAuthedUserId(userId: Long?)

    suspend fun setAutoUpdateGps(auto: Boolean)

    fun flowAutoUpdateGps(): Flow<Boolean>
}

internal class InternalStorageImpl(
    private val applicationContext: Context,
    applicationScope: CoroutineScope,
    applicationIoScope: CoroutineScope
) : InternalStorage {

    private val Context.internalDataStore: DataStore<Preferences> by preferencesDataStore(
        name = PREFERENCE_NAME,
        scope = applicationIoScope
    )

    override val flowAuthedUserId: StateFlow<Long?> = runBlocking {
        applicationContext.internalDataStore.data.map {
            val userId = it[AUTHED_USER_ID]
            if (userId == null || !userId.isValidUserId()) {
                null
            } else {
                userId
            }
        }.stateIn(applicationScope)
    }

    override suspend fun setAuthedUserId(userId: Long?) {
        applicationContext.internalDataStore.edit {
            it[AUTHED_USER_ID] = userId ?: 0
        }
    }

    override suspend fun setAutoUpdateGps(auto: Boolean) {
        applicationContext.internalDataStore.edit {
            it[AUTO_UPDATE_GPS] = auto
        }
    }

    override fun flowAutoUpdateGps(): Flow<Boolean> {
        return applicationContext.internalDataStore.data.map {
            it[AUTO_UPDATE_GPS] ?: false
        }
    }

    /**
     * 是否是有效的用户ID
     */
    private fun Long.isValidUserId(): Boolean {
        return this > 0
    }

    object PreferencesKeys {
        val AUTHED_USER_ID = longPreferencesKey("authed_user_id")
        val AUTO_UPDATE_GPS = booleanPreferencesKey("auto_update_gps")
    }

    companion object {
        const val PREFERENCE_NAME = "internalDataStore"
    }
}