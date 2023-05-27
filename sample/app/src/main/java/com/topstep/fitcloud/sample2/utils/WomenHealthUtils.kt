package com.topstep.fitcloud.sample2.utils

import com.topstep.fitcloud.sample2.model.wh.PregnancyDateType
import com.topstep.fitcloud.sample2.utils.DateTimeUtils.getDateBetween
import com.topstep.fitcloud.sample2.utils.DateTimeUtils.getDaysBetween
import com.topstep.fitcloud.sample2.utils.DateTimeUtils.isDateBefore
import java.util.*

object WomenHealthUtils {
    /**
     * Calculate the due date by last menstruation
     *
     * @param calendar Calendar object for calculate
     * @param latest   Date of the last menstruation
     * @param cycle    Menstruation cycle
     * @return Due date
     */
    fun calculateDueDate(calendar: Calendar, latest: Date, cycle: Int): Date {
        return if (cycle > 28) {
            getDateBetween(calendar, latest, 280)
        } else {
            getDateBetween(calendar, latest, cycle + 252)
        }
    }

    /**
     * Calculate the last menstruation by due date
     *
     * @param calendar Calendar object for calculate
     * @param dueDate  Due date
     * @param cycle    Menstruation cycle
     * @return Date of the last menstruation
     */
    fun calculateLatestMenstruation(calendar: Calendar, dueDate: Date, cycle: Int): Date {
        return if (cycle > 28) {
            getDateBetween(calendar, dueDate, -280)
        } else {
            getDateBetween(calendar, dueDate, -(cycle + 252))
        }
    }

    /**
     * Calculate the days from [target] to the expected delivery date
     *
     * @param calendar Calendar object for calculate
     * @param latest   Date of the last menstruation
     * @param cycle    Menstruation cycle
     * @param target
     * @return Null means not pregnant, data invalid
     */
    fun getDueDays(calendar: Calendar, latest: Date, cycle: Int, target: Date): Int? {
        if (isDateBefore(calendar, target, latest)) return null
        val days = getDaysBetween(calendar, latest, target)
        val limit = if (cycle > 28) 280 else cycle + 252
        return if (days > limit) -1 else limit - days
    }

    /**
     * Number of days pregnant
     *
     * @param calendar Calendar object for calculate
     * @param latest   Date of the last menstruation
     * @param cycle    Menstruation cycle
     * @param target
     * @return Null means data invalid
     */
    fun getPregnancyDays(calendar: Calendar, latest: Date, cycle: Int, target: Date): Int? {
        if (isDateBefore(calendar, target, latest)) return null
        val days = getDaysBetween(calendar, latest, target)
        val limit = if (cycle > 28) 280 else cycle + 252
        return if (days > limit) -1 else days + 1
    }

    /**
     * Date type of pregnancy
     *
     * @param calendar Calendar object for calculate
     * @param latest   Date of the last menstruation
     * @param cycle    Menstruation cycle
     * @param target
     * @return Null means data invalid
     */
    @PregnancyDateType
    fun getPregnancyDateType(calendar: Calendar, latest: Date, cycle: Int, target: Date): Int? {
        if (target.before(latest)) return null
        val days = getDaysBetween(calendar, latest, target)
        val limit = if (cycle > 28) 280 else cycle + 252
        return if (days <= 84) {
            PregnancyDateType.EARLY
        } else if (days <= 189) {
            PregnancyDateType.MIDDLE
        } else if (days <= limit) {
            PregnancyDateType.LATER
        } else {
            null
        }
    }

    /**
     * Obtaining pregnancy rates during menstruation
     *
     * @param days What day of menstruation
     */
    fun rateOfMenstruation(days: Int): Int {
        return when (days % 3) {
            1 -> 3
            2 -> 4
            else -> 2
        }
    }

    /**
     * Obtaining pregnancy rates during the safe period (after ovulation)
     *
     * @param days What day is the safe period (after ovulation)
     */
    fun rateOfSafeAfterOvulation(days: Int): Int {
        return 7.coerceAtLeast(35 - (days - 1) * 2)
    }

    /**
     * Obtaining pregnancy rates during the safe period (before ovulation)
     *
     * @param days What day is the safe period (before ovulation)
     */
    fun rateOfSafeBeforeOvulation(days: Int): Int {
        return 30.coerceAtMost(12 + (days - 1) * 2)
    }

    /**
     * Pregnancy rate on ovulation day
     */
    fun rateOfOvulationDay(): Int {
//        val rates = intArrayOf(35, 48, 66, 74, 88, 90, 87, 72, 51, 43)
//        val index = 5
//        return rates[index]
        return 90
    }

    /**
     * Ovulation pregnancy rate
     * @param days Days from ovulation date
     */
    fun rateOfOvulation(days: Int): Int {
        val rates = intArrayOf(35, 48, 66, 74, 88, 90, 87, 72, 51, 43)
        var index = 5 + days
        if (index < 0) {
            index = 0
        } else if (index > rates.size - 1) {
            index = rates.size - 1
        }
        return rates[index]
    }
}