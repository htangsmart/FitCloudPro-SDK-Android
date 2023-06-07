package com.topstep.fitcloud.sample2.data.auth

import android.database.sqlite.SQLiteConstraintException
import com.topstep.fitcloud.sample2.data.AccountException
import com.topstep.fitcloud.sample2.data.db.AppDatabase
import com.topstep.fitcloud.sample2.data.entity.UserEntity
import com.topstep.fitcloud.sample2.data.storage.InternalStorage

interface AuthManager {

    /**
     * Get current authed user id or null if not exist
     */
    fun getAuthedUserIdOrNull(): Long?

    suspend fun hasAuthedUser(): Boolean

    /**
     * Sign in
     * @param name User name
     * @param password User password
     */
    suspend fun signIn(name: String, password: String)

    /**
     * @param name User name
     * @param password User password
     * @param height User height, unit cm
     * @param weight User weight, unit kg
     * @param sex True for male, false for female
     * @param age
     */
    suspend fun signUp(name: String, password: String, height: Int, weight: Int, sex: Boolean, age: Int)

    /**
     * Sign out
     */
    suspend fun signOut()

}

internal class AuthManagerImpl(
    private val internalStorage: InternalStorage,
    appDatabase: AppDatabase,
) : AuthManager {

    private val userDao = appDatabase.userDao()

    override fun getAuthedUserIdOrNull(): Long? {
        return internalStorage.flowAuthedUserId.value
    }

    override suspend fun hasAuthedUser(): Boolean {
        val userId = getAuthedUserIdOrNull() ?: return false
        return userDao.isUserExist(userId)
    }

    /**
     * A mock sign in function.
     * Match [UserEntity.name] and [name] to determine whether the user has been [signUp]
     */
    override suspend fun signIn(name: String, password: String) {
        val userEntity = userDao.queryUserByName(name) ?: throw AccountException(AccountException.ERROR_CODE_USER_NOT_EXIST)
        if (userEntity.password != password) {
            throw AccountException(AccountException.ERROR_CODE_PASSWORD)
        }
        internalStorage.setAuthedUserId(userEntity.id)
    }

    /**
     * A mock sign up function.
     * We use the [UserEntity.name] as a unique identifier for the user. Creating a user in the database represents a sign up process.
     */
    override suspend fun signUp(name: String, password: String, height: Int, weight: Int, sex: Boolean, age: Int) {
        val userEntity = UserEntity(0, name, password, height, weight, sex, age)
        try {
            userDao.insert(userEntity)
            internalStorage.setAuthedUserId(userDao.queryUserByName(name)!!.id)
        } catch (e: SQLiteConstraintException) {
            //If the username exists, then a conflict exception will be throw
            throw AccountException(AccountException.ERROR_CODE_USER_EXIST)
        }
    }

    override suspend fun signOut() {
        internalStorage.setAuthedUserId(null)
    }

    companion object {
        private const val TAG = "AuthManager"
    }

}