package com.topstep.fitcloud.sample2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.topstep.fitcloud.sample2.utils.room.TimeConverter
import com.topstep.fitcloud.sdk.v2.utils.ICalculateSleepItem
import java.util.*

@Entity
data class SleepItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val userId: Long,

    /**
     * Which day belong.
     */
    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    /**
     * The start time of this time period, in yyyy-MM-dd HH:mm:ss date format
     */
    @field:TypeConverters(TimeConverter::class)
    val startTime: Date,

    /**
     * The end time of this time period, in yyyy-MM-dd HH:mm:ss date format
     */
    @field:TypeConverters(TimeConverter::class)
    val endTime: Date,

    /**
     * Sleep status during this period:
     *
     * 1.deep sleep
     *
     * 2 light sleep
     *
     * 3 awake
     */
    val status: Int,
) : ICalculateSleepItem {
    override fun getCalculateStatus(): Int {
        return status
    }

    override fun getCalculateStartTime(): Long {
        return startTime.time
    }

    override fun getCalculateEndTime(): Long {
        return endTime.time
    }
}