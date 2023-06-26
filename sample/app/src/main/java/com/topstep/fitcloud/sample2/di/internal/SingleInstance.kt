package com.topstep.fitcloud.sample2.di.internal

import android.content.Context
import com.squareup.moshi.Moshi
import com.topstep.fitcloud.sample2.MyApplication
import com.topstep.fitcloud.sample2.data.auth.AuthManager
import com.topstep.fitcloud.sample2.data.auth.AuthManagerImpl
import com.topstep.fitcloud.sample2.data.config.ExerciseGoalRepository
import com.topstep.fitcloud.sample2.data.config.ExerciseGoalRepositoryImpl
import com.topstep.fitcloud.sample2.data.db.AppDatabase
import com.topstep.fitcloud.sample2.data.device.*
import com.topstep.fitcloud.sample2.data.net.ApiClient
import com.topstep.fitcloud.sample2.data.net.json.*
import com.topstep.fitcloud.sample2.data.storage.InternalStorage
import com.topstep.fitcloud.sample2.data.storage.InternalStorageImpl
import com.topstep.fitcloud.sample2.data.user.UserInfoRepository
import com.topstep.fitcloud.sample2.data.user.UserInfoRepositoryImpl
import com.topstep.fitcloud.sample2.data.wh.WomenHealthRepository
import com.topstep.fitcloud.sample2.data.wh.WomenHealthRepositoryImpl

object SingleInstance {

    private val applicationContext: Context = MyApplication.instance

    private val appDatabase: AppDatabase by lazy {
        AppDatabase.build(applicationContext, CoroutinesInstance.ioDispatcher, moshi)
    }

    private val internalStorage: InternalStorage by lazy {
        InternalStorageImpl(applicationContext, CoroutinesInstance.applicationScope, CoroutinesInstance.applicationIOScope)
    }

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(StringNotBlankJsonAdapterFactory)
            .add(BaseResultJsonAdapterFactory)
            .add(ObjectNullableJsonAdapterFactory)
            .add(ObjectNonNullJsonAdapterFactory)
            .add(ListNullableJsonAdapterFactory)
            .add(ListNonNullJsonAdapterFactory)
            .build()
    }

    val apiClient: ApiClient by lazy {
        ApiClient(moshi)
    }

    val authManager: AuthManager by lazy {
        AuthManagerImpl(internalStorage, appDatabase)
    }

    val womenHealthRepository: WomenHealthRepository by lazy {
        WomenHealthRepositoryImpl(
            CoroutinesInstance.applicationScope,
            internalStorage,
            appDatabase
        )
    }

    val userInfoRepository: UserInfoRepository by lazy {
        UserInfoRepositoryImpl(CoroutinesInstance.applicationScope, internalStorage, appDatabase)
    }

    val syncDataRepository: SyncDataRepository by lazy {
        SyncDataRepositoryImpl(appDatabase, userInfoRepository)
    }

    val deviceManager: DeviceManager by lazy {
        DeviceManagerImpl(
            applicationContext,
            CoroutinesInstance.applicationScope,
            internalStorage,
            userInfoRepository,
            womenHealthRepository,
            exerciseGoalRepository,
            syncDataRepository,
            appDatabase,
        )
    }

    val dialRepository: DialRepository by lazy {
        DialRepositoryImpl(
            applicationContext,
            moshi,
            deviceManager,
            apiClient.apiService
        )
    }

    val exerciseGoalRepository: ExerciseGoalRepository by lazy {
        ExerciseGoalRepositoryImpl(
            CoroutinesInstance.applicationScope,
            internalStorage,
            appDatabase
        )
    }
}