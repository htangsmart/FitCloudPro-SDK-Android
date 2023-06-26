package com.topstep.fitcloud.sample2.data.db

import androidx.room.*
import com.topstep.fitcloud.sample2.data.entity.*
import com.topstep.fitcloud.sample2.utils.room.TimeConverter
import com.topstep.fitcloud.sdk.v2.model.data.*
import java.util.*

@Dao
abstract class SyncDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStep(items: List<StepItemEntity>)

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

    @Query("DELETE FROM StepItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end")
    abstract suspend fun deleteStepBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date)

    @Query("SELECT * FROM StepItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end ORDER BY time ASC")
    abstract suspend fun queryStepBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date): List<StepItemEntity>?

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

}