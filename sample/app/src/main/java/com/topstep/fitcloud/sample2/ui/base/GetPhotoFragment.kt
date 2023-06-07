package com.topstep.fitcloud.sample2.ui.base

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.github.kilnn.tool.storage.MediaStoreUtil
import com.github.kilnn.tool.storage.SAFUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.utils.*
import timber.log.Timber
import java.io.File

/**
 * Handle all Fragments that need to take/select photos
 */
abstract class GetPhotoFragment : BaseFragment, GetPhotoDialogFragment.Listener {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    /**
     * Temporarily store photos or select photo Uri
     * Because taking pictures does not return Uri, but uses the url passed in by itself, so this uri needs to be saved for use after the picture is taken successfully.
     */
    private var photoUri: Uri? = null

    /**
     * The Uri where the cropped photo is stored
     * Compatible processing: Some mobile phones do not return Uri for cropped photos, so save the Uri that is passed in and cropped for output in [tryCropPhoto]. Similar to [photoUri].
     */
    private var cropUri: Uri? = null

    /**
     * Whether to take pictures, true to take pictures, false to select
     */
    private var actionTake = true

    /**
     * Crop Mode
     * [CROP_NONE]
     * [CROP_TRY]
     * [CROP_MUST]
     * [CROP_TRY]
     */
    private var cropMode = CROP_TRY

    /**
     * If the choose picture is a gif format, whether to crop it
     */
    private var cropGif = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            photoUri = savedInstanceState.getParcelableCompat(EXTRA_PHOTO_URI)
            cropUri = savedInstanceState.getParcelableCompat(EXTRA_CROP_URI)
            actionTake = savedInstanceState.getBoolean(EXTRA_ACTION_TAKE, true)
            cropMode = savedInstanceState.getInt(EXTRA_CROP_MODE, CROP_TRY)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_PHOTO_URI, photoUri)
        outState.putParcelable(EXTRA_CROP_URI, cropUri)
        outState.putBoolean(EXTRA_ACTION_TAKE, actionTake)
        outState.putInt(EXTRA_CROP_MODE, cropMode)
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = photoUri
        Timber.tag(TAG).i("take photo result:%d , uri:%s", result.resultCode, uri)
        if (result.resultCode == Activity.RESULT_OK) {
            if (uri != null) {
                tryCropPhoto(uri)
            } else {
                promptToast.showFailed(getString(R.string.photo_take_failed))
            }
        }
    }

    private val choosePhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        Timber.tag(TAG).i("choose photo result:%d , uri:%s", result.resultCode, uri)
        if (result.resultCode == Activity.RESULT_OK) {
            if (uri != null) {
                copyChoosePhoto(uri)
            } else {
                promptToast.showFailed(getString(R.string.photo_select_failed))
            }
        }
    }

    private val cropPhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        Timber.tag(TAG).i("crop photo result:%d , uri:%s", result.resultCode, uri)
        if (result.resultCode == Activity.RESULT_OK) {
            handleCropResult(uri ?: cropUri)//部分手机裁剪不返回uri，使用cropUri
        }
    }

    /**
     * Compatible processing: [Intent.ACTION_PICK] only has temporary permissions, so an additional function of copying pictures is made to copy the selected pictures to the APP exclusive directory
     * refer：https://stackoverflow.com/questions/30572261/using-data-from-context-providers-or-requesting-google-photos-read-permission
     */
    private fun copyChoosePhoto(uri: Uri) {
        lifecycleScope.launchWhenStarted {
            promptProgress.showProgress(null, false)
            val resultUri = AppFiles.copyChoosePhotoUri(requireContext(), uri)
            if (resultUri != null) {
                promptProgress.dismiss()
                tryCropPhoto(resultUri)
            } else {
                promptToast.showFailed(getString(R.string.photo_select_failed))
            }
        }
    }

    private fun tryCropPhoto(uri: Uri) {
        if (cropMode == CROP_NONE) {
            onGetPhoto(uri)
        } else {
            if (!cropGif && requireContext().isGif(uri)) {
                onGetPhoto(uri)
                return
            }
            //Save the photo Uri before cropping
            photoUri = uri
            try {
                //call system crop
                val cropFile = getCropPhotoFile() ?: throw NullPointerException()
                val cropParam = getCropPhotoParam() ?: throw NullPointerException()
                val intent = MediaStoreUtil.appSpecialCropIntent(
                    requireContext(), FILE_PROVIDER_AUTHORITY, uri, cropFile,
                    cropParam.aspectX, cropParam.aspectY, cropParam.outputX, cropParam.outputY
                )
                cropUri = intent.getParcelableExtraCompat(MediaStore.EXTRA_OUTPUT)
                Timber.tag(TAG).i("create crop uri:%s", cropUri)
                cropPhoto.launch(intent)
            } catch (e: Exception) {
                Timber.tag(TAG).w(e)
                handleCropResult(null)
            }
        }
    }

    private fun handleCropResult(resultUri: Uri?) {
        if (resultUri != null) {
            //crop success
            onGetPhoto(resultUri)
        } else {
            //If it is not force to crop, then use photoUri. else if it is nul, judgment is failure
            val rawUri = if (cropMode == CROP_TRY) photoUri else null
            if (rawUri != null) {
                Timber.tag(TAG).w("crop failed and use rawUri instead")
                onGetPhoto(rawUri)
            } else {
                Timber.tag(TAG).w("crop failed")
                if (actionTake) {
                    //Prompt to take pictures failed
                    promptToast.showFailed(getString(R.string.photo_take_failed))
                } else {
                    //Prompt selection failed
                    promptToast.showFailed(getString(R.string.photo_select_failed))
                }
            }
        }
    }

    /**
     * Show dialog for taking pictures/selecting.
     */
    fun getPhoto(cropMode: Int = CROP_TRY, cropGif: Boolean = true) {
        GetPhotoDialogFragment.newInstance(cropMode, cropGif).show(childFragmentManager, null)
    }

    /**
     * To take pictures
     */
    final override fun takePhoto(cropMode: Int) {
        actionTake = true
        this.cropMode = cropMode
        PermissionHelper.requestSystemCamera(this) {
            if (it) {
                // Call system camera
                val file = getTakePhotoFile()
                if (file == null) {//Generate file error, prompting failed to take pictures
                    Timber.tag(TAG).w("getTakePhotoFile null")
                    promptToast.showFailed(R.string.photo_take_failed)
                } else {
                    val intent = MediaStoreUtil.appSpecialCaptureIntent(requireContext(), FILE_PROVIDER_AUTHORITY, file)
                    photoUri = intent.getParcelableExtraCompat(MediaStore.EXTRA_OUTPUT)
                    takePhoto.launch(intent)
                }
            }
        }
    }

    /**
     * To choose Photo
     */
    override fun choosePhoto(cropMode: Int, cropGif: Boolean) {
        actionTake = false
        this.cropMode = cropMode
        this.cropGif = cropGif
        //Call the system to select a photo
        try {
            choosePhoto.launch(SAFUtil.openDocumentImageIntent())
        } catch (e: ActivityNotFoundException) {
            Timber.tag(TAG).w(e)
            try {
                choosePhoto.launch(SAFUtil.getContentImageIntent())
            } catch (e: ActivityNotFoundException) {
                Timber.tag(TAG).w(e)
                promptToast.showFailed(getString(R.string.photo_select_failed))
            }
        }
    }

    abstract fun getTakePhotoFile(): File?
    abstract fun getCropPhotoFile(): File?
    abstract fun getCropPhotoParam(): CropParam?
    abstract fun onGetPhoto(uri: Uri)

    companion object {
        private const val TAG = "GetPhotoFragment"

        const val CROP_NONE = 0//no crop required
        const val CROP_TRY = 1//Try to crop, if not successful, return to the original uri
        const val CROP_MUST = 2//The crop must be successful

        internal const val EXTRA_PHOTO_URI = "photo_uri"
        internal const val EXTRA_CROP_URI = "crop_uri"
        internal const val EXTRA_ACTION_TAKE = "action_take"
        internal const val EXTRA_CROP_MODE = "crop_mode"
        internal const val EXTRA_CROP_GIF = "crop_gif"
    }

}

