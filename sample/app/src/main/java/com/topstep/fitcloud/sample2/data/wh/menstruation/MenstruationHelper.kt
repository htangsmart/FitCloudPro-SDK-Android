package com.topstep.fitcloud.sample2.data.wh.menstruation

import com.topstep.fitcloud.sample2.data.db.WomenHealthDao
import com.topstep.fitcloud.sample2.model.wh.MenstruationResult
import com.topstep.fitcloud.sample2.model.wh.WomenHealthConfig
import com.topstep.fitcloud.sample2.model.wh.WomenHealthMode
import com.topstep.fitcloud.sample2.utils.DateTimeUtils
import com.topstep.fitcloud.sample2.utils.WomenHealthUtils
import com.topstep.fitcloud.sdk.v2.model.config.FcWomenHealthConfig
import java.util.*

class MenstruationHelper(
    val userId: Long,
    private val dao: WomenHealthDao,
) {
    private val cacheSegments = ArrayList<MenstruationSegment>(5)

    /**
     * There is no menstruation data before [cacheNoneDate], which is used to quickly filter invalid queries
     */
    private var cacheNoneDate: Date? = null

    private suspend fun getSegmentInfo(calendar: Calendar, select: Date): MenstruationSegment? {
        val cacheNoneDate = this.cacheNoneDate
        if (cacheNoneDate != null && select.before(cacheNoneDate)) {
            return null
        }
        var segment: MenstruationSegment? = null
        //query in cache
        for (exist in cacheSegments) {
            if (exist.isInSegment(select)) {
                segment = exist
                break
            }
        }
        if (segment == null) {
            //query in database
            segment = dao.getMenstruationSegment(userId, select, calendar)
            if (segment != null) {
                cacheSegments.add(segment)
            }
        }
        if (segment == null) {
            if (cacheNoneDate == null || cacheNoneDate.after(select)) {
                //There is no menstruation data this time
                this.cacheNoneDate = select
            }
        }
        return segment
    }

    fun clearCache() {
        cacheSegments.clear()
        cacheNoneDate = null
    }

    suspend fun changeMenstruationEndDate(calendar: Calendar, select: Date, addOrDelete: Boolean) {
        val info = getSegmentInfo(calendar, select) ?: return
        val days = DateTimeUtils.getDaysBetween(calendar, select, info.segmentBegin)
        val cycleIndex = days / info.cycle //Which cycle of this segment
        val cycleInfo = info.getCycleInfo(calendar, dao, userId, cycleIndex)
        if (addOrDelete) {
            dao.setMenstruationEndDate(userId, select, cycleInfo.cycleBegin, cycleInfo.cycleEnd)
        } else {
            dao.deleteMenstruationEndDate(userId, cycleInfo.cycleBegin, cycleInfo.cycleEnd)
        }
        info.calculate(cycleInfo, calendar, dao, userId)
    }

    suspend fun getMenstruationResult(calendar: Calendar, select: Date, remindAdvance: Int): MenstruationResult? {
        val info = getSegmentInfo(calendar, select) ?: return null
        val days: Int = DateTimeUtils.getDaysBetween(calendar, select, info.segmentBegin)
        val cycleIndex = days / info.cycle //Which cycle of this segment
        val dayInCycle = days % info.cycle + 1 //Which day of this cycle。Adding 1 is because this is the index, starting from 0. All other places are days, which should start from 1
        val cycleInfo = info.getCycleInfo(calendar, dao, userId, cycleIndex)

        val dateType: Int
        val pregnancyRate: Int
        if (dayInCycle <= cycleInfo.menstruationEndDay) {//Menstruation
            dateType = MenstruationResult.DateType.MENSTRUATION
            pregnancyRate = WomenHealthUtils.rateOfMenstruation(dayInCycle)
        } else {
            val ovulationPeriodDay = cycleInfo.ovulationPeriodDay
            if (ovulationPeriodDay == null) {//no ovulation period
                dateType = MenstruationResult.DateType.SAFE_AFTER_OVULATION
                pregnancyRate = WomenHealthUtils.rateOfSafeAfterOvulation(dayInCycle - cycleInfo.menstruationEndDay)
            } else {
                if (dayInCycle < ovulationPeriodDay) {
                    dateType = MenstruationResult.DateType.SAFE_BEFORE_OVULATION
                    pregnancyRate = WomenHealthUtils.rateOfSafeBeforeOvulation(dayInCycle - cycleInfo.menstruationEndDay)
                } else if (dayInCycle < ovulationPeriodDay + cycleInfo.ovulationPeriodLength) {
                    if (dayInCycle == cycleInfo.ovulationDay) {
                        dateType = MenstruationResult.DateType.OVULATION_DAY
                        pregnancyRate = WomenHealthUtils.rateOfOvulationDay()
                    } else {
                        dateType = MenstruationResult.DateType.OVULATION
                        pregnancyRate = WomenHealthUtils.rateOfOvulation(dayInCycle - cycleInfo.ovulationDay)
                    }
                } else {
                    dateType = MenstruationResult.DateType.SAFE_AFTER_OVULATION
                    pregnancyRate = WomenHealthUtils.rateOfSafeAfterOvulation(dayInCycle - (ovulationPeriodDay + cycleInfo.ovulationPeriodLength) + 1)
                }
            }
        }

        @MenstruationResult.OperationType val operationType = if (dayInCycle == 1) {
            null
        } else if (dayInCycle <= info.duration + 5) {
            MenstruationResult.OperationType.END
        } else {
            MenstruationResult.OperationType.BEGIN
        }

        val menstruationEndDate = cycleInfo.menstruationEndDate
        val hasSetEndDate = if (menstruationEndDate != null) {
            DateTimeUtils.isSameDay(select, menstruationEndDate)
        } else {
            false
        }

        val remindNext = if (dateType != MenstruationResult.DateType.MENSTRUATION) {
            //Non menstrual period, then it is necessary to determine whether to remind the next menstrual period time
            val nextGap: Int = cycleInfo.cycleLength - dayInCycle + 1
            if (nextGap <= remindAdvance) {
                nextGap
            } else {
                null
            }
        } else {
            null
        }

        return MenstruationResult(
            cycleBegin = cycleInfo.cycleBegin,
            cycleEnd = cycleInfo.cycleEnd,
            dayInCycle = dayInCycle,
            dateType = dateType,
            pregnancyRate = pregnancyRate,
            operationType = operationType,
            hasSetEndDate = hasSetEndDate,
            remindNext = remindNext,
        )
    }

    suspend fun getConfigForDevice(config: WomenHealthConfig?): FcWomenHealthConfig {
        val builder = FcWomenHealthConfig.Builder()
        if (config != null && config.remindDevice) {
            val calendar = Calendar.getInstance()
            builder.setMode(config.mode)
            builder.setMenstruationRemindAdvance(config.remindAdvance)
            builder.setRemindTime(config.remindTime)
            builder.setMenstruationDuration(config.duration)
            builder.setMenstruationCycle(config.cycle)
            calendar.time = config.latest
            builder.setMenstruationLatest(calendar)
            builder.setPregnancyRemindType(config.remindType)
            if (config.mode != WomenHealthMode.PREGNANCY) {
                //If it is MENSTRUATION or PREGNANCY_PREPARE , check if the end time of the current cycle has been set
                val today = Date()
                val info = getSegmentInfo(calendar, today)
                if (info != null) {
                    val days: Int = DateTimeUtils.getDaysBetween(calendar, today, info.segmentBegin)
                    val cycleIndex: Int = days / info.cycle //Which cycle is it during this period
                    val cycleInfo = info.getCycleInfo(calendar, dao, userId, cycleIndex)
                    if (cycleInfo.menstruationEndDate != null) {
                        //If have set the end time of this cycle yourself, need to adjust the config
                        calendar.time = cycleInfo.cycleBegin
                        builder.setMenstruationLatest(calendar)
                        builder.setMenstruationEndDay(cycleInfo.menstruationEndDay - 1)
                    }
                }
            }
        }
        return builder.create()
    }
}