package com.topstep.fitcloud.sample2.data.entity

import androidx.room.Entity
import androidx.room.TypeConverters
import com.topstep.fitcloud.sample2.utils.room.TimeConverter
import java.util.*

@Entity(primaryKeys = ["userId", "time"])
class HeartRateItemEntity(
    val userId: Long,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    val heartRate: Int,
)

@Entity(primaryKeys = ["userId", "time"])
class OxygenItemEntity(

    val userId: Long,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    val oxygen: Int,
)

@Entity(primaryKeys = ["userId", "time"])
class BloodPressureItemEntity(

    val userId: Long,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    val sbp: Int,

    val dbp: Int,
)

@Entity(primaryKeys = ["userId", "time"])
class TemperatureItemEntity(
    val userId: Long,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    val body: Float,
    val wrist: Float,
)

@Entity(primaryKeys = ["userId", "time"])
class PressureItemEntity(

    val userId: Long,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    val pressure: Int,
)