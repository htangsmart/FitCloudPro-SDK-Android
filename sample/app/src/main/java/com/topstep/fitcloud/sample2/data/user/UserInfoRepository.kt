package com.topstep.fitcloud.sample2.data.user

import com.topstep.fitcloud.sample2.data.db.AppDatabase
import com.topstep.fitcloud.sample2.data.storage.InternalStorage
import com.topstep.fitcloud.sample2.model.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

interface UserInfoRepository {

    val flowCurrent: StateFlow<UserInfo?>

    suspend fun getUserInfo(userId: Long): UserInfo

    suspend fun setUserInfo(userId: Long, height: Int, weight: Int, sex: Boolean, age: Int)

}

internal class UserInfoRepositoryImpl constructor(
    applicationScope: CoroutineScope,
    internalStorage: InternalStorage,
    appDatabase: AppDatabase,
) : UserInfoRepository {

    private val userDao = appDatabase.userDao()

    override val flowCurrent: StateFlow<UserInfo?> = internalStorage.flowAuthedUserId.flatMapLatest {
        if (it == null) {
            flowOf(null)
        } else {
            appDatabase.userDao().flowUserInfo(it)
        }
    }.stateIn(applicationScope, SharingStarted.Eagerly, null)

    override suspend fun getUserInfo(userId: Long): UserInfo {
        return userDao.queryUserInfo(userId)
    }

    override suspend fun setUserInfo(userId: Long, height: Int, weight: Int, sex: Boolean, age: Int) {
        userDao.updateUserInfo(
            userId, height, weight, sex, age
        )
    }

}