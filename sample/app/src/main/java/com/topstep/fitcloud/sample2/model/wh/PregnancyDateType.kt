package com.topstep.fitcloud.sample2.model.wh

import androidx.annotation.IntDef

@IntDef(
    PregnancyDateType.EARLY,
    PregnancyDateType.MIDDLE,
    PregnancyDateType.LATER,
)
@Retention(AnnotationRetention.SOURCE)
annotation class PregnancyDateType {
    companion object {

        /**
         * First trimester
         */
        const val EARLY = 1

        /**
         * second trimester
         */
        const val MIDDLE = 2

        /**
         * third trimester
         */
        const val LATER = 3
    }
}