package com.topstep.fitcloud.sample2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.topstep.fitcloud.sample2.utils.room.TimeConverter
import com.topstep.fitcloud.sample2.utils.room.UUIDConverter
import java.util.*

@Entity
class GameRecordEntity(
    val userId: Long,

    @PrimaryKey
    @field:TypeConverters(UUIDConverter::class)
    val gameId: UUID,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    val type: Int,

    val duration: Int,

    val score: Int,

    val level: Int
)