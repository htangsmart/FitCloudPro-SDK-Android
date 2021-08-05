package com.github.kilnn.wristband2.sample.dial.library

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.kilnn.wristband2.sample.MyApplication
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.databinding.DialogDialUpgradeBinding
import com.github.kilnn.wristband2.sample.dfu.DfuDialogFragment
import com.github.kilnn.wristband2.sample.dial.DialBinSelectFragment
import com.github.kilnn.wristband2.sample.dial.DialDownloadException
import com.github.kilnn.wristband2.sample.dial.DialFileHelper
import com.github.kilnn.wristband2.sample.dial.entity.DialInfo
import com.github.kilnn.wristband2.sample.dial.task.DialParam
import com.github.kilnn.wristband2.sample.file.SimpleFileDownloader
import com.github.kilnn.wristband2.sample.utils.Utils
import com.htsmart.wristband2.WristbandApplication
import com.htsmart.wristband2.dfu.DfuCallback
import com.htsmart.wristband2.dfu.DfuManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class DialUpgradeFragment : AppCompatDialogFragment(), DialBinSelectFragment.Listener {

    interface Listener {
        fun scheduleUpgrade(runnable: Runnable)
    }

    companion object {
        private const val TAG = "DialUpgradeFragment"
        private const val EXTRA_DIAL_INFO = "dial_info"
        private const val EXTRA_DIAL_PARAM = "dial_param"

        fun newInstance(dialInfo: DialInfo, param: DialParam): DialUpgradeFragment {
            val fragment = DialUpgradeFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(EXTRA_DIAL_INFO, dialInfo)
                putParcelable(EXTRA_DIAL_PARAM, param)
            }
            return fragment
        }
    }

    override fun onDialBinSelect(binFlag: Byte) {
        startSyncDial(binFlag)
    }

    private var listener: Listener? = null

    private lateinit var dialInfo: DialInfo
    private lateinit var param: DialParam
    private lateinit var dfuManager: DfuManager

    private val manager = WristbandApplication.getWristbandManager()

    private val appDatabase = MyApplication.getSyncDataDb()

    private var _viewBind: DialogDialUpgradeBinding? = null
    private val viewBind get() = _viewBind!!

    private var binSize = 0L

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment != null && parentFragment is Listener) {
            listener = parentFragment as Listener?
        } else if (context is Listener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            dialInfo = it.getParcelable(EXTRA_DIAL_INFO)!!
            param = it.getParcelable(EXTRA_DIAL_PARAM)!!
        }

        binSize = dialInfo.binSize
        if (binSize <= 0L) {//没有大小数据，从本地加载试试
            val binFile = DialFileHelper.getNormalFileByUrl(requireContext(), dialInfo.binUrl)
            if (binFile?.exists() == true) {
                binSize = binFile.length()
            }
        }

        dfuManager = DfuManager(context)
        dfuManager.setDfuCallback(dfuCallback)
        dfuManager.init()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogDialUpgradeBinding.inflate(LayoutInflater.from(context))

        viewBind.tvTitle.text = dialInfo.name

        val requestOptions = RequestOptions.placeholderOf(R.drawable.ic_dial_load_failed)
        val deviceImgFile = DialFileHelper.getNormalFileByUrl(requireContext(), dialInfo.deviceImgUrl)
        if (deviceImgFile != null && deviceImgFile.exists()) {
            Glide.with(requireContext())
                .load(deviceImgFile)
                .apply(requestOptions)
                .into(viewBind.imgView)
        } else {
            Glide.with(requireContext())
                .load(dialInfo.deviceImgUrl)
                .apply(requestOptions)
                .into(viewBind.imgView)
        }

        viewBind.upgradeDialView.text = getString(R.string.ds_dial_sync) + "（" + Utils.fileSizeStr(binSize) + "）"
        viewBind.upgradeDialView.setOnClickListener {
            listener?.let {
                it.scheduleUpgrade {
                    if (param.isSelectableDialBinParams()) {
                        //有多表盘，先选择升级位置
                        DialBinSelectFragment.newInstance(param, binSize).show(childFragmentManager, null)
                    } else {
                        //没有多表盘信息，直接升级
                        startSyncDial(0)
                    }
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(viewBind.root)
            .setCancelable(true)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

    private fun startSyncDial(binFlag: Byte) = lifecycleScope.launch {
        try {
            //1.检查是否需要下载
            val imgFile = DialFileHelper.getNormalFileByUrl(requireContext(), dialInfo.imgUrl)
            val needDownLoadImg = imgFile == null || !imgFile.exists()

            val deviceImgFile = DialFileHelper.getNormalFileByUrl(requireContext(), dialInfo.deviceImgUrl)
            val needDownloadDeviceImg = deviceImgFile == null || !deviceImgFile.exists()

            val binFile = DialFileHelper.getNormalFileByUrl(requireContext(), dialInfo.binUrl)
            var binPath = if (binFile != null && binFile.exists()) binFile.absolutePath else null

            //2.下载文件
            viewBind.upgradeDialView.isEnabled = false
            viewBind.upgradeDialView.setText(R.string.ds_dial_downloading)
            viewBind.upgradeDialView.setProgress(0)

            if (needDownLoadImg) {
                DialFileHelper.download(requireContext(), dialInfo.imgUrl)
            }
            if (needDownloadDeviceImg) {
                DialFileHelper.download(requireContext(), dialInfo.deviceImgUrl)
            }
            if (binPath.isNullOrEmpty()) {
                DialFileHelper.downloadWithProgress(requireContext(), dialInfo.binUrl)
                    .collect {
                        if (it.progress == 100) {
                            binPath = it.result
                        } else {
                            viewBind.upgradeDialView.setProgress(it.progress)
                        }
                    }
            }

            isCancelable = false
            viewBind.upgradeDialView.setText(R.string.ds_dial_syncing)
            viewBind.upgradeDialView.setProgress(0)
            dfuManager.upgradeDial(binPath, binFlag)
        } catch (e: Exception) {
            e.printStackTrace()
            resetOriginalState()
            if (e is DialDownloadException) {
                if (e.errorCode == SimpleFileDownloader.ERROR_SD_CARD) {
                    toast(R.string.version_download_failed_check_sd_card)
                } else {
                    toast(R.string.version_download_failed)
                }
            } else {
                toast(Utils.parserError(context, e))
            }
        }
    }

    private fun resetOriginalState() {
        viewBind.upgradeDialView.isEnabled = true
        viewBind.upgradeDialView.text = getString(R.string.ds_dial_sync) + "（" + Utils.fileSizeStr(binSize) + "）"
        viewBind.upgradeDialView.setProgress(100)
        isCancelable = true
    }

    private val dfuCallback: DfuCallback = object : DfuCallback {
        override fun onError(errorType: Int, errorCode: Int) {
            DfuDialogFragment.toastError(context, errorType, errorCode)
            //TODO 如果当升级失败时，需要立即回连，那么你要在这里调用连接
            // manager.connect()
            resetOriginalState()
        }

        override fun onStateChanged(state: Int, cancelable: Boolean) {

        }

        override fun onProgressChanged(progress: Int) {
            viewBind.upgradeDialView.setProgress(progress)
        }

        override fun onSuccess() {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    //保存到数据库，表示使用过这个表盘
                    appDatabase.dialInfoDao().save(dialInfo)
                }

                viewBind.upgradeDialView.setText(R.string.ds_dial_sync_success)
                viewBind.upgradeDialView.setProgress(100)
                isCancelable = true

                //TODO 如果当升级成功时，需要立即回连，那么你要在这里调用连接
                // manager.connect()

                delay(3000)

                dismissAllowingStateLoss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        //TODO 如果当升级取消时，需要立即回连，那么你要在这里调用连接
        // manager.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        //释放dfuManager
        dfuManager.release()
    }

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun toast(@StringRes resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

}