package com.topstep.fitcloud.sample2.model.wh

import androidx.annotation.IntDef
import com.topstep.fitcloud.sdk.v2.model.config.FcWomenHealthConfig
import java.util.*

/**
 * Define the mode use for ui.
 * Don't contains mode of [FcWomenHealthConfig.Mode.NONE]
 */
@IntDef(
    WomenHealthMode.MENSTRUATION,
    WomenHealthMode.PREGNANCY_PREPARE,
    WomenHealthMode.PREGNANCY,
)
@Retention(AnnotationRetention.SOURCE)
annotation class WomenHealthMode {
    companion object {
        const val MENSTRUATION = FcWomenHealthConfig.Mode.MENSTRUATION
        const val PREGNANCY_PREPARE = FcWomenHealthConfig.Mode.PREGNANCY_PREPARE
        const val PREGNANCY = FcWomenHealthConfig.Mode.PREGNANCY
    }
}

data class WomenHealthConfig(

    /**
     * Corresponding [FcWomenHealthConfig.getMode]
     */
    @WomenHealthMode val mode: Int,

    /**
     * Whether enabled device women health reminder.
     *
     * If it is false, then women health only enabled on the APP, and the device disabled this feature by set [FcWomenHealthConfig.Mode.NONE]
     */
    val remindDevice: Boolean = true,

    /***
     * What time does the device remind.
     * Minutes relative to 0, for example, time 11:30 is a int value 11*60+30=690
     *
     * Corresponding [FcWomenHealthConfig.getRemindTime]
     */
    val remindTime: Int = 21 * 60,

    /**
     * How many days in advance to remind menstruation is coming
     *
     * Corresponding [FcWomenHealthConfig.getMenstruationRemindAdvance]
     */
    val remindAdvance: Int = 1,

    /**
     * Pregnancy remind type
     *
     * Corresponding [FcWomenHealthConfig.getPregnancyRemindType]
     */
    @FcWomenHealthConfig.RemindType
    val remindType: Int = FcWomenHealthConfig.RemindType.PREGNANCY_DAYS,

    /**
     * Total number of days per menstruation cycle.
     *
     * Corresponding [FcWomenHealthConfig.getMenstruationCycle]
     */
    val cycle: Int = 28,

    /**
     * Days of menstruation duration per menstruation cycle
     * When mode is [WomenHealthMode.PREGNANCY], this value is ignored.
     *
     * Corresponding [FcWomenHealthConfig.getMenstruationDuration]
     */
    val duration: Int = 7,

    /**
     * When mode is [WomenHealthMode.PREGNANCY], this value represents the date of the last menstruation
     * When mode is [WomenHealthMode.MENSTRUATION] or [WomenHealthMode.PREGNANCY_PREPARE], this value represents the start date of the last menstruation cycle.
     *
     * Corresponding [FcWomenHealthConfig.getMenstruationLatest]
     */
    val latest: Date = Date(),
)