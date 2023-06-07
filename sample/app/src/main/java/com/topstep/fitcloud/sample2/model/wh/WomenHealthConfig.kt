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
    @WomenHealthMode val mode: Int,
    /**
     * Whether enable device reminder
     */
    val remindDevice: Boolean = true,

    /**
     * What time does the device remind.
     * Minutes relative to 0, for example, time 11:30 is a int value 11*60+30=690
     */
    val remindTime: Int = 21 * 60,

    /**
     * How many days in advance to remind menstruation is coming
     */
    val remindAdvance: Int = 1,

    /**
     * Pregnancy remind type
     */
    @FcWomenHealthConfig.RemindType
    val remindType: Int = FcWomenHealthConfig.RemindType.PREGNANCY_DAYS,

    /**
     * Total number of days per menstruation cycle.
     */
    val cycle: Int = 28,

    /**
     * Days of menstruation duration per menstruation cycle
     * When mode is [WomenHealthMode.PREGNANCY], this value is ignored.
     */
    val duration: Int = 7,

    /**
     * When mode is [WomenHealthMode.PREGNANCY], this value represents the date of the last menstruation
     * When mode is [WomenHealthMode.MENSTRUATION] or [WomenHealthMode.PREGNANCY_PREPARE], this value represents the start date of the last menstruation cycle.
     */
    val latest: Date = Date(),
)