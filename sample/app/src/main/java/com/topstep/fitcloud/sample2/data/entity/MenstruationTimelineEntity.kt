package com.topstep.fitcloud.sample2.data.entity

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.topstep.fitcloud.sample2.data.entity.MenstruationTimelineEntity.Type
import com.topstep.fitcloud.sample2.utils.room.DateConverter
import java.util.*

/**
 * Menstruation timeline
 */
@Entity
data class MenstruationTimelineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val userId: Long,

    /**
     * Node type
     */
    @Type val type: Int,

    @field:TypeConverters(DateConverter::class)
    val date: Date,

    /**
     * When type is [Type.SEGMENT_BEGIN], this value is valid
     */
    val cycle: Int,

    /**
     * When type is [Type.SEGMENT_BEGIN], this value is valid
     */
    val duration: Int,
) {

    @IntDef(
        Type.SEGMENT_BEGIN,
        Type.CYCLE_END
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type {
        companion object {
            /**
             * Menstruation segment begin
             * A menstruation segment may contains many menstruation cycle.
             */
            const val SEGMENT_BEGIN = 0

            /**
             * The end time of menstruation in one cycle
             */
            const val CYCLE_END = 1
        }
    }
}