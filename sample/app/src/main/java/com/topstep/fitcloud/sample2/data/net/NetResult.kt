package com.topstep.fitcloud.sample2.data.net

open class BaseResult(
    val errorCode: Int,
    val errorMsg: String?,
    val time: String?
)

open class ObjectNullable<T>(
    errorCode: Int,
    errorMsg: String?,
    time: String?,
    open val data: T?
) : BaseResult(errorCode, errorMsg, time)

class ObjectNonNull<T>(
    errorCode: Int,
    errorMsg: String?,
    time: String?,
    data: T
) : ObjectNullable<T>(errorCode, errorMsg, time, data) {
    @Suppress("UNCHECKED_CAST")
    override val data: T
        get() = super.data as T
}

open class ListNullable<T>(
    errorCode: Int,
    errorMsg: String?,
    time: String?,
    open val data: List<T>?
) : BaseResult(errorCode, errorMsg, time)

class ListNonNull<T>(
    errorCode: Int,
    errorMsg: String?,
    time: String?,
    data: List<T>
) : ListNullable<T>(errorCode, errorMsg, time, data) {
    override val data: List<T>
        get() = super.data as List<T>
}
