package com.topstep.fitcloud.sample2.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object FormatterUtil {

    lateinit var systemLocale: Locale
        private set
    private lateinit var DECIMAL_1_FORMAT: DecimalFormat
    private lateinit var DECIMAL_2_FORMAT: DecimalFormat

    fun init(locale: Locale) {
        systemLocale = locale
        DECIMAL_1_FORMAT = NumberFormat.getInstance(locale) as DecimalFormat
        DECIMAL_1_FORMAT.applyPattern("0.0")
        DECIMAL_1_FORMAT.roundingMode = RoundingMode.DOWN
        DECIMAL_2_FORMAT = NumberFormat.getInstance(locale) as DecimalFormat
        DECIMAL_2_FORMAT.applyPattern("0.00")
        DECIMAL_2_FORMAT.roundingMode = RoundingMode.DOWN
    }

    /**
     * 格式化整数
     * 使用format，而不是[Int.toString]，是为了国际化
     */
    fun intStr(value: Int): String {
        return String.format(systemLocale, "%d", value)
    }

    /**
     * 格式化保留一位小数的数字
     * DecimalFormat只接受double类型，而float强转double会改变数值，如2.8f变成2.799999999..
     * 所以使用[Float.toString().toDouble()]的形式，保证数值不会改变
     */
    fun decimal1Str(value: Float): String {
        return DECIMAL_1_FORMAT.format(value.toString().toDouble())
    }

    /**
     * 格式化保留两位小数的数字
     */
    fun decimal2Str(value: Float): String {
        return DECIMAL_2_FORMAT.format(value.toString().toDouble())
    }

    ////////////// Time format////////////
    fun minute2Hmm(minute: Int): String {
        return hmm(minute / 60, minute % 60)
    }

    fun hmm(hour: Int, minute: Int): String {
        return String.format(systemLocale, "%d:%02d", hour, minute)
    }

}