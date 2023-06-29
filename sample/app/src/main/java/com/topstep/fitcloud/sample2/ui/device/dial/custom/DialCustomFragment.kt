package com.topstep.fitcloud.sample2.ui.device.dial.custom

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.polidea.rxandroidble3.exceptions.BleDisconnectedException
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.UnSupportDialCustomException
import com.topstep.fitcloud.sample2.data.UnSupportDialLcdException
import com.topstep.fitcloud.sample2.data.device.isConnected
import com.topstep.fitcloud.sample2.databinding.FragmentDialCustomBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.dial.DialCustomParams
import com.topstep.fitcloud.sample2.model.dial.DialPushParams
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.device.dial.DialPushViewModel
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.utils.dial.DialDrawer
import kotlinx.coroutines.launch
import java.io.File

class DialCustomFragment : GetPhotoFragment(R.layout.fragment_dial_custom) {

    private val viewBind: FragmentDialCustomBinding by viewBinding()

    private val dialPushViewModel: DialPushViewModel by viewModels()
    private val viewModel: DialCustomViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DialCustomViewModel(dialPushViewModel) as T
            }
        }
    }

    private var selectBackgroundUri: Uri? = null
    private lateinit var styleAdapter: DialCustomStyleAdapter
    private var selectPosition = DialDrawer.Position.TOP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        styleAdapter = DialCustomStyleAdapter()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.refresh()
        }

        viewBind.btnCreateDial.clickTrigger {
            if (dialPushViewModel.deviceManager.isConnected()) {
                viewModel.state.async()?.let {
                    val background = selectBackgroundUri ?: it.custom.defaultBackgroundUri
                    val style = it.custom.styles.getOrNull(styleAdapter.selectPosition) ?: it.custom.styles[0]
                    val position = selectPosition
                    DialCustomDfuDialogFragment.newInstance(background, style, position, it.pushParams)
                        .show(childFragmentManager, null)
                }
            } else {
                promptToast.showInfo(R.string.device_state_disconnected)
            }
        }

        viewBind.btnSelectBackground.clickTrigger {
            getPhoto(CROP_MUST)
        }

        viewBind.styleRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        viewBind.styleRecyclerView.adapter = styleAdapter
        styleAdapter.listener = object : DialCustomStyleAdapter.Listener {
            override fun onItemSelect(position: Int, item: DialCustomParams.Style) {
                //style changed
                glideLoadDialStyle(requireContext(), viewBind.dialView, item.styleUri, item.styleBaseOnWidth)
                viewBind.btnCreateDial.text = getString(R.string.ds_dial_create, fileSizeStr(item.binSize))
            }
        }

        viewBind.rgPosition.setOnCheckedChangeListener { group, checkedId ->
            //position changed
            selectPosition = when (checkedId) {
                R.id.rb_top -> DialDrawer.Position.TOP
                R.id.rb_bottom -> DialDrawer.Position.BOTTOM
                R.id.rb_left -> DialDrawer.Position.LEFT
                R.id.rb_right -> DialDrawer.Position.RIGHT
                else -> throw IllegalStateException()
            }
            viewBind.dialView.stylePosition = selectPosition
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    when (val async = it.async) {
                        Uninitialized, is Loading -> {
                            viewBind.loadingView.showLoading()
                        }
                        is Fail -> {
                            when (async.error) {
                                is UnSupportDialLcdException -> {
                                    viewBind.loadingView.showError(R.string.ds_dial_error_shape)
                                }
                                is UnSupportDialCustomException -> {//No components or styles available
                                    viewBind.loadingView.showError(R.string.ds_dial_error_style)
                                }
                                else -> {
                                    viewBind.loadingView.showError(R.string.tip_load_error)
                                }
                            }
                        }
                        is Success -> {
                            val result = async()
                            val shape = result.pushParams.shape
                            viewBind.dialView.shape = shape
                            viewBind.loadingView.visibility = View.GONE

                            val dialCustomParams = result.custom
                            //update background
                            val backgroundUri = selectBackgroundUri ?: dialCustomParams.defaultBackgroundUri
                            glideLoadDialBackground(requireContext(), viewBind.dialView, backgroundUri)

                            //update style
                            styleAdapter.items = dialCustomParams.styles
                            val style = dialCustomParams.styles.getOrNull(styleAdapter.selectPosition) ?: dialCustomParams.styles[0]
                            glideLoadDialStyle(requireContext(), viewBind.dialView, style.styleUri, style.styleBaseOnWidth)
                            viewBind.btnCreateDial.text = getString(R.string.ds_dial_create, fileSizeStr(style.binSize))

                            //update position
                            viewBind.dialView.stylePosition = selectPosition
                        }
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect {
                    when (it) {
                        is AsyncEvent.OnFail -> {
                            promptToast.showFailed(it.error)
                        }
                        else -> {}
                    }
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                launch {
                    flowLocationServiceState(requireContext()).collect { isEnabled ->
                        viewBind.layoutLocationService.isVisible = !isEnabled
                    }
                }
            } else {
                viewBind.layoutLocationService.isVisible = false
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
        val shape = viewBind.dialView.shape
        return CropParam(shape.width, shape.height, shape.width, shape.height)
    }

    override fun onGetPhoto(uri: Uri) {
        //background changed
        selectBackgroundUri = uri
        glideLoadDialBackground(requireContext(), viewBind.dialView, uri)
    }
}

data class PushParamsAndCustom(val pushParams: DialPushParams, val custom: DialCustomParams) {
    override fun toString(): String {
        return "pushParams:$pushParams , custom:$custom"
    }
}

/**
 * Request and combine [DialPushParams] and [DialCustomParams]
 */
class DialCustomViewModel(
    private val dialPushViewModel: DialPushViewModel,
) : AsyncViewModel<SingleAsyncState<PushParamsAndCustom>>(SingleAsyncState()) {

    private val dialRepository = Injector.getDialRepository()

    init {
        viewModelScope.launch {
            dialPushViewModel.flowState.collect {
                when (val async = it.async) {
                    is Loading -> {
                        state.copy(async = Loading()).newState()
                    }
                    is Fail -> {
                        state.copy(async = Fail(async.error)).newState()
                    }
                    is Success -> {
                        refreshInternal(async())
                    }
                    else -> {}
                }
            }
        }
    }

    private fun refreshInternal(pushParams: DialPushParams) {
        suspend {
            val custom = dialRepository.getDialCustomParams(pushParams)
            PushParamsAndCustom(pushParams, custom)
        }.execute(SingleAsyncState<PushParamsAndCustom>::async) {
            copy(async = it)
        }
    }

    fun refresh() {
        when (val async = dialPushViewModel.state.async) {
            is Loading -> {
                //Requesting, so there is no need to process the request again
            }
            is Fail -> {
                if (async.error is BleDisconnectedException) {
                    //Device disconnected without performing refresh operation
                } else {
                    //Previous request failed, request DialParam again
                    dialPushViewModel.refresh()
                }
            }
            is Success -> {
                refreshInternal(async())
            }
            else -> {}
        }
    }

}