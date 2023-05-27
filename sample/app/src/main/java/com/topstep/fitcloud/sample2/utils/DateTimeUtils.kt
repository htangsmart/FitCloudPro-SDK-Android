package com.topstep.fitcloud.sample2.utils

import java.util.*

object DateTimeUtils {

    /**
     * Obtain the start time of the day where [time] is located
     * @param dayOffset   0 represents today, 1 represents tomorrow, and -1 represents yesterday.
     */
    fun getDayStartTime(calendar: Calendar, time: Date, dayOffset: Int = 0): Date {
        calendar.time = time
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0

        if (dayOffset != 0) {
            calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + dayOffset
        }

        return calendar.time
    }

    /**
     * Obtain the end time of the day where [time] is located
     * @param dayOffset   0 represents today, 1 represents tomorrow, and -1 represents yesterday.
     */
    fun getDayEndTime(calendar: Calendar, time: Date, dayOffset: Int = 0): Date {
        calendar.time = time
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MILLISECOND] = 999

        if (dayOffset != 0) {
            calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + dayOffset
        }

        return calendar.time
    }

    /**
     * Determine whether a certain date is the current day
     */
    fun isToday(date: Date): Boolean {
        return isSameDay(date, Date())
    }

    /**
     * Check if two dates are the same day
     */
    @Suppress("DEPRECATION")
    fun isSameDay(date1: Date, date2: Date): Boolean {
        return date1.year == date2.year &&
                date1.month == date2.month &&
                date1.date == date2.date
    }

    /**
     * Get the date that is different from a certain date by how many days
     *
     * @param calendar    Calendar object for calculate
     * @param date        Date for calculate
     * @param daysBetween Days. A positive number indicates the date after the [date], and a negative number indicates the date before the [date]
     * @return Result date
     */
    fun getDateBetween(calendar: Calendar, date: Date, daysBetween: Int): Date {
        calendar.time = date
        calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + daysBetween
        return calendar.time
    }

    /**
     * Judge whether [date1] is earlier than [date2]
     * @param calendar Calendar object for calculate
     * @param date1    date1
     * @param date2    date2
     * @return True for before, False for not.
     */
    fun isDateBefore(calendar: Calendar, date1: Date, date2: Date): Boolean {
        calendar.time = date1
        val year1 = calendar[Calendar.YEAR]
        val day1 = calendar[Calendar.DAY_OF_YEAR]
        calendar.time = date2
        val year2 = calendar[Calendar.YEAR]
        val day2 = calendar[Calendar.DAY_OF_YEAR]
        return if (year1 < year2) {
            true
        } else if (year1 == year2) {
            day1 < day2
        } else {
            false
        }
    }

    /**
     * Calculate the total number of days between two dates.
     * For example:2022-12-01, 2022-12-02, will return 1.
     *
     * @param calendar Calendar object for calculate
     * @param date1    Date1
     * @param date2    Date2
     * @return Total number of days
     */
    fun getDaysBetween(calendar: Calendar, date1: Date, date2: Date): Int {
        val start: Date
        val end: Date
        if (date1.before(date2)) {
            start = date1
            end = date2
        } else {
            start = date2
            end = date1
        }
        calendar.time = start
        val startDay = calendar[Calendar.DAY_OF_YEAR]
        val startYear = calendar[Calendar.YEAR]
        calendar.time = end
        val endDay = calendar[Calendar.DAY_OF_YEAR]
        val endYear = calendar[Calendar.YEAR]
        return if (startYear != endYear) {
            var timeDistance = 0
            for (i in startYear until endYear) {
                timeDistance += if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {
                    366
                } else {
                    365
                }
            }
            timeDistance + (endDay - startDay)
        } else {
            endDay - startDay
        }
    }
}