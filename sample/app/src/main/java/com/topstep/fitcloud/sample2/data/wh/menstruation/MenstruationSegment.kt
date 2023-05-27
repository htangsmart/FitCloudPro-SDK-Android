package com.topstep.fitcloud.sample2.data.wh.menstruation

import android.util.SparseArray
import com.topstep.fitcloud.sample2.data.db.WomenHealthDao
import com.topstep.fitcloud.sample2.utils.DateTimeUtils
import java.util.*
import kotlin.math.ceil

class MenstruationSegment(
    calendar: Calendar,
    /**
     * Segment begin time(include)
     */
    val segmentBegin: Date,

    /**
     * Segment end time(exclude)
     * When it is null, it means unlimited
     */
    private val segmentEnd: Date?,

    /**
     * Days of menstruation duration per menstruation cycle
     */
    val duration: Int,

    /**
     * Total number of days per menstruation cycle.
     */
    val cycle: Int,
) {

    private val cycleCount = if (segmentEnd != null) {
        ceil(DateTimeUtils.getDaysBetween(calendar, segmentBegin, segmentEnd) / cycle.toFloat()).toInt()
    } else {
        Int.MAX_VALUE
    }

    private val cycleInfos: SparseArray<MenstruationCycle> = SparseArray()

    /**
     * Is this [date] in this segment
     */
    fun isInSegment(date: Date): Boolean {
        return if (date.before(segmentBegin)) {
            false
        } else {
            segmentEnd == null || date.before(segmentEnd)
        }
    }

    suspend fun getCycleInfo(calendar: Calendar, dao: WomenHealthDao, userId: Long, cycleIndex: Int): MenstruationCycle {
        var cycleInfo = cycleInfos.get(cycleIndex)
        if (cycleInfo == null) {
            //Cycle begin time(include)
            val cycleBegin = DateTimeUtils.getDateBetween(calendar, segmentBegin, cycleIndex * cycle)
            //Cycle end time(exclude)
            var cycleEnd = DateTimeUtils.getDateBetween(calendar, cycleBegin, cycle)

            val cycleLength: Int
            //Calculation of cycle length
            if (cycleIndex == cycleCount - 1 && segmentEnd != null) {
                //The last cycle may not be long enough due to the end limit
                val overDays = DateTimeUtils.getDaysBetween(calendar, cycleEnd, segmentEnd) //Days exceeded
                cycleLength = cycle - overDays //Actual cycle length
                cycleEnd = segmentEnd
            } else {
                cycleLength = cycle
            }
            cycleInfo = MenstruationCycle(cycleBegin, cycleEnd, cycleLength)
            calculate(cycleInfo, calendar, dao, userId)
            cycleInfos.put(cycleIndex, cycleInfo)
        }
        return cycleInfo
    }

    suspend fun calculate(cycleInfo: MenstruationCycle, calendar: Calendar, dao: WomenHealthDao, userId: Long) {
        //Calculate the menstruationEndDate
        val menstruationEndDate = dao.queryMenstruationEndDate(userId, cycleInfo.cycleBegin, cycleInfo.cycleEnd).also {
            cycleInfo.menstruationEndDate = it
        }
        if (menstruationEndDate == null) {
            cycleInfo.menstruationEndDay = duration
        } else {
            cycleInfo.menstruationEndDay = DateTimeUtils.getDaysBetween(calendar, cycleInfo.cycleBegin, menstruationEndDate) + 1
        }
        if (cycleInfo.menstruationEndDay > cycleInfo.cycleLength) {
            cycleInfo.menstruationEndDay = cycleInfo.cycleLength
        }

        //Calculate ovulation
        cycleInfo.ovulationDay = cycleInfo.cycleLength - 14 + 1
        val remainingLength = cycleInfo.cycleLength - cycleInfo.menstruationEndDay
        if (remainingLength <= 9) {
            //No ovulation
            cycleInfo.ovulationPeriodDay = null
            cycleInfo.ovulationPeriodLength = 0
        } else if (remainingLength < 19) {
            //The number of days of ovulation is less than 10
            cycleInfo.ovulationPeriodDay = cycleInfo.menstruationEndDay + 1
            cycleInfo.ovulationPeriodLength = 10 - (19 - remainingLength)
        } else {
            cycleInfo.ovulationPeriodDay = cycleInfo.cycleLength - 19 + 1
            cycleInfo.ovulationPeriodLength = 10
        }
    }

}