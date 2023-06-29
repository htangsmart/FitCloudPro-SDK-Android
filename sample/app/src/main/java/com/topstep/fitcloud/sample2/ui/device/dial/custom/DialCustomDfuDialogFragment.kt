package com.topstep.fitcloud.sample2.ui.device.dial.custom

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.MyApplication
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.DialogDialCustomDfuBinding
import com.topstep.fitcloud.sample2.model.dial.DialCustomParams
import com.topstep.fitcloud.sample2.model.dial.DialPushParams
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.device.DfuViewModel
import com.topstep.fitcloud.sample2.ui.device.dial.DialSpacePacketAdapter
import com.topstep.fitcloud.sample2.ui.device.showDfuFail
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sdk.util.download.FileDownloadException
import com.topstep.fitcloud.sdk.util.download.FileDownloader
import com.topstep.fitcloud.sdk.util.download.ProgressResult
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import com.topstep.fitcloud.sdk.v2.utils.dial.DialDrawer
import com.topstep.fitcloud.sdk.v2.utils.dial.DialDrawer.Position
import com.topstep.fitcloud.sdk.v2.utils.dial.DialWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class DialCustomDfuDialogFragment : AppCompatDialogFragment() {

    companion object {
        internal const val EXTRA_BACKGROUND = "background"
        internal const val EXTRA_STYLE = "style"
        internal const val EXTRA_POSITION = "position"
        internal const val EXTRA_PUSH_PARAMS = "push_params"

        fun newInstance(background: Uri, style: DialCustomParams.Style, position: Position, params: DialPushParams): DialCustomDfuDialogFragment {
            val fragment = DialCustomDfuDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(EXTRA_BACKGROUND, background)
                putParcelable(EXTRA_STYLE, style)
                putInt(EXTRA_POSITION, position.ordinal)
                putParcelable(EXTRA_PUSH_PARAMS, params)
            }
            return fragment
        }
    }

    private lateinit var background: Uri
    private lateinit var style: DialCustomParams.Style
    private lateinit var position: Position
    private lateinit var pushParams: DialPushParams
    private var adapter: DialSpacePacketAdapter? = null

    private var _viewBind: DialogDialCustomDfuBinding? = null
    private val viewBind get() = _viewBind!!

    private val dfuViewModel: DfuViewModel by viewModels({ requireParentFragment() })
    private val viewModel: DialCustomDfuViewModel by viewModels()
    private val toast by promptToast()

    private var spaceIndexSelected: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        background = viewModel.background
        style = viewModel.style
        position = viewModel.position
        pushParams = viewModel.pushParams
        val pushableSpacePackets = pushParams.filterPushableSpacePackets()
        if (pushableSpacePackets.isNotEmpty()) {
            adapter = DialSpacePacketAdapter(pushableSpacePackets, style.binSize, pushParams.shape)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogDialCustomDfuBinding.inflate(LayoutInflater.from(context))

        viewBind.tvName.setText(R.string.ds_dial_custom)

        viewBind.dialView.shape = pushParams.shape
        glideLoadDialBackground(requireContext(), viewBind.dialView, background)
        glideLoadDialStyle(requireContext(), viewBind.dialView, style.styleUri, style.styleBaseOnWidth)
        viewBind.dialView.stylePosition = position

        if (adapter == null) {
            viewBind.layoutSelect.visibility = View.GONE
        } else {
            viewBind.layoutSelect.visibility = View.VISIBLE
            viewBind.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            viewBind.recyclerView.adapter = adapter
        }

        resetStateView()

        viewBind.stateView.clickTrigger {
            when (val dialFileState = viewModel.state.dialFile) {
                is Uninitialized, is Fail -> {
                    viewModel.createDial()
                }
                is Loading -> {}
                is Success -> {
                    if (!dfuViewModel.isDfuIng()) {
                        PermissionHelper.requestBle(this) { granted ->
                            if (granted) {
                                spaceIndexSelected = adapter?.getSelectedItem()?.spaceIndex ?: 0
                                val binFlag = adapter?.getSelectedItem()?.binFlag ?: 0
                                dfuViewModel.startDfu(FcDfuManager.DfuType.DIAL, Uri.fromFile(dialFileState()), binFlag)
                            }
                        }
                    }
                }
            }
        }

        lifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.combine(dfuViewModel.flowDfuStateProgress()) { createState, dfuState ->
                    CombineState(createState, dfuState)
                }.collect {
                    when (it.createState.dialFile) {
                        is Loading -> {
                            val progress = it.createState.progress
                            viewBind.stateView.progress = progress
                            if (progress < 100) {
                                viewBind.stateView.setText(R.string.ds_dfu_downloading)
                            } else {
                                viewBind.stateView.setText(R.string.ds_dial_creating)
                            }
                        }
                        is Fail -> {
                            resetStateView()
                        }
                        is Success -> {
                            when (it.dfuState.state) {
                                FcDfuManager.DfuState.NONE, FcDfuManager.DfuState.DFU_FAIL, FcDfuManager.DfuState.DFU_SUCCESS -> {
                                    resetStateView()
                                    isCancelable = true
                                }
                                FcDfuManager.DfuState.DOWNLOAD_FILE -> {
                                    viewBind.stateView.setText(R.string.ds_dfu_downloading)
                                    isCancelable = false
                                }
                                FcDfuManager.DfuState.PREPARE_FILE, FcDfuManager.DfuState.PREPARE_DFU -> {
                                    viewBind.stateView.setText(R.string.ds_dfu_preparing)
                                    isCancelable = false
                                }
                                FcDfuManager.DfuState.DFU_ING -> {
                                    viewBind.stateView.setText(R.string.ds_dfu_pushing)
                                    isCancelable = false
                                }
                            }
                            viewBind.stateView.progress = it.dfuState.progress
                        }
                        Uninitialized -> {}
                    }
                }
            }
            launch {
                dfuViewModel.flowDfuEvent.collect {
                    when (it) {
                        is DfuViewModel.DfuEvent.OnSuccess -> {
                            if (pushParams.isSupportGUI) {
                                dfuViewModel.setGUICustomDialComponent(spaceIndexSelected, style.styleIndex)
                            }
                            toast.showSuccess(R.string.ds_push_success, intercept = true)
                            lifecycleScope.launchWhenStarted {
                                delay(2000)
                                dismiss()
                            }
                        }
                        is DfuViewModel.DfuEvent.OnFail -> {
                            toast.showDfuFail(requireContext(), it.error)
                        }
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect {
                    when (it) {
                        is DialCustomDfuViewModel.Event.CreateDialFail -> {
                            showCreateDialFail(it.error)
                        }
                    }
                }
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(viewBind.root)
            .setCancelable(true)
            .create()
    }

    private fun showCreateDialFail(throwable: Throwable) {
        Timber.w(throwable)
        var toastId = 0
        if (throwable is FileDownloadException) {
            toastId = if (throwable.errorCode == FileDownloadException.ERROR_CODE_INSUFFICIENT_STORAGE) {
                R.string.ds_dfu_error_insufficient_storage
            } else {
                R.string.tip_download_failed
            }
        } else if (throwable is DialWriter.WriterException) {
            toastId = R.string.ds_dial_create_fail
        }
        if (toastId != 0) {
            toast.showFailed(toastId)
        } else {
            toast.showFailed(throwable)
        }
    }

    private fun resetStateView() {
        viewBind.stateView.isEnabled = adapter?.hasSelectedItem() ?: true
        if (viewModel.state.dialFile() != null) {
            //If has dial file, the display push start text
            viewBind.stateView.text = getString(R.string.ds_push_start, fileSizeStr(style.binSize))
        } else {
            //No dial file，display create dial text
            viewBind.stateView.text = getString(R.string.ds_dial_create, fileSizeStr(style.binSize))
        }
    }

    private data class CombineState(
        val createState: DialCustomDfuViewModel.State,
        val dfuState: FcDfuManager.StateProgress
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }
}

class DialCustomDfuViewModel constructor(
    savedStateHandle: SavedStateHandle
) : StateEventViewModel<DialCustomDfuViewModel.State, DialCustomDfuViewModel.Event>(State()) {

    private val application = MyApplication.instance
    private val fileDir = AppFiles.dirDownload(application)

    val background: Uri = savedStateHandle[DialCustomDfuDialogFragment.EXTRA_BACKGROUND]!!
    val style: DialCustomParams.Style = savedStateHandle[DialCustomDfuDialogFragment.EXTRA_STYLE]!!
    val position: Position = Position.fromOrdinal(savedStateHandle.get<Int>(DialCustomDfuDialogFragment.EXTRA_POSITION)!!)
    val pushParams: DialPushParams = savedStateHandle[DialCustomDfuDialogFragment.EXTRA_PUSH_PARAMS]!!

    init {
        createDial()
    }

    fun createDial() {
        viewModelScope.launch {
            runCatchingWithLog {
                state.copy(dialFile = Loading(), progress = ProgressResult.PROGRESS_UNKNOWN).newState()

                withContext(Dispatchers.IO) {
                    val binSource = FileDownloader.newInstance(fileDir, 30_000).download(
                        style.binUrl, null, true
                    ).asFlow().filter {
                        state.copy(progress = it.progress).newState()
                        it.progress == 100
                    }.first().result!!

                    val backgroundSource = glideGetBitmap(application, background)
                    val styleSource = glideGetBitmap(application, style.styleUri)

                    //创建所需的资源
                    val background = DialDrawer.createDialBackground(backgroundSource, pushParams.shape, DialDrawer.ScaleType.CENTER_CROP)
                    val preview = DialDrawer.createDialPreview(
                        backgroundSource, styleSource, pushParams.shape, DialDrawer.ScaleType.CENTER_CROP, position, style.styleBaseOnWidth,
                        pushParams.shape.width, pushParams.shape.height
                    )

                    //writer生成bin文件
                    val writer = DialWriter(binSource, background!!, preview, position, pushParams.isSupportGUI)
                    val temp = File(binSource.parent, "temp_" + binSource.name)
                    writer.setCopyFile(temp)
                    writer.setAutoScaleBackground(true)
                    writer.setAutoScalePreview(true)
                    writer.execute()
                }
            }.onFailure {
                state.copy(dialFile = Fail(it)).newState()
                Event.CreateDialFail(it).newEvent()
            }.onSuccess {
                state.copy(dialFile = Success(it)).newState()
            }
        }
    }

    /**
     * When [dialFile] is [Loading]
     * [progress] -1 :Unknown progress
     * [progress] 0-99:Downloading
     * [progress] 100:Download completed, generating dial
     *
     * When [dialFile] is [Success], generate dial success
     * When [dialFile] is [Fail] , generate dial failed
     */
    data class State(
        val dialFile: Async<File> = Uninitialized,
        val progress: Int = ProgressResult.PROGRESS_UNKNOWN,
    )

    sealed class Event {
        class CreateDialFail(val error: Throwable) : Event()
    }

}