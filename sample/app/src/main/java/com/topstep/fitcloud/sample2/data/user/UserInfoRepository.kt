package com.topstep.fitcloud.sample2.data.user

import com.topstep.fitcloud.sample2.data.db.AppDatabase
import com.topstep.fitcloud.sample2.model.user.UserInfo

interface UserInfoRepository {

    suspend fun getUserInfo(userId: Long): UserInfo

    suspend fun setUserInfo(userId: Long, info: UserInfo)

}

internal class UserInfoRepositoryImpl constructor(
    appDatabase: AppDatabase,
) : UserInfoRepository {

    private val userDao = appDatabase.userDao()

    override suspend fun getUserInfo(userId: Long): UserInfo {
        return userDao.queryUserInfo(userId)
    }

    override suspend fun setUserInfo(userId: Long, info: UserInfo) {
        userDao.updateUserInfo(
            userId,
            info.height, info.weight, info.sex, info.age
        )
    }

}