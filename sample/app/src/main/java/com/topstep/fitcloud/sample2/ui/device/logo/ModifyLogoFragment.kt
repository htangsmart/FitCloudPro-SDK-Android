package com.topstep.fitcloud.sample2.ui.device.logo

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.MyApplication
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentModifyLogoBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.utils.AppFiles
import com.topstep.fitcloud.sample2.utils.isGif
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.settings.dial.FcShape
import com.topstep.fitcloud.sdk.v2.utils.logo.LogoWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class ModifyLogoFragment : GetPhotoFragment(R.layout.fragment_modify_logo) {

    private val viewBind: FragmentModifyLogoBinding by viewBinding()
    private val viewModel: ModifyLogoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.requestShape()
        }
        viewBind.layoutBoot.clickTrigger(block = blockClick)
        viewBind.layoutShutdown.clickTrigger(block = blockClick)
        viewBind.btnSave.clickTrigger(block = blockClick)
        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    when (val async = it.asyncShape) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }
                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }
                        is Success -> {
                            val shape = async()
                            if (shape == null) {
                                viewBind.loadingView.showInfo(R.string.ds_dial_error_shape)
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                                if (it.bootUri == null) {
                                    viewBind.imgBoot.visibility = View.GONE
                                    viewBind.tvBootGif.visibility = View.GONE
                                } else {
                                    viewBind.imgBoot.visibility = View.VISIBLE
                                    Glide.with(requireContext())
                                        .load(it.bootUri)
                                        .into(viewBind.imgBoot)

                                    val isGif = requireContext().isGif(it.bootUri)
                                    viewBind.tvBootGif.isVisible = isGif
                                }
                                if (it.shutdownUri == null) {
                                    viewBind.imgShutdown.visibility = View.GONE
                                    viewBind.tvShutdownGif.visibility = View.GONE
                                } else {
                                    viewBind.imgShutdown.visibility = View.VISIBLE
                                    Glide.with(requireContext())
                                        .load(it.shutdownUri)
                                        .into(viewBind.imgShutdown)

                                    val isGif = requireContext().isGif(it.shutdownUri)
                                    viewBind.tvShutdownGif.isVisible = isGif
                                }

                                viewBind.tvTips3.text = getString(R.string.ds_modify_logo_gif_tips3, "${shape.width}*${shape.height}")

                                viewBind.btnSave.isEnabled = it.bootUri != null || it.shutdownUri != null
                            }
                        }
                        else -> {}
                    }

                    if (it.asyncCreateLogoFile is Loading) {
                        promptProgress.showProgress(R.string.tip_please_wait)
                    } else {
                        promptProgress.dismiss()
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect {
                    val isPropertyCreateLogoFile = State::asyncCreateLogoFile
                    when (it) {
                        is AsyncEvent.OnFail -> {
                            if (it.property == isPropertyCreateLogoFile) {
                                promptToast.showFailed("创建Logo文件失败")
                            }
                        }
                        is AsyncEvent.OnSuccess<*> -> {
                            if (it.property == isPropertyCreateLogoFile) {
                                val file = it.value as File
                                LogoDfuDialogFragment.newInstance(file).show(childFragmentManager, null)
                            }
                        }
                    }
                }
            }
        }

    }

    private var selectBoot = true

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.layoutBoot -> {
                selectBoot = true
                getPhoto(cropMode = CROP_NONE, cropGif = false)
            }
            viewBind.layoutShutdown -> {
                selectBoot = false
                getPhoto(cropMode = CROP_NONE, cropGif = false)
            }
            viewBind.btnSave -> {
                viewModel.createLogoFile()
            }
        }
    }

    override fun getTakePhotoFile(): File? {
        return AppFiles.generateJpegFile(requireContext())
    }

    override fun getCropPhotoFile(): File? {
        return AppFiles.generateJpegFile(requireContext())
    }

    override fun getCropPhotoParam(): CropParam {
        val shape = viewModel.state.asyncShape()!!
        return CropParam(1, 1, shape.width, shape.height)
    }

    override fun onGetPhoto(uri: Uri) {
        if (selectBoot) {
            viewModel.setBootUri(uri)
        } else {
            viewModel.setShutdownUri(uri)
        }
    }

    data class State(
        val asyncShape: Async<FcShape?> = Uninitialized,
        val asyncCreateLogoFile: Async<File> = Uninitialized,
        val bootUri: Uri? = null,
        val shutdownUri: Uri? = null,
    )

}

class ModifyLogoViewModel : AsyncViewModel<ModifyLogoFragment.State>(ModifyLogoFragment.State()) {

    private val deviceManager = Injector.getDeviceManager()
    private val context = MyApplication.instance

    init {
        requestShape()
    }

    fun requestShape() {
        suspend {
            val shape = deviceManager.settingsFeature.requestDialPushInfo().await().shape
            Timber.i("shape:%s", shape)
            shape
        }.execute(ModifyLogoFragment.State::asyncShape) {
            copy(asyncShape = it)
        }
    }

    fun setBootUri(uri: Uri) {
        viewModelScope.launch {
            state.copy(bootUri = uri).newState()
        }
    }

    fun setShutdownUri(uri: Uri) {
        viewModelScope.launch {
            state.copy(shutdownUri = uri).newState()
        }
    }

    fun createLogoFile() {
        suspend {
            withContext(Dispatchers.IO) {
                LogoWriter(
                    context,
                    state.asyncShape()!!,
                    state.bootUri,
                    state.shutdownUri,
                    null
                ).execute(null)
            }
        }.execute(ModifyLogoFragment.State::asyncCreateLogoFile) {
            copy(asyncCreateLogoFile = it)
        }
    }

}