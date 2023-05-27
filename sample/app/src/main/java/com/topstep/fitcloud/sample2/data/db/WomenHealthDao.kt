package com.topstep.fitcloud.sample2.data.db

import androidx.room.*
import com.topstep.fitcloud.sample2.data.entity.*
import com.topstep.fitcloud.sample2.data.wh.menstruation.MenstruationSegment
import com.topstep.fitcloud.sample2.model.wh.WomenHealthConfig
import com.topstep.fitcloud.sample2.model.wh.WomenHealthMode
import com.topstep.fitcloud.sample2.utils.room.DateConverter
import com.topstep.fitcloud.sdk.v2.model.config.FcWomenHealthConfig
import kotlinx.coroutines.flow.*
import java.util.*

@Dao
abstract class WomenHealthDao {

    /*WomenHealthConfigEntity*/
    @Query("SELECT * FROM WomenHealthConfigEntity WHERE userId=:userId")
    protected abstract fun flowBaseConfig(userId: Long): Flow<WomenHealthConfigEntity?>

    @Query("SELECT * FROM WomenHealthConfigEntity WHERE userId=:userId")
    protected abstract suspend fun queryBaseConfig(userId: Long): WomenHealthConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun saveBaseConfig(entity: WomenHealthConfigEntity)

    /*MenstruationTimelineEntity*/
    @Query("DELETE FROM MenstruationTimelineEntity WHERE userId=:userId AND date>=:date")
    protected abstract suspend fun deleteOldTimeline(userId: Long, @TypeConverters(DateConverter::class) date: Date)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun saveTimeline(timeline: MenstruationTimelineEntity)

    @Query("SELECT * FROM MenstruationTimelineEntity WHERE userId=:userId AND type=${MenstruationTimelineEntity.Type.SEGMENT_BEGIN} ORDER BY date DESC LIMIT 1")
    protected abstract fun flowLatestSegmentBegin(userId: Long): Flow<MenstruationTimelineEntity?>

    @Query("SELECT * FROM MenstruationTimelineEntity WHERE userId=:userId AND type=${MenstruationTimelineEntity.Type.SEGMENT_BEGIN} AND date<=:date ORDER BY date DESC LIMIT 1")
    protected abstract suspend fun querySegmentBegin(userId: Long, @TypeConverters(DateConverter::class) date: Date): MenstruationTimelineEntity?

    @Query("SELECT date FROM MenstruationTimelineEntity WHERE userId=:userId AND type=${MenstruationTimelineEntity.Type.SEGMENT_BEGIN} AND date>:begin ORDER BY date ASC LIMIT 1")
    @TypeConverters(DateConverter::class)
    protected abstract suspend fun querySegmentEnd(userId: Long, @TypeConverters(DateConverter::class) begin: Date): Date?

    @Query("SELECT date FROM MenstruationTimelineEntity WHERE userId=:userId AND type=${MenstruationTimelineEntity.Type.CYCLE_END} AND date>:cycleBegin AND date<:cycleEnd")
    @TypeConverters(DateConverter::class)
    abstract suspend fun queryMenstruationEndDate(userId: Long, @TypeConverters(DateConverter::class) cycleBegin: Date, @TypeConverters(DateConverter::class) cycleEnd: Date): Date?

    @Query("DELETE FROM MenstruationTimelineEntity WHERE userId=:userId AND type=${MenstruationTimelineEntity.Type.CYCLE_END} AND date>:cycleBegin AND date<:cycleEnd")
    abstract suspend fun deleteMenstruationEndDate(userId: Long, @TypeConverters(DateConverter::class) cycleBegin: Date, @TypeConverters(DateConverter::class) cycleEnd: Date)

    @Transaction
    open suspend fun getMenstruationSegment(userId: Long, date: Date, calendar: Calendar): MenstruationSegment? {
        val timeline = querySegmentBegin(userId, date) ?: return null
        val endDate = querySegmentEnd(userId, timeline.date)
        return MenstruationSegment(calendar, timeline.date, endDate, timeline.duration, timeline.cycle)
    }

    @Transaction
    open suspend fun setMenstruationEndDate(userId: Long, date: Date, cycleBegin: Date, cycleEnd: Date) {
        deleteMenstruationEndDate(userId, cycleBegin, cycleEnd)
        val timeline = MenstruationTimelineEntity(
            id = 0,
            userId = userId,
            type = MenstruationTimelineEntity.Type.CYCLE_END,
            date = date,
            cycle = 0,
            duration = 0,
        )
        saveTimeline(timeline)
    }

    /**
     * Flow a special user's WomenHealthConfig
     *
     * Emit null if mode is [FcWomenHealthConfig.Mode.NONE] for save memory
     *
     */
    fun flowWomenHealthConfig(userId: Long): Flow<WomenHealthConfig?> {
        return flowBaseConfig(userId).flatMapLatest { config ->
            if (config == null || config.mode == FcWomenHealthConfig.Mode.NONE) {
                flowOf(null)
            } else if (config.mode == FcWomenHealthConfig.Mode.PREGNANCY) {
                flowOf(config.toModel())
            } else {
                //Use latest timeline menstruation info replace
                flowLatestSegmentBegin(userId).map { timeline ->
                    config.toModel(timeline)
                }
            }
        }
    }

    /**
     * Like [flowWomenHealthConfig] but modify the config mode
     */
    suspend fun getWomenHealthConfigByMode(userId: Long, @WomenHealthMode mode: Int): WomenHealthConfig? {
        val config = queryBaseConfig(userId) ?: return null
        return if (mode == WomenHealthMode.PREGNANCY) {
            config.toModel(null, mode)
        } else {
            val timeline = flowLatestSegmentBegin(userId).first()
            config.toModel(timeline, mode)
        }
    }

    /**
     * @return Is menstruation timeline changed
     */
    @Transaction
    open suspend fun setWomenHealthConfig(userId: Long, config: WomenHealthConfig?): Boolean {
        if (config == null) {
            val exist = queryBaseConfig(userId)
            if (exist == null || exist.mode == FcWomenHealthConfig.Mode.NONE) {
                //no changes
                return false
            }
            //Set mode to Mode.None. Other attributes remain unchanged
            val result = exist.copy(mode = FcWomenHealthConfig.Mode.NONE)
            saveBaseConfig(result)
            return false
        }

        //If new config is Mode.PREGNANCY, save directly
        if (config.mode == WomenHealthMode.PREGNANCY) {
            saveBaseConfig(config.toEntity(userId))
            return false
        }

        val exist = queryBaseConfig(userId)
        //If new config is Mode.MENSTRUATION or Mode.PREGNANCY_PREPARE
        //Keep menstruation info in the exist config.
        saveBaseConfig(config.toEntity(userId, exist))
        //And save new menstruation info to MenstruationTimelineEntity
        deleteOldTimeline(userId, config.latest)
        val timeline = MenstruationTimelineEntity(
            id = 0,
            userId = userId,
            type = MenstruationTimelineEntity.Type.SEGMENT_BEGIN,
            date = config.latest,
            cycle = config.cycle,
            duration = config.duration
        )
        saveTimeline(timeline)
        return true
    }
}