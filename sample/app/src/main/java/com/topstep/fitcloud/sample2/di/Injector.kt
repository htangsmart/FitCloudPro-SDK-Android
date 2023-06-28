package com.topstep.fitcloud.sample2.di

import com.topstep.fitcloud.sample2.MyApplication
import com.topstep.fitcloud.sample2.data.auth.AuthManager
import com.topstep.fitcloud.sample2.data.config.ExerciseGoalRepository
import com.topstep.fitcloud.sample2.data.device.*
import com.topstep.fitcloud.sample2.data.user.UserInfoRepository
import com.topstep.fitcloud.sample2.data.version.VersionRepository
import com.topstep.fitcloud.sample2.data.version.VersionRepositoryImpl
import com.topstep.fitcloud.sample2.data.wh.WomenHealthRepository
import com.topstep.fitcloud.sample2.di.internal.CoroutinesInstance
import com.topstep.fitcloud.sample2.di.internal.SingleInstance
import kotlinx.coroutines.CoroutineScope

/**
 * Because some developers may not use dagger or hilt.
 * In order to reduce their learning cost, this sample uses manual injection of dependencies
 */
object Injector {

    fun getAuthManager(): AuthManager {
        return SingleInstance.authManager
    }

    fun requireAuthedUserId(): Long {
        return SingleInstance.authManager.getAuthedUserIdOrNull()!!
    }

    fun getDeviceManager(): DeviceManager {
        return SingleInstance.deviceManager
    }

    fun getUserInfoRepository(): UserInfoRepository {
        return SingleInstance.userInfoRepository
    }

    fun getVersionRepository(): VersionRepository {
        return VersionRepositoryImpl(
            SingleInstance.deviceManager,
            SingleInstance.apiClient.apiService
        )
    }

    fun getGameRepository(): GameRepository {
        return GameRepositoryImpl(
            MyApplication.instance,
            SingleInstance.deviceManager,
            SingleInstance.apiClient.apiService
        )
    }

    fun getSportPushRepository(): SportPushRepository {
        return SportPushRepositoryImpl(
            SingleInstance.deviceManager,
            SingleInstance.apiClient.apiService
        )
    }

    fun getDialRepository(): DialRepository {
        return SingleInstance.dialRepository
    }

    fun getApplicationScope(): CoroutineScope {
        return CoroutinesInstance.applicationScope
    }

    fun getWomenHealthRepository(): WomenHealthRepository {
        return SingleInstance.womenHealthRepository
    }

    fun getExerciseGoalRepository(): ExerciseGoalRepository {
        return SingleInstance.exerciseGoalRepository
    }

    fun getSyncDataRepository(): SyncDataRepository {
        return SingleInstance.syncDataRepository
    }
}