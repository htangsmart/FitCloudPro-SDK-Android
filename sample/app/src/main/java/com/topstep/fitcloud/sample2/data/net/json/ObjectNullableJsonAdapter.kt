package com.topstep.fitcloud.sample2.data.net.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.internal.Util
import com.topstep.fitcloud.sample2.data.NetResultException
import com.topstep.fitcloud.sample2.data.net.ObjectNullable
import timber.log.Timber
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.Int
import kotlin.String

object ObjectNullableJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type is ParameterizedType && type.rawType == ObjectNullable::class.java) {
            return ObjectNullableJsonAdapter<Any>(moshi, type.actualTypeArguments).nullSafe()
        }
        return null
    }
}

class ObjectNullableJsonAdapter<T>(
    moshi: Moshi,
    types: Array<Type>
) : JsonAdapter<ObjectNullable<T>>() {
    init {
        require(types.size == 1) {
            buildString { append("TypeVariable mismatch: Expecting ").append(1).append(" type for generic type variables [").append("T").append("], but received ").append(types.size) }
        }
        Timber.tag("JsonAdapter").d("create new ObjectNullableJsonAdapter for ${types[0]}")
    }

    private val options: JsonReader.Options = JsonReader.Options.of("errorCode", "errorMsg", "data")

    private val intAdapter: JsonAdapter<Int> = moshi.adapter(Int::class.java, emptySet(), "errorCode")

    private val nullableStringAdapter: JsonAdapter<String?> = moshi.adapter(String::class.java, emptySet(), "errorMsg")

    private val nullableTNullableAnyAdapter: JsonAdapter<T?> = moshi.adapter(types[0], emptySet(), "data")

    override fun toString(): String {
        return "ObjectNullableJsonAdapter"
    }

    override fun fromJson(reader: JsonReader): ObjectNullable<T> {
        var errorCode: Int? = null
        var errorMsg: String? = null
        var data: T? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> errorCode = intAdapter.fromJson(reader) ?: throw Util.unexpectedNull("errorCode", "errorCode", reader)
                1 -> errorMsg = nullableStringAdapter.fromJson(reader)
                2 -> data = nullableTNullableAnyAdapter.fromJson(reader)
                -1 -> {
                    // Unknown name, skip it.
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        val resultErrorCode = errorCode ?: throw Util.missingProperty("errorCode", "errorCode", reader)
        if (resultErrorCode != NetResultException.ERROR_CODE_NONE) {
            throw NetResultException(resultErrorCode, errorMsg)
        }
        return ObjectNullable(
            errorCode = resultErrorCode,
            errorMsg = errorMsg,
            data = data
        )
    }

    override fun toJson(writer: JsonWriter, value: ObjectNullable<T>?) {
        if (value == null) {
            throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name("errorCode")
        intAdapter.toJson(writer, value.errorCode)
        writer.name("errorMsg")
        nullableStringAdapter.toJson(writer, value.errorMsg)
        writer.name("data")
        nullableTNullableAnyAdapter.toJson(writer, value.data)
        writer.endObject()
    }
}
