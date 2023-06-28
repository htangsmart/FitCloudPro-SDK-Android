package com.topstep.fitcloud.sample2.utils.room

import androidx.room.TypeConverter
import java.util.*

object UUIDConverter {
    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun fromStr(str: String?): UUID {
        return UUID.fromString(str)
    }
}