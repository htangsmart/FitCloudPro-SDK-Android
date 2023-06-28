package com.topstep.fitcloud.sample2.ui.device.epo

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.MyApplication
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentEpoUpgradeBinding
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.device.DfuViewModel
import com.topstep.fitcloud.sample2.ui.device.showDfuFail
import com.topstep.fitcloud.sample2.utils.AppFiles
import com.topstep.fitcloud.sample2.utils.DisposableCancellationSignal
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.util.download.FileDownloadException
import com.topstep.fitcloud.sdk.util.download.FileDownloader
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import com.topstep.fitcloud.sdk.v2.utils.DfuFileHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx3.asFlow
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class EpoUpgradeFragment : BaseFragment(R.layout.fragment_epo_upgrade) {

    private val viewBind: FragmentEpoUpgradeBinding by viewBinding()

    private val dfuViewModel: DfuViewModel by viewModels()
    private val viewModel: EpoUpgradeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.btnUpgrade.clickTrigger {
            viewModel.createEpoFile()
        }

        lifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.combine(dfuViewModel.flowDfuStateProgress()) { createState, dfuState ->
                    CombineState(createState.async, dfuState)
                }.collect {
                    when (it.createState) {
                        is Loading -> {
                            viewBind.btnUpgrade.visibility = View.GONE
                            viewBind.layoutUpgrade.visibility = View.VISIBLE
                            viewBind.tvState.setText(R.string.ds_dfu_downloading)
                        }
                        is Fail -> {
                            viewBind.btnUpgrade.visibility = View.VISIBLE
                            viewBind.layoutUpgrade.visibility = View.GONE
                        }
                        is Success -> {
                            viewBind.btnUpgrade.visibility = View.GONE
                            viewBind.layoutUpgrade.visibility = View.VISIBLE
                            when (it.dfuState.state) {
                                FcDfuManager.DfuState.NONE, FcDfuManager.DfuState.DFU_FAIL, FcDfuManager.DfuState.DFU_SUCCESS -> {
                                    viewBind.btnUpgrade.visibility = View.VISIBLE
                                    viewBind.layoutUpgrade.visibility = View.GONE
                                }
                                FcDfuManager.DfuState.DOWNLOAD_FILE -> {
                                    viewBind.tvState.setText(R.string.ds_dfu_downloading)
                                }
                                FcDfuManager.DfuState.PREPARE_FILE, FcDfuManager.DfuState.PREPARE_DFU -> {
                                    viewBind.tvState.setText(R.string.ds_dfu_preparing)
                                }
                                FcDfuManager.DfuState.DFU_ING -> {
                                    viewBind.tvState.setText(R.string.ds_dfu_pushing)
                                }
                            }
                            viewBind.progressBar.progress = it.dfuState.progress
                        }
                        else -> {}
                    }
                }
            }
            launch {
                dfuViewModel.flowDfuEvent.collect {
                    when (it) {
                        is DfuViewModel.DfuEvent.OnSuccess -> {
                            promptToast.showSuccess(R.string.ds_push_success, intercept = true)
                        }
                        is DfuViewModel.DfuEvent.OnFail -> {
                            promptToast.showDfuFail(requireContext(), it.error)
                        }
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect {
                    when (it) {
                        is AsyncEvent.OnFail -> {
                            showCreateEpoFail(it.error)
                        }
                        is AsyncEvent.OnSuccess<*> -> {
                            if (it.property == SingleAsyncState<File>::async) {
                                val file = it.value as File
                                dfuViewModel.startDfu(
                                    FcDfuManager.DfuType.UI, Uri.fromFile(file), 0
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    private fun showCreateEpoFail(throwable: Throwable) {
        Timber.w(throwable)
        val toastId = if (throwable is FileDownloadException) {
            if (throwable.errorCode == FileDownloadException.ERROR_CODE_INSUFFICIENT_STORAGE) {
                R.string.ds_dfu_error_insufficient_storage
            } else {
                R.string.tip_download_failed
            }
        } else {
            R.string.epo_create_fail
        }
        promptToast.showFailed(toastId)
    }
}

private data class CombineState(
    val createState: Async<File>,
    val dfuState: FcDfuManager.StateProgress
)

class EpoUpgradeViewModel : AsyncViewModel<SingleAsyncState<File>>(SingleAsyncState()) {

    private val context = MyApplication.instance
    private val url1 = "https://elpo.airoha.com/ELPO_GR3_1.DAT?vendor=T-MAX&project=ZG2oL3k-H1Zx76K4kPei6htZ8KFaTEcB2IDxzvlaeQw&device_id=111"
    private val url2 = "https://elpo.airoha.com/ELPO_GR3_2.DAT?vendor=T-MAX&project=ZG2oL3k-H1Zx76K4kPei6htZ8KFaTEcB2IDxzvlaeQw&device_id=111"

    private var epoFile1: File? = null
    private var epoFile2: File? = null

    private val fileDownloader by lazy {
        FileDownloader.newInstance(AppFiles.dirDownload(context), 30_000)
    }

    fun createEpoFile() {
        viewModelScope.launch {
            suspend {
                withContext(Dispatchers.IO) {
                    val file1 = epoFile1 ?: fileDownloader.download(url1, null, true).asFlow().filter { it.progress == 100 }.first().result!!
                    val file2 = epoFile2 ?: fileDownloader.download(url2, null, true).asFlow().filter { it.progress == 100 }.first().result!!
                    suspendCancellableCoroutine { cont ->
                        val cancelSignal = DisposableCancellationSignal()
                        cont.invokeOnCancellation {
                            cancelSignal.dispose()
                        }
                        try {
                            cont.resume(DfuFileHelper.createEpoDfuFile(context, listOf(file1, file2), null, cancelSignal))
                        } catch (e: Exception) {
                            cont.resumeWithException(e)
                        }
                    }
                }
            }.execute(SingleAsyncState<File>::async) {
                copy(async = it)
            }
        }
    }

}
