package com.topstep.fitcloud.sample2.utils

import android.content.Context
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.kilnn.tool.system.SystemUtil
import com.tencent.mars.xlog.Xlog
import com.topstep.fitcloud.sample2.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureTimeMillis

object AppLogger {

    private var xlog: Xlog? = null

    fun init(context: Context) {
        val time = System.currentTimeMillis()
        val logDir = dirLog(context)
        xlog = if (logDir != null) {
            initXlog(context, logDir)
        } else {
            null
        }

        val msg: String = if (xlog == null) {
            Timber.plant(Timber.DebugTree())
            "Timber.DebugTree"
        } else {
            if (BuildConfig.DEBUG) {
                Timber.plant(DebugTree())
                "DebugTree"
            } else {
                Timber.plant(ReleaseTree())
                "ReleaseTree"
            }
        }
        Timber.i("AppLogger init with %s time use %d , dir:${logDir?.absolutePath}", msg, System.currentTimeMillis() - time)
        Timber.i("App Version:%s", BuildConfig.VERSION_NAME)
    }

    fun dirLog(context: Context): File? {
        val externalRoot = ContextCompat.getExternalFilesDirs(context, null).firstOrNull() ?: return null
        val logDir = File(externalRoot, "log")
        if (!logDir.exists() && !logDir.mkdirs()) {
            return null
        }
        return logDir
    }

    private data class FileSorter(
        val time: Long,
        val index: Int,
        val file: File
    )

    suspend fun getLogFiles(context: Context): List<File>? {
        //打印下日志目录下的文件
        val dir = dirLog(context) ?: return null
        dir.listFiles()?.forEach {
            //打印所有文件的信息
            Timber.i("log file:%s", it.name)
        }

        //刷新日志
        flush()

        val allFiles = dir.listFiles()
        if (allFiles.isNullOrEmpty()) return null

        val calendar = Calendar.getInstance()
        val current = Date()
        //只取3天之内的日志文件
        val timeStart = DateTimeUtils.getDayStartTime(calendar, current, -2).time
        val timeEnd = DateTimeUtils.getDayStartTime(calendar, current).time
        val format = SimpleDateFormat("yyyyMMdd", Locale.US)

        val sorters = ArrayList<FileSorter>(allFiles.size)
        for (file in allFiles) {
            //ToNote:Xlog日志格式如下 main_20220721_1.xlog 或 main_20220721.xlog
            var filename = file.name
            if (!file.isFile || filename.isNullOrEmpty()) {
                continue
            }
            val suffixIndex = filename.lastIndexOf(".xlog")
            if (suffixIndex == -1) {
                runCatchingWithLog {
                    file.delete()
                }
                continue
            }
            filename = filename.substring(0, suffixIndex)
            val nameArray = filename.split("_")
            if (nameArray.size < 2) continue

            try {
                val fileTime = format.parse(nameArray[1])?.time ?: continue
                if (fileTime !in timeStart..timeEnd) {
                    continue
                }
                val fileIndex = if (nameArray.size < 3) {
                    0
                } else {
                    nameArray[2].toInt()
                }
                sorters.add(FileSorter(fileTime, fileIndex, file))
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
        sorters.sortWith { o1, o2 ->
            val timeDelta = o1.time - o2.time
            if (timeDelta < 0) {
                1
            } else if (timeDelta == 0L) {
                val indexDelta = o1.index - o2.index
                if (indexDelta < 0) {
                    1
                } else if (indexDelta == 0) {
                    0
                } else {
                    -1
                }
            } else {
                -1
            }
        }
        return sorters.map {
            Timber.i("sorted file:%s", it.file.name)
            it.file
        }
    }

    suspend fun flush() {
        val xlog = this.xlog ?: return
        withContext(Dispatchers.IO) {
            val usedTimes = measureTimeMillis {
                xlog.appenderFlush(0, true)
            }
            Timber.i("flush used:%d", usedTimes)
        }
    }

    private class DebugTree : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, tag, message, t)
            Xlog.logWrite2(0, tag, "", "", 0, Process.myPid(), Thread.currentThread().id, Looper.getMainLooper().thread.id, message)
        }
    }

    private class ReleaseTree : Timber.DebugTree() {
        override fun isLoggable(tag: String?, priority: Int): Boolean {
            return priority > Log.DEBUG
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            Xlog.logWrite2(0, tag, "", "", 0, Process.myPid(), Thread.currentThread().id, Looper.getMainLooper().thread.id, message)
        }
    }

    private fun initXlog(context: Context, logDir: File): Xlog {
        val processName = SystemUtil.getProcessAliasName(context)
        System.loadLibrary("c++_shared")
        System.loadLibrary("marsxlog")
        val xlog = Xlog()
        xlog.appenderOpen(
            Xlog.LEVEL_VERBOSE,
            Xlog.AppednerModeAsync,
            context.filesDir.absolutePath + "/xlog",
            logDir.absolutePath,
            processName,
            0
        )
        xlog.setConsoleLogOpen(0, false)
        xlog.setMaxFileSize(0, 10 * 1024 * 1024)//日志文件最大值，10M
        return xlog
    }

}