/**
 * Crop params
 */
data class CropParam(
    /**
     * aspectX
     */
    val aspectX: Int,

    /**
     * aspectY
     */
    val aspectY: Int,

    /**
     * outputX
     */
    val outputX: Int,

    /**
     * outputY
     */
    val outputY: Int,
)

/**
 * The dialog box for selecting photos provides two options: "Take photo" and "Select".
 */
class GetPhotoDialogFragment : AppCompatDialogFragment() {
    interface Listener {
        fun takePhoto(cropMode: Int)
        fun choosePhoto(cropMode: Int, cropGif: Boolean)
    }

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? Listener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = arrayOf<CharSequence>(
            getString(R.string.action_take_photo),
            getString(R.string.action_choose_photo),
        )
        val cropMode = requireArguments().getInt(GetPhotoFragment.EXTRA_CROP_MODE, GetPhotoFragment.CROP_TRY)
        val cropGif = requireArguments().getBoolean(GetPhotoFragment.EXTRA_CROP_GIF, true)
        return MaterialAlertDialogBuilder(requireContext())
            .setItems(items) { _, which ->
                if (which == 0) {
                    listener?.takePhoto(cropMode)
                } else {
                    listener?.choosePhoto(cropMode, cropGif)
                }
            }
            .create()
    }

    companion object {
        fun newInstance(cropMode: Int, cropGif: Boolean): GetPhotoDialogFragment {
            val fragment = GetPhotoDialogFragment()
            fragment.arguments = Bundle().apply {
                putInt(GetPhotoFragment.EXTRA_CROP_MODE, cropMode)
                putBoolean(GetPhotoFragment.EXTRA_CROP_GIF, cropGif)
            }
            return fragment
        }
    }
}