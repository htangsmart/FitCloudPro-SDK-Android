package com.topstep.fitcloud.sample2.data

import java.io.IOException

/**
 * Network exception with an error code and message
 */
class NetResultException(val errorCode: Int, val errorMsg: String?) : IOException() {
    override val message: String
        get() = "errorCode=${errorCode},errorMsg=${errorMsg}"

    companion object {
        internal const val ERROR_CODE_NONE = 0
    }
}

class AccountException(
    val errorCode: Int
) : IOException() {
    override val message: String
        get() = "errorCode=$errorCode"

    companion object {
        const val ERROR_CODE_USER_EXIST = 0
        const val ERROR_CODE_USER_NOT_EXIST = 1
        const val ERROR_CODE_PASSWORD = 2
    }
}

/**
 * Persistent data error, generally write data to the configuration, file or database error
 */
class DataPersistedException : IOException()

class UnSupportDialLcdException : Exception()

class UnSupportDialCustomException : Exception()