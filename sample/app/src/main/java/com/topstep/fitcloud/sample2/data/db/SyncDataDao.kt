package com.topstep.fitcloud.sample2.data.db

import androidx.room.*
import com.topstep.fitcloud.sample2.data.entity.*
import com.topstep.fitcloud.sample2.utils.room.TimeConverter
import com.topstep.fitcloud.sample2.utils.room.UUIDConverter
import com.topstep.fitcloud.sdk.v2.model.data.*
import java.util.*

@Dao
abstract class SyncDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStep(items: List<StepItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSleep(items: List<SleepItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHeartRate(items: List<HeartRateItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOxygen(items: List<OxygenItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBloodPressure(items: List<BloodPressureItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTemperature(items: List<TemperatureItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPressure(items: List<PressureItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEcg(items: List<EcgRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertGame(items: List<GameRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSport(items: List<SportRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertGps(items: List<SportGpsEntity>)

    @Query("DELETE FROM StepItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end")
    abstract suspend fun deleteStepBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date)

    @Query("SELECT * FROM StepItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end ORDER BY time ASC")
    abstract suspend fun queryStepBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date): List<StepItemEntity>?

    @Query("DELETE FROM SleepItemEntity WHERE userId=:userId AND time=:time AND startTime>=:start")
    abstract suspend fun deleteSleepAfter(userId: Long, @TypeConverters(TimeConverter::class) time: Date, @TypeConverters(TimeConverter::class) start: Date)

    @Query("SELECT * FROM SleepItemEntity WHERE userId=:userId AND time=:time")
    abstract suspend fun querySleep(userId: Long, @TypeConverters(TimeConverter::class) time: Date): List<SleepItemEntity>?

    @Query("SELECT * FROM HeartRateItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end ORDER BY time ASC")
    abstract suspend fun queryHeartRateBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date): List<HeartRateItemEntity>?

    @Query("SELECT * FROM OxygenItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end ORDER BY time ASC")
    abstract suspend fun queryOxygenBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date): List<OxygenItemEntity>?

    @Query("SELECT * FROM BloodPressureItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end ORDER BY time ASC")
    abstract suspend fun queryBloodPressureBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date): List<BloodPressureItemEntity>?

    @Query("SELECT * FROM TemperatureItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end ORDER BY time ASC")
    abstract suspend fun queryTemperatureBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date): List<TemperatureItemEntity>?

    @Query("SELECT * FROM PressureItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end ORDER BY time ASC")
    abstract suspend fun queryPressureBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date): List<PressureItemEntity>?

    @Query("SELECT * FROM EcgRecordEntity WHERE userId=:userId ORDER BY time DESC")
    abstract suspend fun queryEcg(userId: Long): List<EcgRecordEntity>?

    @Query("SELECT * FROM GameRecordEntity WHERE userId=:userId ORDER BY time DESC")
    abstract suspend fun queryGame(userId: Long): List<GameRecordEntity>?

    @Query("SELECT sportId FROM SportRecordEntity WHERE userId=:userId AND gpsId=:gpsId")
    @TypeConverters(UUIDConverter::class)
    abstract suspend fun querySportIdByGpsId(userId: Long, gpsId: String): UUID?

    /**
     * Clear gpsId if has associated GPS data
     */
    @Query("UPDATE SportRecordEntity SET gpsId=NULL WHERE sportId=:sportId")
    abstract suspend fun clearGpsId(@TypeConverters(UUIDConverter::class) sportId: UUID)

    /**
     * Clear all gpsId every time synchronization is completed
     */
    @Query("UPDATE SportRecordEntity SET gpsId=NULL WHERE userId=:userId")
    abstract suspend fun clearGpsId(userId: Long)

    /**
     * Only display devices with null gpsId
     */
    @Query("SELECT * FROM SportRecordEntity WHERE userId=:userId AND gpsId is NULL ORDER BY time DESC")
    abstract suspend fun querySport(userId: Long): List<SportRecordEntity>?
}