package com.topstep.fitcloud.sample2.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.github.kilnn.tool.storage.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

object AppFiles {
    private const val TAG = "AppFiles"

    fun dirPicture(context: Context): File? {
        val dir = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_PICTURES).firstOrNull() ?: return null
        if (!dir.exists() && !dir.mkdirs()) {
            return null
        }
        return dir
    }

    fun dirDownload(context: Context): File? {
        val dir = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS).firstOrNull() ?: return null
        if (!dir.exists() && !dir.mkdirs()) {
            return null
        }
        return dir
    }

    fun generateJpegFile(context: Context): File? {
        val dir = dirPicture(context) ?: return null
        return File(dir, FileUtil.generateImageFileName())
    }

    private fun generateGifFile(context: Context): File? {
        val dir = dirPicture(context) ?: return null
        return File(dir, FileUtil.generateFileName() + ".gif")
    }

    fun dirCache(context: Context): File? {
        val dir = ContextCompat.getExternalCacheDirs(context).firstOrNull() ?: return null
        if (!dir.exists() && !dir.mkdirs()) {
            return null
        }
        return dir
    }

    /**
     * Because [Intent.ACTION_PICK] only has temporary permission to obtain images, so an additional copy image function is added.
     * Then use the copied Uri instead of [Intent.ACTION_PICK] Uri
     */
    suspend fun copyChoosePhotoUri(context: Context, src: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            val file = if (context.isGif(src)) {
                generateGifFile(context)
            } else {
                generateJpegFile(context)
            } ?: return@withContext null
            src.runCatching {
                context.contentResolver.openInputStream(src)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                        FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
                    }
                }
            }.onSuccess {
                Timber.tag(TAG).i("copyChoosePhotoUri success:%s", it)
            }.onFailure {
                Timber.tag(TAG).w(it, "copyChoosePhotoUri failed")
            }.getOrNull()
        }
    }

}