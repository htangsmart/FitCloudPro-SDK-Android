package com.topstep.fitcloud.sample2.data.device

import com.github.kilnn.tool.util.roundDown1
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

    suspend fun saveSleep(userId: Long, data: List<FcSleepData>?)

    suspend fun saveHeartRate(userId: Long, data: List<FcHeartRateData>?)

    suspend fun saveOxygen(userId: Long, data: List<FcOxygenData>?)

    suspend fun saveBloodPressure(userId: Long, data: List<FcBloodPressureData>?)

    suspend fun saveBloodPressureMeasure(userId: Long, data: List<FcBloodPressureMeasureData>?)

    suspend fun saveTemperature(userId: Long, data: List<FcTemperatureData>?)

    suspend fun savePressure(userId: Long, data: List<FcPressureData>?)

    suspend fun saveEcg(userId: Long, data: List<FcEcgData>?, isTiEcg: Boolean)

    suspend fun saveGame(userId: Long, data: List<FcGameData>?)

    suspend fun saveSport(userId: Long, data: List<FcSportData>?)

    suspend fun saveGps(userId: Long, data: List<FcGpsData>?)

    suspend fun clearSportGpsId(userId: Long)

    suspend fun queryStep(userId: Long, date: Date): List<StepItemEntity>?

    suspend fun queryTodayStep(userId: Long): TodayStepData?

    suspend fun querySleep(userId: Long, date: Date): List<SleepItemEntity>?

    suspend fun queryHeartRate(userId: Long, date: Date): List<HeartRateItemEntity>?

    suspend fun queryOxygen(userId: Long, date: Date): List<OxygenItemEntity>?

    suspend fun queryBloodPressure(userId: Long, date: Date): List<BloodPressureItemEntity>?

    suspend fun queryTemperature(userId: Long, date: Date): List<TemperatureItemEntity>?

    suspend fun queryPressure(userId: Long, date: Date): List<PressureItemEntity>?

    suspend fun queryEcg(userId: Long): List<EcgRecordEntity>?

    suspend fun queryGame(userId: Long): List<GameRecordEntity>?

    suspend fun querySport(userId: Long): List<SportRecordEntity>?
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

    override suspend fun saveSleep(userId: Long, data: List<FcSleepData>?) {
        if (data.isNullOrEmpty()) return
        data.forEach {
            val time = Date(it.timestamp)
            val entities = it.items.map { item ->
                SleepItemEntity(0, userId, time, Date(item.startTime), Date(item.endTime), item.status)
            }
            if (entities.isNotEmpty()) {
                //Remove data that may conflict on this day, such as data obtained from two different device
                val firstStartTime = entities.first().startTime
                syncDao.deleteSleepAfter(userId, time, firstStartTime)
                syncDao.insertSleep(entities)
            }
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

    override suspend fun saveEcg(userId: Long, data: List<FcEcgData>?, isTiEcg: Boolean) {
        if (data.isNullOrEmpty()) return
        syncDao.insertEcg(
            data.map {
                EcgRecordEntity(
                    userId = userId,
                    ecgId = UUID.randomUUID(),
                    time = Date(it.timestamp),
                    type = if (isTiEcg) {
                        EcgRecordEntity.Type.TI
                    } else {
                        EcgRecordEntity.Type.NORMAL
                    },
                    samplingRate = it.samplingRate,
                    detail = it.items,
                )
            }
        )
    }

    override suspend fun saveGame(userId: Long, data: List<FcGameData>?) {
        if (data.isNullOrEmpty()) return
        syncDao.insertGame(
            data.map {
                GameRecordEntity(
                    userId = userId,
                    gameId = UUID.randomUUID(),
                    time = Date(it.timestamp),
                    type = it.type,
                    duration = it.duration,
                    score = it.score,
                    level = it.level,
                )
            }
        )
    }

    /**
     * Filter sport.
     *
     * Data that is too short in time or not exercising enough will not be saved.
     */
    private fun filterSport(sportData: FcSportData): Boolean {
        if (sportData.duration <= 300) {
            return false
        }
        val isMainAttrDistance = SportRecordEntity.getSportMainAttr(sportData.type) == SportRecordEntity.SportMainAttr.DISTANCE
        if (isMainAttrDistance) {
            if (sportData.distance <= 0.1f) {//<=0.1km
                return false
            }
        } else {
            if (sportData.calories <= 5) {//<=5kCal
                return false
            }
        }
        return true
    }

    override suspend fun saveSport(userId: Long, data: List<FcSportData>?) {
        if (data.isNullOrEmpty()) return
        syncDao.insertSport(
            data.filter {
                filterSport(it)
            }.map {
                val sportId = UUID.randomUUID()
                val time = Date(it.timestamp)
                SportRecordEntity(
                    userId = userId,
                    sportId = sportId,
                    time = time,
                    duration = it.duration,
                    distance = it.distance.roundDown1(),
                    calorie = it.calories.roundDown1(),
                    step = it.steps,
                    climb = 0f,
                    sportType = it.type,
                    gpsId = it.sportId//ID associated with GPS data
                )
            }
        )
    }

    override suspend fun saveGps(userId: Long, data: List<FcGpsData>?) {
        if (data.isNullOrEmpty()) return
        data.forEach {
            val sportId = syncDao.querySportIdByGpsId(userId, it.sportId)
            if (sportId != null) {
                syncDao.insertGps(
                    it.items.map { item ->
                        SportGpsEntity(
                            id = 0,
                            sportId = sportId,
                            duration = item.duration,
                            lng = item.lng,
                            lat = item.lat,
                            altitude = item.altitude,
                            satellites = item.satellites,
                            isRestart = item.isRestart,
                        )
                    }
                )
                //FcSportData is associated with GPS data,then clear the gpsId
                syncDao.clearGpsId(sportId)
            }
        }
    }

    override suspend fun clearSportGpsId(userId: Long) {
        syncDao.clearGpsId(userId)
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

    override suspend fun querySleep(userId: Long, date: Date): List<SleepItemEntity>? {
        val calendar = Calendar.getInstance()
        val time = DateTimeUtils.getDayStartTime(calendar, date)
        return syncDao.querySleep(userId, time)
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

    override suspend fun queryEcg(userId: Long): List<EcgRecordEntity>? {
        return syncDao.queryEcg(userId)
    }

    override suspend fun queryGame(userId: Long): List<GameRecordEntity>? {
        return syncDao.queryGame(userId)
    }

    override suspend fun querySport(userId: Long): List<SportRecordEntity>? {
        return syncDao.querySport(userId)
    }

}