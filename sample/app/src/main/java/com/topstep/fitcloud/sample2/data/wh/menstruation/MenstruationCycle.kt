package com.topstep.fitcloud.sample2.data.wh.menstruation

import java.util.*

class MenstruationCycle(
    /**
     * Cycle begin time(include)
     */
    val cycleBegin: Date,
    /**
     * Cycle end time(exclude)
     */
    val cycleEnd: Date,

    /**
     * Cycle length
     */
    val cycleLength: Int
) {
    /**
     * Null if the end date is not set
     */
    var menstruationEndDate: Date? = null

    /**
     * Which day in the menstruation cycle is the end menstruation date
     */
    var menstruationEndDay = 0

    /**
     * Which day in the menstruation cycle is ovulation period begin
     * If it is null, means no ovulation period
     */
    var ovulationPeriodDay: Int? = null

    /**
     * How many days is the ovulation period
     */
    var ovulationPeriodLength = 0

    /**
     * Which day in the menstruation cycle is ovulation day
     * <=0 means no ovulation day
     */
    var ovulationDay: Int = 0

}