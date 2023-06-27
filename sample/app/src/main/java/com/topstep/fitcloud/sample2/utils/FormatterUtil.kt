package com.topstep.fitcloud.sample2.utils

import android.text.format.DateFormat
import com.github.kilnn.wheellayout.WheelIntFormatter
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
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

    fun intStr(value: Int): String {
        return String.format(systemLocale, "%d", value)
    }

    /**
     * Keep one decimal place
     */
    fun decimal1Str(value: Float): String {
        return DECIMAL_1_FORMAT.format(value.toString().toDouble())
    }

    /**
     * Keep two decimal places
     */
    fun decimal2Str(value: Float): String {
        return DECIMAL_2_FORMAT.format(value.toString().toDouble())
    }

    ///////////////// SimpleDateFormat /////////////////
    private fun getDateFormat(skeleton: String): SimpleDateFormat {
        var pattern = DateFormat.getBestDateTimePattern(systemLocale, skeleton)
        pattern = pattern
            .replace(",".toRegex(), "")
            .replace("'à'".toRegex(), "")
            .replace(" {2,}".toRegex(), " ")
        return SimpleDateFormat(pattern, systemLocale)
    }

    fun getFormatterYYYYMMM(): SimpleDateFormat {
        return getDateFormat("yyyy-MMM")
    }

    fun getFormatterYYYYMMMdd(): SimpleDateFormat {
        return getDateFormat("yyyy-MMM-dd")
    }

    //////////////// WheelIntFormatter //////////////////
    fun getGenericWheelIntFormatter(): WheelIntFormatter {
        return object : WheelIntFormatter {
            override fun format(index: Int, value: Int): String {
                return intStr(value)
            }
        }
    }

    fun get02dWheelIntFormatter(): WheelIntFormatter {
        return object : WheelIntFormatter {
            override fun format(index: Int, value: Int): String {
                return String.format(systemLocale, "%02d", value)
            }
        }
    }

    ////////////// Time format////////////
    fun minute2Hmm(minute: Int): String {
        return hmm(minute / 60, minute % 60)
    }

    fun second2Hmm(second: Int): String {
        return minute2Hmm(second / 60)
    }

    fun hmm(hour: Int, minute: Int): String {
        return String.format(systemLocale, "%d:%02d", hour, minute)
    }

    fun minute2Duration(minute: Int): String {
        return String.format(systemLocale, "%02d:%02d", minute / 60, minute % 60)
    }

}