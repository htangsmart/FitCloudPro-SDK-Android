package com.topstep.fitcloud.sample2.ui.device.cricket

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.squareup.moshi.JsonAdapter
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.nio.channels.FileLock

/**
 * FileLock控制多进程访问。但对于同一个JVM，只保持一个FileLock。所以需要使用synchronized控制多线程的访问。
 */

/**
 *
 *
 * @param context Context
 * @param filename 文件名，必须为字符串字面量，否则多线程访问可能出错
 * @return 读出来的数据，如果为null，说明读取异常，或者文件为空
 */
fun <T> JsonAdapter<T>.readFile(context: Context, filename: String): T? {
    synchronized(filename) {
        val file = getJsonFile(context, filename)
        var input: FileInputStream? = null
        var channel: FileChannel? = null
        var lock: FileLock? = null
        try {
            input = FileInputStream(file)
            channel = input.channel
            lock = channel.lock(0L, Long.MAX_VALUE, true)
            val buf = ByteArray(512)
            var len: Int
            var str = ""
            while ((input.read(buf).also { len = it }) != -1) {
                str += String(buf, 0, len)
            }
            if (str.isEmpty()) {//写入Null数据的时候，写入的是""空字符，所以判断下
                return null
            }
            return this.fromJson(str)
        } catch (e: Exception) {
            Timber.tag("FileJsonExt").w(e, "read from file %s failed", filename)
            throw e
        } finally {
            try {
                lock?.close()
                channel?.close()
                input?.close()
            } catch (e: Exception) {
                //do nothing
            }
        }
    }
}

/**
 * @param context Context
 * @param filename 文件名，必须为字符串字面量，否则多线程访问可能出错
 * @param value 写入的数据。如果为null，则清空数据。
 */
fun <T> JsonAdapter<T>.writeFile(context: Context, filename: String, value: T?) {
    synchronized(filename) {
        val file: File = getJsonFile(context, filename)
        var output: FileOutputStream? = null
        var channel: FileChannel? = null
        var lock: FileLock? = null
        try {
            output = FileOutputStream(file)
            channel = output.channel
            lock = channel.lock()
            if (value == null) {
                output.write("".toByteArray())
            } else {
                output.write(this.toJson(value).toByteArray())
            }
            output.flush()
        } catch (e: Exception) {
            Timber.tag("FileJsonExt").w(e, "write %s to file %s failed", value, filename)
            throw e
        } finally {
            try {
                lock?.close()
                channel?.close()
                output?.close()
            } catch (e: Exception) {
                //do nothing
            }
        }
    }
}

@VisibleForTesting
@Throws(FileNotFoundException::class)
fun getJsonFile(context: Context, filename: String): File {
    val file = File(context.filesDir, "json/$filename.json")
    val parent = file.parentFile
    if (parent == null || (!parent.exists() && !parent.mkdirs())) {
        throw FileNotFoundException("file ${file.absolutePath} not exist")
    }
    return file
}

fun isJsonFileExist(context: Context, filename: String): Boolean {
    val file = File(context.filesDir, "json/$filename.json")
    val parent = file.parentFile
    if (parent == null || (!parent.exists() && !parent.mkdirs())) {
        return false
    }
    return file.exists()
}