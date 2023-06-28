package com.topstep.fitcloud.sample2.utils.room

import androidx.room.TypeConverter
import com.squareup.moshi.Types
import com.topstep.fitcloud.sample2.di.internal.SingleInstance

private val moshi = SingleInstance.moshi

object ListIntConverter {
    @TypeConverter
    fun fromList(list: List<Int>?): String? {
        return if (list.isNullOrEmpty()) {
            null
        } else {
            val type = Types.newParameterizedType(List::class.java, Integer::class.java)
            val adapter = moshi.adapter<List<Int>>(type)
            adapter.toJson(list)
        }
    }

    @TypeConverter
    fun fromStr(str: String?): List<Int>? {
        return if (str.isNullOrEmpty()) {
            null
        } else {
            val type = Types.newParameterizedType(List::class.java, Integer::class.java)
            val adapter = moshi.adapter<List<Int>>(type)
            adapter.fromJson(str)
        }
    }
}
