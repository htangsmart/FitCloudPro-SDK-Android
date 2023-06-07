package com.topstep.fitcloud.sample2.data.net.json

import com.squareup.moshi.*
import com.squareup.moshi.internal.Util
import com.topstep.fitcloud.sample2.data.NetResultException
import com.topstep.fitcloud.sample2.data.net.BaseResult
import timber.log.Timber
import java.lang.reflect.Type
import kotlin.Int
import kotlin.String

object BaseResultJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type == BaseResult::class.java) {
            return BaseResultJsonAdapter(moshi).nullSafe()
        }
        return null
    }
}

class BaseResultJsonAdapter(
    moshi: Moshi
) : JsonAdapter<BaseResult>() {
    init {
        Timber.tag("JsonAdapter").d("create new BaseResultJsonAdapter")
    }

    private val options: JsonReader.Options = JsonReader.Options.of("errorCode", "errorMsg")

    private val intAdapter: JsonAdapter<Int> = moshi.adapter(Int::class.java, emptySet(), "errorCode")

    private val nullableStringAdapter: JsonAdapter<String?> = moshi.adapter(String::class.java, emptySet(), "errorMsg")

    override fun toString(): String {
        return "BaseResultJsonAdapter"
    }

    @FromJson
    override fun fromJson(reader: JsonReader): BaseResult {
        var errorCode: Int? = null
        var errorMsg: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> errorCode = intAdapter.fromJson(reader) ?: throw Util.unexpectedNull("errorCode", "errorCode", reader)
                1 -> errorMsg = nullableStringAdapter.fromJson(reader)
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
        return BaseResult(
            errorCode = resultErrorCode,
            errorMsg = errorMsg
        )
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: BaseResult?) {
        if (value == null) {
            throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name("errorCode")
        intAdapter.toJson(writer, value.errorCode)
        writer.name("errorMsg")
        nullableStringAdapter.toJson(writer, value.errorMsg)
        writer.endObject()
    }
}
