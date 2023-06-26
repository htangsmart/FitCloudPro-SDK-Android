package com.topstep.fitcloud.sample2.data.entity

import androidx.room.Entity
import androidx.room.TypeConverters
import com.topstep.fitcloud.sample2.utils.room.TimeConverter
import java.util.*

@Entity(primaryKeys = ["userId", "time"])
class StepItemEntity(
    val userId: Long,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    val step: Int,
    val distance: Float,
    val calories: Float,
)