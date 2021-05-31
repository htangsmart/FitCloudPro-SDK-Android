package com.github.kilnn.wristband2.sample.dial

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.kilnn.wristband2.sample.file.DefaultFilePathGenerator
import com.github.kilnn.wristband2.sample.file.SimpleFileDownloader
import com.htsmart.wristband2.dial.DialDrawer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class DialFileHelper private constructor() {
    companion object {
        private const val TAG = "DialFileHelper"

        internal val PATH_GENERATOR = DefaultFilePathGenerator()

        /**
         * 获取表盘bin缓存文件，图片缓存文件，背景图缓存文件 等的根目录
         */
        private fun getRootDir(context: Context): File? {
            return PATH_GENERATOR.getFileDir(context)
        }

        /**
         * 根据Uri生成文件名称
         */
        private fun generatorFileNameByUrl(context: Context, url: String?): String? {
            if (url.isNullOrEmpty()) return null
            return PATH_GENERATOR.getFileName(context, url)
        }

        /**
         * 获取普通文件的缓存地址。
         * 普通文件指不包含额外文件夹路径的文件
         */
        fun getNormalFileByUrl(context: Context, url: String?): File? {
            val dir = getRootDir(context)
            if (dir != null) {
                val fileName = generatorFileNameByUrl(context, url)
                if (!fileName.isNullOrEmpty()) {
                    return File(dir, fileName)
                }
            }
            return null
        }

        /**
         * 获取自定义表盘的缓存文件地址
         */
        private fun getDialCustomCacheDir(context: Context, shape: DialDrawer.Shape): File? {
            val dir = getRootDir(context) ?: return null
            val name = if (shape.isShapeCircle) {
                "circle_" + shape.width() + "_" + shape.height()
            } else {
                "rect_" + shape.width() + "_" + shape.height()
            }
            return File(dir, name)
        }

        /**
         * 获取自定义表盘的文件路径
         */
        fun newDialCustomBgFile(context: Context, shape: DialDrawer.Shape): File? {
            val dir = getDialCustomCacheDir(context, shape) ?: return null
            val fileName: String = UUID.randomUUID().toString().replace("-", "").toString() + ".jpg"
            val file = File(dir, fileName)
            if (!file.parentFile.exists()) { //父目录不存在
                if (!file.parentFile.mkdirs()) {
                    return null
                }
            }
            return file
        }

        fun loadDialCustomBgFiles(context: Context, shape: DialDrawer.Shape): MutableList<Uri>? {
            val dir = getDialCustomCacheDir(context, shape) ?: return null
            if (!dir.exists()) return null
            return dir.listFiles().filter {
                val fileName = it.name.toLowerCase(Locale.US)
                fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
            }.map {
                Uri.fromFile(it)
            } as MutableList<Uri>
        }

        fun newDialCustomBgCropIntent(shape: DialDrawer.Shape): Intent {
            val intent = Intent("com.android.camera.action.CROP")
            intent.putExtra("crop", "true")
            intent.putExtra("aspectX", shape.width())
            intent.putExtra("aspectY", shape.height())
            intent.putExtra("outputX", shape.width())
            intent.putExtra("outputY", shape.height())
            intent.putExtra("scale", true)
            intent.putExtra("scaleUpIfNeeded", true) // 部分机型没有设置该参数截图会有黑边
            intent.putExtra("return-data", false)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            // 不启用人脸识别
            intent.putExtra("noFaceDetection", false)
            return intent
        }

        @ExperimentalCoroutinesApi
        fun downloadWithProgress(context: Context, url: String) = callbackFlow<ProgressResult<String>> {
            val dialDownloader = DialDownloader(context)
            dialDownloader.setListener(object : SimpleFileDownloader.SimpleListener() {
                override fun onCompleted(filePath: String) {
                    Timber.tag(TAG).d("download completed url:$url path:$filePath")
                    try {
                        sendBlocking(ProgressResult(100, filePath))
                        close()
                    } catch (e: Exception) {
                    }
                }

                override fun onError(errorCode: Int) {
                    Timber.tag(TAG).w("download error url:$url")
                    close(DialDownloadException(errorCode))
                }

                override fun onProgress(progress: Int) {
                    try {
                        sendBlocking(ProgressResult(progress))
                    } catch (e: Exception) {
                    }
                }
            })
            Timber.tag(TAG).w("download start url:$url")
            dialDownloader.start(url)
            awaitClose {
                Timber.tag(TAG).w("download canceled url:$url")
                dialDownloader.cancel()
            }
        }

        suspend fun download(context: Context, url: String) = suspendCancellableCoroutine<String> {
            val dialDownloader = DialDownloader(context)
            it.invokeOnCancellation {
                Timber.tag(TAG).d("download canceled : $url")
                dialDownloader.cancel()
            }
            dialDownloader.setListener(object : SimpleFileDownloader.SimpleListener() {
                override fun onCompleted(filePath: String) {
                    Timber.tag(TAG).d("download completed url:$url path:$filePath")
                    it.resume(filePath)
                }

                override fun onError(errorCode: Int) {
                    Timber.tag(TAG).w("download error url:$url")
                    it.resumeWithException(DialDownloadException(errorCode))
                }
            })
            Timber.tag(TAG).w("download start url:$url")
            dialDownloader.start(url)
        }

        suspend fun glideGetBitmap(context: Context, uri: Uri) = suspendCancellableCoroutine<Bitmap> {
            val target = Glide.with(context)
                .asBitmap()
                .load(uri)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        it.resume(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        it.resumeWithException(DialBitmapException())
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })
            it.invokeOnCancellation {
                Glide.with(context).clear(target)
            }
        }

    }
}

class ProgressResult<T>(val progress: Int, val result: T? = null)

class DialDownloadException(val errorCode: Int) : Exception()

class DialBitmapException : Exception()

/**
 * 其实和SimpleFileDownloader实现一模一样，只是为了逻辑更清楚，专门建一个类用于表单下载
 */
class DialDownloader : SimpleFileDownloader {
    constructor(context: Context) : super(context) {
        setFilePathGenerator(DialFileHelper.PATH_GENERATOR)
        setNoGzip()
    }

    constructor(context: Context, downloadPost: Boolean) : super(context, downloadPost) {
        setFilePathGenerator(DialFileHelper.PATH_GENERATOR)
        setNoGzip()
    }
}