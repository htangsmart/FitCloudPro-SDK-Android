package com.topstep.fitcloud.sample2

import timber.log.Timber

class MyCrashHandler : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Timber.tag("Crash").i("\n")
        Timber.tag("Crash").i("--------------------------------------")
        Timber.tag("Crash").i("\n")
        Timber.tag("Crash").e(e, "thread:%s", t.toString())
        Timber.tag("Crash").i("\n")
        Timber.tag("Crash").i("--------------------------------------")
        Timber.tag("Crash").i("\n")
        defaultHandler?.uncaughtException(t, e)
    }

}