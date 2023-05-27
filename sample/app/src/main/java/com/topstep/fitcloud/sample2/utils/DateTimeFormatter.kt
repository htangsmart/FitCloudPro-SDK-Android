package com.topstep.fitcloud.sample2.utils

import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateTimeFormatter {

    private val DATE_HOLDER = ThreadLocal<SimpleDateFormat>()
    private val TIME_HOLDER = ThreadLocal<SimpleDateFormat>()

    private val DEFAULT_DATE = Calendar.getInstance().apply {
        set(1900, 0, 1)
    }.time

    private fun dateFormatter(): SimpleDateFormat {
        var format = DATE_HOLDER.get()
        if (format == null) {
            format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            DATE_HOLDER.set(format)
        }
        return format
    }

    private fun timeFormatter(): SimpleDateFormat {
        var format = TIME_HOLDER.get()
        if (format == null) {
            format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            TIME_HOLDER.set(format)
        }
        return format
    }

    fun formatDate(date: Date): String {
        return dateFormatter().format(date)
    }

    fun parseDate(str: String): Date {
        var result: Date? = null
        try {
            result = dateFormatter().parse(str)
        } catch (e: ParseException) {
            Timber.w(e)
        }
        return result ?: DEFAULT_DATE
    }

    fun formatTime(time: Date): String {
        return timeFormatter().format(time)
    }

    fun parseTime(str: String): Date {
        var result: Date? = null
        try {
            result = timeFormatter().parse(str)
        } catch (e: ParseException) {
            Timber.w(e)
        }
        return result ?: DEFAULT_DATE
    }
}