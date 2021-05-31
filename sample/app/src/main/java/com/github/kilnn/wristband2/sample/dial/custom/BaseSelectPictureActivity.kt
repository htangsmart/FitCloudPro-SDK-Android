package com.github.kilnn.wristband2.sample.dial.custom

import android.content.ClipData
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.core.content.FileProvider
import com.github.kilnn.wristband2.sample.BaseActivity
import com.github.kilnn.wristband2.sample.BuildConfig
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.glide.MatisseGlideEngine
import com.github.kilnn.wristband2.sample.utils.AndPermissionHelper
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.internal.entity.CaptureStrategy
import java.io.File
import java.util.*

abstract class BaseSelectPictureActivity : BaseActivity() {

    companion object {
        private const val REQUEST_CODE_CHOOSE = 1
        private const val REQUEST_CODE_CROP = 2
    }

    private var pictureUri: Uri? = null

    fun selectPicture() {
        pictureUri = null //clear
        AndPermissionHelper.cameraRequest(this) {
            Matisse.from(this)
                .choose(MimeType.ofImage())
                .capture(true)
                .captureStrategy(CaptureStrategy(false, BuildConfig.FileAuthorities))
                .countable(false)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(MatisseGlideEngine())
                .forResult(REQUEST_CODE_CHOOSE)
        }
    }

    abstract fun getPictureCropFile(): File?
    abstract fun getPictureCropIntent(): Intent?
    abstract fun onPictureSelect(uri: Uri)

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            val success: Boolean = data?.let { Matisse.obtainResult(it) }?.firstOrNull()?.runCatching {
                val cropFile = getPictureCropFile() ?: throw NullPointerException()
                val intent = getPictureCropIntent() ?: throw NullPointerException()
                intent.setDataAndType(this, "image/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val cropUri = FileProvider.getUriForFile(this@BaseSelectPictureActivity, BuildConfig.FileAuthorities, cropFile)
                intent.clipData = ClipData.newRawUri("", cropUri)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri)
                pictureUri = cropUri
                startActivityForResult(intent, REQUEST_CODE_CROP)
            }?.isSuccess ?: false
            if (!success) {
                Toast.makeText(this, R.string.photo_select_failed, Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_CODE_CROP && resultCode == RESULT_OK) {
            val uri = pictureUri
            if (uri == null) {
                Toast.makeText(this, R.string.photo_select_failed, Toast.LENGTH_SHORT).show()
            } else {
                onPictureSelect(uri)
                pictureUri = null
            }
        }
    }
}