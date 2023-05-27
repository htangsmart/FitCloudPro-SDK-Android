package com.topstep.fitcloud.sample2.di.internal

import android.content.Context
import com.squareup.moshi.Moshi
import com.topstep.fitcloud.sample2.MyApplication
import com.topstep.fitcloud.sample2.data.auth.AuthManager
import com.topstep.fitcloud.sample2.data.auth.AuthManagerImpl
import com.topstep.fitcloud.sample2.data.db.AppDatabase
import com.topstep.fitcloud.sample2.data.device.DeviceManager
import com.topstep.fitcloud.sample2.data.device.DeviceManagerImpl
import com.topstep.fitcloud.sample2.data.device.DialRepository
import com.topstep.fitcloud.sample2.data.device.DialRepositoryImpl
import com.topstep.fitcloud.sample2.data.net.ApiClient
import com.topstep.fitcloud.sample2.data.net.json.*
import com.topstep.fitcloud.sample2.data.storage.InternalStorage
import com.topstep.fitcloud.sample2.data.storage.InternalStorageImpl
import com.topstep.fitcloud.sample2.data.wh.WomenHealthRepository
import com.topstep.fitcloud.sample2.data.wh.WomenHealthRepositoryImpl

object SingleInstance {

    private val applicationContext: Context = MyApplication.instance

    val appDatabase: AppDatabase by lazy { AppDatabase.build(applicationContext, CoroutinesInstance.ioDispatcher) }

    private val internalStorage: InternalStorage by lazy { InternalStorageImpl(applicationContext, CoroutinesInstance.applicationScope, CoroutinesInstance.applicationIOScope) }

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

    val apiClient: ApiClient by lazy { ApiClient(moshi) }

    val authManager: AuthManager by lazy { AuthManagerImpl(internalStorage, appDatabase) }

    val womenHealthRepository: WomenHealthRepository by lazy {
        WomenHealthRepositoryImpl(
            CoroutinesInstance.applicationScope,
            internalStorage,
            appDatabase
        )
    }

    val deviceManager: DeviceManager by lazy {
        DeviceManagerImpl(
            applicationContext,
            CoroutinesInstance.applicationScope,
            internalStorage,
            womenHealthRepository,
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
}