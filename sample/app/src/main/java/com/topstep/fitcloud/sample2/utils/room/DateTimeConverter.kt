package com.topstep.fitcloud.sample2.utils.room

import androidx.room.TypeConverter
import com.topstep.fitcloud.sample2.utils.DateTimeFormatter
import java.util.*

object DateConverter {
    @TypeConverter
    fun fromDate(date: Date): String {
        return DateTimeFormatter.formatDate(date)
    }

    @TypeConverter
    fun fromStr(str: String): Date {
        return DateTimeFormatter.parseDate(str)
    }
}

object TimeConverter {
    @TypeConverter
    fun fromDate(date: Date): String {
        return DateTimeFormatter.formatTime(date)
    }

    @TypeConverter
    fun fromStr(str: String): Date {
        return DateTimeFormatter.parseTime(str)
    }
}

