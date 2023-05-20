package com.topstep.fitcloud.sample2.data.net.json

import com.squareup.moshi.*
import timber.log.Timber
import java.lang.reflect.Type

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class StringNotBlank

object StringNotBlankJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type == String::class.java && annotations.size > 0) {
            if (annotations.filterIsInstance<StringNotBlank>().isNotEmpty()) {
                return StringNotBlankJsonAdapter().nullSafe()
            }
        }
        return null
    }
}

class StringNotBlankJsonAdapter : JsonAdapter<String>() {
    init {
        Timber.tag("JsonAdapter").d("create new StringNotBlankJsonAdapter")
    }

    override fun fromJson(reader: JsonReader): String {
        val str = reader.nextString()
        if (str == null || str.isBlank()) {
            throw JsonDataException("Expected a NotBlank String but was $str")
        }
        return str
    }

    override fun toJson(writer: JsonWriter, value: String?) {
        writer.value(value)
    }

    override fun toString(): String {
        return "NotBlankStringJsonAdapter"
    }
}
