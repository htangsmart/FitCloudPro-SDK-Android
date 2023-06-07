package com.topstep.fitcloud.sample2.data.net.json

import com.squareup.moshi.*
import com.squareup.moshi.internal.Util
import com.topstep.fitcloud.sample2.data.net.ObjectNonNull
import com.topstep.fitcloud.sample2.data.net.ObjectNullable
import timber.log.Timber
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object ObjectNonNullJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type is ParameterizedType && type.rawType == ObjectNonNull::class.java) {
            return ObjectNonNullJsonAdapter<Any>(moshi, type.actualTypeArguments).nullSafe()
        }
        return null
    }
}

class ObjectNonNullJsonAdapter<T>(
    moshi: Moshi,
    types: Array<Type>
) : JsonAdapter<ObjectNonNull<T>>() {

    private val delegate: JsonAdapter<ObjectNullable<T>>

    init {
        require(types.size == 1) {
            buildString { append("TypeVariable mismatch: Expecting ").append(1).append(" type for generic type variables [").append("T").append("], but received ").append(types.size) }
        }
        Timber.tag("JsonAdapter").d("create new ObjectNonNullJsonAdapter for ${types[0]}")
        delegate = moshi.adapter(Types.newParameterizedType(ObjectNullable::class.java, types[0]))
    }

    override fun toString(): String {
        return "ObjectNonNullJsonAdapter"
    }

    override fun fromJson(reader: JsonReader): ObjectNonNull<T> {
        val result: ObjectNullable<T>? = delegate.fromJson(reader)
        val data: T = result?.data ?: throw Util.unexpectedNull("data", "data", reader)
        return ObjectNonNull(
            errorCode = result.errorCode,
            errorMsg = result.errorMsg,
            data = data
        )
    }

    override fun toJson(writer: JsonWriter, value: ObjectNonNull<T>?) {
        delegate.toJson(writer, value)
    }
}
