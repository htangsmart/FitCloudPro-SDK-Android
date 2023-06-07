package com.topstep.fitcloud.sample2.model.wh

import androidx.annotation.IntDef
import com.topstep.fitcloud.sample2.model.wh.MenstruationResult.DateType
import java.util.*

data class MenstruationResult(
    /**
     * The start time of this cycle, including this time
     */
    val cycleBegin: Date,

    /**
     * The end time of this cycle, excluding this time
     */
    val cycleEnd: Date,

    /**
     * Represents the day of this cycle
     */
    val dayInCycle: Int,

    @DateType val dateType: Int,
    /**
     * Pregnancy rate, 80 represents 80%
     */
    val pregnancyRate: Int,

    @OperationType val operationType: Int?,

    /**
     * Has this date been set to an end date
     */
    val hasSetEndDate: Boolean,

    /**
     * 当[dateType]!=[DateType.MENSTRUATION]有效.
     * Reminder the time from the next menstrual period, [1-3] is valid data. Null means not remind
     */
    val remindNext: Int?,

    ) {

    fun isInCycle(date: Date): Boolean {
        return date.after(cycleBegin) && date.before(cycleEnd)
    }

    @IntDef(
        DateType.MENSTRUATION,
        DateType.SAFE_BEFORE_OVULATION,
        DateType.SAFE_AFTER_OVULATION,
        DateType.OVULATION,
        DateType.OVULATION_DAY,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class DateType {
        companion object {
            /**
             * Menstruation
             */
            const val MENSTRUATION = 1

            /**
             * Safe period (before ovulation)
             */
            const val SAFE_BEFORE_OVULATION = 2

            /**
             * Safe period (after ovulation)
             */
            const val SAFE_AFTER_OVULATION = 3

            /**
             * period of ovulation
             */
            const val OVULATION = 4

            /**
             * ovulation day
             */
            const val OVULATION_DAY = 5
        }
    }

    @IntDef(
        OperationType.END,
        OperationType.BEGIN,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class OperationType {
        companion object {
            /**
             * Can be set to end
             */
            const val END = 1

            /**
             * Can be set to start
             */
            const val BEGIN = 2
        }
    }

}