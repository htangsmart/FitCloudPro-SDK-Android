package com.topstep.fitcloud.sample2.data.entity

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.topstep.fitcloud.sample2.utils.room.ListIntConverter
import com.topstep.fitcloud.sample2.utils.room.TimeConverter
import com.topstep.fitcloud.sample2.utils.room.UUIDConverter
import java.util.*

@Entity
class EcgRecordEntity(
    val userId: Long,

    @PrimaryKey
    @field:TypeConverters(UUIDConverter::class)
    val ecgId: UUID,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    @Type
    val type: Int = Type.NORMAL,

    val samplingRate: Int = 0,

    @field:TypeConverters(ListIntConverter::class)
    val detail: List<Int>? = null,
) {

    fun getIntArrays(): IntArray? {
        if (detail == null || detail.isEmpty()) return null
        val data = IntArray(detail.size)
        for (i in detail.indices) {
            data[i] = detail[i]
        }
        return data
    }

    @IntDef(Type.NORMAL, Type.TI)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type {
        companion object {
            const val NORMAL = 0 //Data normal
            const val TI = 1 //Data of ti chip
        }
    }

}
