package com.topstep.fitcloud.sample2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.topstep.fitcloud.sample2.model.wh.WomenHealthConfig
import com.topstep.fitcloud.sample2.utils.room.DateConverter
import com.topstep.fitcloud.sdk.v2.model.config.FcWomenHealthConfig
import java.util.*

/**
 * Women health base config.
 *
 * The menstruation info of [cycle]、[duration]、[latest] have some special treatment.
 *
 * When [mode] is [FcWomenHealthConfig.Mode.MENSTRUATION] or [FcWomenHealthConfig.Mode.PREGNANCY_PREPARE], the menstruation info will convert and save as [MenstruationTimelineEntity] to database.
 *
 * When [mode] is [FcWomenHealthConfig.Mode.NONE] or [FcWomenHealthConfig.Mode.PREGNANCY], save directly to database
 */
@Entity
data class WomenHealthConfigEntity(
    @PrimaryKey
    val userId: Long,

    @FcWomenHealthConfig.Mode val mode: Int,

    val remindDevice: Boolean,

    val remindTime: Int,

    val remindAdvance: Int,

    @FcWomenHealthConfig.RemindType
    val remindType: Int,

    val cycle: Int,

    val duration: Int,

    @field:TypeConverters(DateConverter::class)
    val latest: Date,
) {

    /**
     * @param timeline Whether use menstruation info from a timeline object
     * @param specialMode Whether need to change to the specified mode
     */
    fun toModel(timeline: MenstruationTimelineEntity? = null, specialMode: Int? = null): WomenHealthConfig {
        return WomenHealthConfig(
            mode = specialMode ?: mode,
            remindTime = remindTime,
            remindDevice = remindDevice,
            remindAdvance = remindAdvance,
            remindType = remindType,
            cycle = timeline?.cycle ?: cycle,
            duration = timeline?.duration ?: duration,
            latest = timeline?.date ?: latest
        )
    }
}

/**
 * @param userId
 * @param exist Whether need keep menstruation info from a exist object
 */
fun WomenHealthConfig.toEntity(userId: Long, exist: WomenHealthConfigEntity? = null): WomenHealthConfigEntity {
    return WomenHealthConfigEntity(
        userId = userId,
        mode = mode,
        remindTime = remindTime,
        remindDevice = remindDevice,
        remindAdvance = remindAdvance,
        remindType = remindType,
        cycle = exist?.cycle ?: cycle,
        duration = exist?.duration ?: duration,
        latest = exist?.latest ?: latest
    )
}