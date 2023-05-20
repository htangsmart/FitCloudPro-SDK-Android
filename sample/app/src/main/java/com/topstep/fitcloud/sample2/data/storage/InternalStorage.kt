package com.topstep.fitcloud.sample2.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.topstep.fitcloud.sample2.data.storage.InternalStorageImpl.PreferencesKeys.AUTHED_USER_ID
import kotlinx.coroutines.CoroutineScope
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

    /**
     * 是否是有效的用户ID
     */
    private fun Long.isValidUserId(): Boolean {
        return this > 0
    }

    object PreferencesKeys {
        val AUTHED_USER_ID = longPreferencesKey("authed_user_id")
    }

    companion object {
        const val PREFERENCE_NAME = "internalDataStore"
    }
}