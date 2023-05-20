package com.topstep.fitcloud.sample2.data.net.json

import com.squareup.moshi.*
import com.squareup.moshi.internal.Util
import com.topstep.fitcloud.sample2.data.net.ListNonNull
import com.topstep.fitcloud.sample2.data.net.ListNullable
import timber.log.Timber
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object ListNonNullJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type is ParameterizedType && type.rawType == ListNonNull::class.java) {
            return ListNonNullJsonAdapter<Any>(moshi, type.actualTypeArguments).nullSafe()
        }
        return null
    }
}

class ListNonNullJsonAdapter<T>(
    moshi: Moshi,
    types: Array<Type>
) : JsonAdapter<ListNonNull<T>>() {

    private val delegate: JsonAdapter<ListNullable<T>>

    init {
        require(types.size == 1) {
            buildString { append("TypeVariable mismatch: Expecting ").append(1).append(" type for generic type variables [").append("T").append("], but received ").append(types.size) }
        }
        Timber.tag("JsonAdapter").d("create new ListNonNullJsonAdapter for ${types[0]}")
        delegate = moshi.adapter(Types.newParameterizedType(ListNullable::class.java, types[0]))
    }

    override fun toString(): String {
        return "ListNonNullJsonAdapter"
    }

    override fun fromJson(reader: JsonReader): ListNonNull<T> {
        val result: ListNullable<T>? = delegate.fromJson(reader)
        val data: List<T> = result?.data?.takeIf { it.isNotEmpty() } ?: throw Util.unexpectedNull("data", "data", reader)
        return ListNonNull(
            errorCode = result.errorCode,
            errorMsg = result.errorMsg,
            data = data
        )
    }

    override fun toJson(writer: JsonWriter, value: ListNonNull<T>?) {
        delegate.toJson(writer, value)
    }
}
