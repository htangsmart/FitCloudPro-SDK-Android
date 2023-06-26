package com.topstep.fitcloud.sample2.data.device

import com.github.kilnn.tool.util.roundHalfUp3
import com.topstep.fitcloud.sample2.data.db.AppDatabase
import com.topstep.fitcloud.sample2.data.entity.*
import com.topstep.fitcloud.sample2.data.user.UserInfoRepository
import com.topstep.fitcloud.sample2.model.user.getStepLength
import com.topstep.fitcloud.sample2.model.user.getWeight
import com.topstep.fitcloud.sample2.utils.DateTimeUtils
import com.topstep.fitcloud.sample2.utils.km2Calories
import com.topstep.fitcloud.sample2.utils.step2Km
import com.topstep.fitcloud.sdk.v2.model.data.*
import java.util.*

interface SyncDataRepository {

    suspend fun saveStep(userId: Long, data: List<FcStepData>?, isSupportStepExtra: Boolean)

    suspend fun saveTodayStep(userId: Long, data: FcTodayTotalData?)

    suspend fun saveHeartRate(userId: Long, data: List<FcHeartRateData>?)

    suspend fun saveOxygen(userId: Long, data: List<FcOxygenData>?)

    suspend fun saveBloodPressure(userId: Long, data: List<FcBloodPressureData>?)

    suspend fun saveBloodPressureMeasure(userId: Long, data: List<FcBloodPressureMeasureData>?)

    suspend fun saveTemperature(userId: Long, data: List<FcTemperatureData>?)

    suspend fun savePressure(userId: Long, data: List<FcPressureData>?)

    suspend fun queryStep(userId: Long, date: Date): List<StepItemEntity>?

    suspend fun queryTodayStep(userId: Long): TodayStepData?

    suspend fun queryHeartRate(userId: Long, date: Date): List<HeartRateItemEntity>?

    suspend fun queryOxygen(userId: Long, date: Date): List<OxygenItemEntity>?

    suspend fun queryBloodPressure(userId: Long, date: Date): List<BloodPressureItemEntity>?

    suspend fun queryTemperature(userId: Long, date: Date): List<TemperatureItemEntity>?

    suspend fun queryPressure(userId: Long, date: Date): List<PressureItemEntity>?
}

internal class SyncDataRepositoryImpl(
    appDatabase: AppDatabase,
    private val userInfoRepository: UserInfoRepository
) : SyncDataRepository {

    private val stringTypedDao = appDatabase.stringTypedDao()
    private val syncDao = appDatabase.syncDataDao()

    override suspend fun saveStep(userId: Long, data: List<FcStepData>?, isSupportStepExtra: Boolean) {
        if (data.isNullOrEmpty()) return
        return if (isSupportStepExtra) {
            syncDao.insertStep(
                data.map { StepItemEntity(userId, Date(it.timestamp), it.step, it.distance, it.calories) }
            )
        } else {
            val userInfo = userInfoRepository.flowCurrent.value
            val stepLength = userInfo.getStepLength()
            val weight = userInfo.getWeight()
            syncDao.insertStep(
                data.map {
                    val distance = step2Km(it.step, stepLength).roundHalfUp3()
                    val calories = km2Calories(distance, weight).roundHalfUp3()
                    StepItemEntity(userId, Date(it.timestamp), it.step, distance, calories)
                }
            )
        }
    }

    override suspend fun saveTodayStep(userId: Long, data: FcTodayTotalData?) {
        if (data == null) {
            //Clear today step data
            val today = Date()
            val calendar = Calendar.getInstance()
            val start: Date = DateTimeUtils.getDayStartTime(calendar, today)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, today)
            syncDao.deleteStepBetween(userId, start, end)
            stringTypedDao.setTodayStepData(userId, null)
        } else {
            stringTypedDao.setTodayStepData(
                userId, TodayStepData(
                    data.timestamp,
                    data.step,
                    data.distance / 1000.0f,// FcTodayTotalData.distance unit is meters
                    data.calorie / 1000.0f,// FcTodayTotalData.calorie unit is calorie, not kilocalorie
                )
            )
        }
    }

    override suspend fun saveHeartRate(userId: Long, data: List<FcHeartRateData>?) {
        if (data.isNullOrEmpty()) return
        syncDao.insertHeartRate(
            data.map { HeartRateItemEntity(userId, Date(it.timestamp), it.heartRate) }
        )
    }

    override suspend fun saveOxygen(userId: Long, data: List<FcOxygenData>?) {
        if (data.isNullOrEmpty()) return
        syncDao.insertOxygen(
            data.map { OxygenItemEntity(userId, Date(it.timestamp), it.oxygen) }
        )
    }

    override suspend fun saveBloodPressure(userId: Long, data: List<FcBloodPressureData>?) {
        if (data.isNullOrEmpty()) return
        syncDao.insertBloodPressure(
            data.map { BloodPressureItemEntity(userId, Date(it.timestamp), it.sbp, it.dbp) }
        )
    }

    override suspend fun saveBloodPressureMeasure(userId: Long, data: List<FcBloodPressureMeasureData>?) {
        if (data.isNullOrEmpty()) return
        syncDao.insertBloodPressure(
            data.map { BloodPressureItemEntity(userId, Date(it.timestamp), it.sbp, it.dbp) }
        )
    }

    override suspend fun saveTemperature(userId: Long, data: List<FcTemperatureData>?) {
        if (data.isNullOrEmpty()) return
        syncDao.insertTemperature(
            data.map { TemperatureItemEntity(userId, Date(it.timestamp), it.body, it.wrist) }
        )
    }

    override suspend fun savePressure(userId: Long, data: List<FcPressureData>?) {
        if (data.isNullOrEmpty()) return
        syncDao.insertPressure(
            data.map { PressureItemEntity(userId, Date(it.timestamp), it.pressure) }
        )
    }

    override suspend fun queryStep(userId: Long, date: Date): List<StepItemEntity>? {
        val calendar = Calendar.getInstance()
        val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
        val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
        return syncDao.queryStepBetween(userId, start, end)
    }

    override suspend fun queryTodayStep(userId: Long): TodayStepData? {
        return stringTypedDao.getTodayStepData(userId)
    }

    /**
     * Query Heart Rate data for a day
     */
    override suspend fun queryHeartRate(userId: Long, date: Date): List<HeartRateItemEntity>? {
        val calendar = Calendar.getInstance()
        val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
        val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
        return syncDao.queryHeartRateBetween(userId, start, end)
    }

    override suspend fun queryOxygen(userId: Long, date: Date): List<OxygenItemEntity>? {
        val calendar = Calendar.getInstance()
        val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
        val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
        return syncDao.queryOxygenBetween(userId, start, end)
    }

    override suspend fun queryBloodPressure(userId: Long, date: Date): List<BloodPressureItemEntity>? {
        val calendar = Calendar.getInstance()
        val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
        val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
        return syncDao.queryBloodPressureBetween(userId, start, end)
    }

    override suspend fun queryTemperature(userId: Long, date: Date): List<TemperatureItemEntity>? {
        val calendar = Calendar.getInstance()
        val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
        val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
        return syncDao.queryTemperatureBetween(userId, start, end)
    }

    override suspend fun queryPressure(userId: Long, date: Date): List<PressureItemEntity>? {
        val calendar = Calendar.getInstance()
        val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
        val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
        return syncDao.queryPressureBetween(userId, start, end)
    }

}