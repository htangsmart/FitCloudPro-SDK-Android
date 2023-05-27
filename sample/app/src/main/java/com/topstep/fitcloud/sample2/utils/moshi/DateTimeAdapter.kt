package com.topstep.fitcloud.sample2.utils.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import com.topstep.fitcloud.sample2.utils.DateTimeFormatter
import java.util.*

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class DateField

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class TimeField

class DateAdapter {
    @ToJson
    fun toJson(@DateField date: Date): String {
        return DateTimeFormatter.formatDate(date)
    }

    @FromJson
    @DateField
    fun fromJson(str: String): Date {
        return DateTimeFormatter.parseDate(str)
    }
}

class TimeAdapter {
    @ToJson
    fun toJson(@TimeField date: Date): String {
        return DateTimeFormatter.formatTime(date)
    }

    @FromJson
    @TimeField
    fun fromJson(str: String): Date {
        return DateTimeFormatter.parseTime(str)
    }
}
