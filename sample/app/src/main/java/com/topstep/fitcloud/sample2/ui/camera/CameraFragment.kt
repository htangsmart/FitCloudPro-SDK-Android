package com.topstep.fitcloud.sample2.ui.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.window.layout.WindowMetricsCalculator
import com.github.kilnn.tool.storage.FileUtil
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentCameraBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.PermissionHelper
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.message.FcMessageType
import kotlinx.coroutines.rx3.asFlow
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Helper type alias used for analysis use case callbacks */
typealias LumaListener = (luma: Double) -> Unit

class CameraFragment : BaseFragment(R.layout.fragment_camera) {

    private val viewBind: FragmentCameraBinding by viewBinding()

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.displayId) {
                Timber.tag(TAG).d("Rotation changed: ${view.display.rotation}")
                imageCapture?.targetRotation = view.display.rotation
                imageAnalyzer?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }

    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val deviceManager = Injector.getDeviceManager()

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Every time the orientation of device changes, update rotation for use cases
        displayManager.registerDisplayListener(displayListener, null)

        // Wait for the views to be properly laid out
        viewBind.viewFinder.post {
            // Keep track of the display in which this view is attached
            displayId = viewBind.viewFinder.display.displayId

            // Build UI controls
            updateCameraUi()

            // Set up the camera and its use cases
            setUpCamera()
        }
        viewLifecycle.launchRepeatOnStarted {
            deviceManager.messageFeature.observerMessage().asFlow().collect {
                if (it.type == FcMessageType.CAMERA_TAKE_PHOTO) {
                    viewBind.btnShutter.simulateClick()
                } else if (it.type == FcMessageType.CAMERA_EXIT) {
                    requireActivity().finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionHelper.hasCamera(requireContext())) {
            findNavController().navigate(CameraFragmentDirections.toPermission())
        }
        deviceManager.messageFeature.setCameraStatus(true).onErrorComplete().subscribe()
    }

    override fun onPause() {
        super.onPause()
        viewBind.countDownView.cancelCountDown()
        deviceManager.messageFeature.setCameraStatus(false).onErrorComplete().subscribe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }

    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     *
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Rebind the camera with the updated display metrics
        bindCameraUseCases()

        // Enable or disable switching between cameras
        updateCameraSwitchButton()
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            if (view == null) return@addListener
            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Select lensFacing depending on the available cameras
            lensFacing = when {
                hasBackCamera() -> {
                    viewBind.imgFacing.setImageResource(R.drawable.ic_baseline_camera_rear_24)
                    CameraSelector.LENS_FACING_BACK
                }
                hasFrontCamera() -> {
                    viewBind.imgFacing.setImageResource(R.drawable.ic_baseline_camera_front_24)
                    CameraSelector.LENS_FACING_FRONT
                }
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            // Enable or disable switching between cameras
            updateCameraSwitchButton()

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(requireActivity()).bounds
        Timber.tag(TAG).d("Screen metrics: ${metrics.width()} x ${metrics.height()}")

        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
        Timber.tag(TAG).d("Preview aspect ratio: $screenAspectRatio")

        val rotation = viewBind.viewFinder.display.rotation

        // CameraProvider
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build().also { this.imageCapture = it }

        when (imageCapture.flashMode) {
            ImageCapture.FLASH_MODE_AUTO -> viewBind.imgFlash.setImageResource(R.drawable.ic_baseline_flash_auto_24)
            ImageCapture.FLASH_MODE_ON -> viewBind.imgFlash.setImageResource(R.drawable.ic_baseline_flash_on_24)
            ImageCapture.FLASH_MODE_OFF -> viewBind.imgFlash.setImageResource(R.drawable.ic_baseline_flash_off_24)
            else -> viewBind.imgFlash.isEnabled = false
        }

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    // Values returned from our analyzer are passed to the attached listener
                    // We log image analysis results here - you should do something useful
                    // instead!
//                    Timber.tag(TAG).d("Average luminosity: $luma")
                })
            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        camera?.let {
            // Must remove observers from the previous camera instance
            removeCameraStateObservers(it.cameraInfo)
        }

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(viewBind.viewFinder.surfaceProvider)
            camera?.let {
                observeCameraState(it.cameraInfo)
            }
        } catch (exc: Exception) {
            Timber.tag(TAG).w(exc, "Use case binding failed")
        }
    }

    private fun removeCameraStateObservers(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.removeObservers(viewLifecycleOwner)
    }

    private fun observeCameraState(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.observe(viewLifecycleOwner) { cameraState ->
            Timber.tag(TAG).i("cameraState:%s", cameraState.type)
            cameraState.error?.let { error ->
                promptToast.showFailed("Camera error:${error.code}")
            }
        }
    }

    /**
     *  [androidx.camera.core.ImageAnalysis.Builder] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /** Method used to re-draw the camera UI controls, called every time configuration changes. */
    private fun updateCameraUi() {
        // Listener for button used to capture photo
        viewBind.btnShutter.clickTrigger {
            prepareShutter()
        }

        // Setup for button used to switch cameras

        viewBind.imgFacing.isEnabled = false// Disable the button until the camera is set up
        viewBind.imgFacing.clickTrigger {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                viewBind.imgFacing.setImageResource(R.drawable.ic_baseline_camera_rear_24)
                CameraSelector.LENS_FACING_BACK
            } else {
                viewBind.imgFacing.setImageResource(R.drawable.ic_baseline_camera_front_24)
                CameraSelector.LENS_FACING_FRONT
            }

            // Re-bind use cases to update selected camera
            bindCameraUseCases()
        }

        viewBind.imgFlash.clickTrigger {
            imageCapture?.let {
                when (it.flashMode) {
                    ImageCapture.FLASH_MODE_AUTO -> {
                        it.flashMode = ImageCapture.FLASH_MODE_ON
                        viewBind.imgFlash.setImageResource(R.drawable.ic_baseline_flash_on_24)
                    }
                    ImageCapture.FLASH_MODE_ON -> {
                        it.flashMode = ImageCapture.FLASH_MODE_OFF
                        viewBind.imgFlash.setImageResource(R.drawable.ic_baseline_flash_off_24)
                    }
                    ImageCapture.FLASH_MODE_OFF -> {
                        it.flashMode = ImageCapture.FLASH_MODE_AUTO
                        viewBind.imgFlash.setImageResource(R.drawable.ic_baseline_flash_auto_24)
                    }
                    else -> {}
                }
            }
        }

        viewBind.imgFile.clickTrigger {
            try {
                val intent = IntentCompat.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(intent)
            } catch (e: Exception) {
                Timber.tag(TAG).w(e)
                try {
                    //打开系统gallery
                    //https://stackoverflow.com/questions/19436366/android-open-gallery-app
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("content://media/internal/images/media"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(intent)
                } catch (e2: Exception) {
                    Timber.tag(TAG).w(e2)
                }
            }
        }

        viewBind.countDownView.setCountDownFinishedListener { shutter() }
    }

    private fun prepareShutter() {
        if (imageCapture == null) return
        if (viewBind.countDownView.isCountingDown) {
            viewBind.countDownView.cancelCountDown()
        }
        viewBind.countDownView.startCountDown(3, true)
    }

    private fun shutter() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            return
        }
        val imageCapture = this.imageCapture ?: return

        // Setup image capture metadata
        val metadata = ImageCapture.Metadata().apply {
            // Mirror image when using the front camera
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }

        // Create output options object which contains file + metadata
        val contentValues = makePublicContentValues(requireContext())
        if (contentValues == null) {
            promptToast.showFailed(R.string.photo_take_failed)
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).setMetadata(metadata).build()

        // Setup image capture listener which is triggered after photo has been taken
        imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Timber.tag(TAG).e(exc, "Photo capture failed: ${exc.message}")
                lifecycleScope.launchWhenStarted {
                    promptToast.showFailed(R.string.photo_take_failed)
                }
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                if (view != null) {
                    viewBind.countDownView.playBeepShutter()
//                 We can only change the foreground Drawable using API level 23+ API
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // Display flash animation to indicate that photo was captured
                        viewBind.root.displayFlashAnim()
                    }
                }

                val savedUri = output.savedUri
                Timber.tag(TAG).i("Photo capture succeeded: $savedUri")
                lifecycleScope.launchWhenStarted {
                    promptToast.showSuccess(R.string.photo_take_success)
                }
            }
        })
    }

    /** Enabled or disabled a button to switch cameras depending on the available cameras */
    private fun updateCameraSwitchButton() {
        try {
            viewBind.imgFacing.isEnabled = hasBackCamera() && hasFrontCamera()
        } catch (exception: CameraInfoUnavailableException) {
            viewBind.imgFacing.isEnabled = false
        }
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    /**
     * Our custom image analysis class.
     *
     * <p>All we need to do is override the function `analyze` with our desired operations. Here,
     * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
     */
    private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        override fun analyze(image: ImageProxy) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // Keep track of frames analyzed
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                    frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

            // Analysis could take an arbitrarily long amount of time
            // Since we are running in a different thread, it won't stall other use cases

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values ranging 0-255
            val pixels = data.map { it.toInt() and 0xFF }

            // Compute average luminance for the image
            val luma = pixels.average()

            // Call all listeners with new value
            listeners.forEach { it(luma) }

            image.close()
        }
    }

    companion object {
        private const val TAG = "Camera"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        fun makePublicContentValues(context: Context): ContentValues? {
            val contentValues = ContentValues()
            val appName = context.getString(R.string.app_name).replace(" ", "")
            var dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), appName)
            if (!dir.exists() && !dir.mkdirs()) {
                Timber.tag(TAG).w("dir create fail 1:%s", dir.absolutePath)
                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                if (!dir.exists() && !dir.mkdirs()) {
                    Timber.tag(TAG).w("dir create fail 2:%s", dir.absolutePath)
                    return null
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, FileUtil.generateFileName())
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/$appName")
            } else {
                val filename = FileUtil.generateImageFileName()
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                val file = File(dir, filename)
                contentValues.put(MediaStore.Images.Media.DATA, file.absolutePath)
            }
            return contentValues
        }
    }
}
