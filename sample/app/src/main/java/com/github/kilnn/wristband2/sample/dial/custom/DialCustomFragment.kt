package com.github.kilnn.wristband2.sample.dial.custom

import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.github.kilnn.wristband2.sample.ConnectActivity
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.databinding.DialogDialCustomBinding
import com.github.kilnn.wristband2.sample.dfu.DfuDialogFragment
import com.github.kilnn.wristband2.sample.dial.DialFileHelper
import com.htsmart.wristband2.WristbandApplication
import com.htsmart.wristband2.dfu.DfuCallback
import com.htsmart.wristband2.dfu.DfuManager
import com.htsmart.wristband2.dial.DialDrawer
import com.htsmart.wristband2.dial.DialWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class DialCustomFragment : AppCompatDialogFragment() {

    data class Param(
        val isGUI: Boolean,
        val binUrl: String,
        val backgroundUri: Uri,
        val styleIndex: Int,
        val styleUri: Uri,
        val shape: DialDrawer.Shape,
        val scaleType: DialDrawer.ScaleType,
        val position: DialDrawer.Position,
        val styleBaseOnWidth: Int,
        val spaceIndex: Int,
        val binFlag: Byte
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readString() ?: "",
            parcel.readParcelable(Uri::class.java.classLoader)!!,
            parcel.readInt(),
            parcel.readParcelable(Uri::class.java.classLoader)!!,
            parcel.readParcelable(DialDrawer.Shape::class.java.classLoader)!!,
            scaleType = DialDrawer.ScaleType.fromId(parcel.readInt()),
            position = DialDrawer.Position.fromId(parcel.readInt()),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readByte()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeByte(if (isGUI) 1 else 0)
            parcel.writeString(binUrl)
            parcel.writeParcelable(backgroundUri, flags)
            parcel.writeInt(styleIndex)
            parcel.writeParcelable(styleUri, flags)
            parcel.writeParcelable(shape, flags)
            parcel.writeInt(scaleType.id)
            parcel.writeInt(position.id)
            parcel.writeInt(styleBaseOnWidth)
            parcel.writeInt(spaceIndex)
            parcel.writeByte(binFlag)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Param> {
            override fun createFromParcel(parcel: Parcel): Param {
                return Param(parcel)
            }

            override fun newArray(size: Int): Array<Param?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        private const val TAG = "DialCustomFragment"
        private const val EXTRA_PARAM = "param"

        fun newInstance(param: Param): DialCustomFragment {
            val fragment = DialCustomFragment()
            fragment.arguments = Bundle().apply { putParcelable(EXTRA_PARAM, param) }
            return fragment
        }

    }

    private lateinit var param: Param
    private lateinit var dfuManager: DfuManager

    private val manager = WristbandApplication.getWristbandManager()

    private var _viewBind: DialogDialCustomBinding? = null
    private val viewBind get() = _viewBind!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            param = it.getParcelable(EXTRA_PARAM)!!
        }
        dfuManager = DfuManager(context)
        dfuManager.setDfuCallback(dfuCallback)
        dfuManager.init()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogDialCustomBinding.inflate(LayoutInflater.from(context))

        startCreateDial()

        return AlertDialog.Builder(requireContext())
            .setView(viewBind.root)
            .setCancelable(true)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

    private fun startCreateDial() = lifecycleScope.launch {
        try {
            viewBind.tvTitle.setText(R.string.ds_dial_create)
            viewBind.tvPercent.text = null
            viewBind.progressBar.isIndeterminate = true

            //检查是否需要下载
            var binSource = DialFileHelper.getNormalFileByUrl(requireContext(), param.binUrl)
            if (binSource == null || !binSource.exists()) {
                binSource = File(DialFileHelper.download(requireContext(), param.binUrl))
            }
            val backgroundSource = DialFileHelper.glideGetBitmap(requireContext(), param.backgroundUri)
            val styleSource = DialFileHelper.glideGetBitmap(requireContext(), param.styleUri)

            //创建所需的资源
            val background = DialDrawer.createDialBackground(backgroundSource, param.shape, param.scaleType)
            val preview = DialDrawer.createDialPreview(backgroundSource, styleSource, param.shape, param.scaleType, param.position, param.styleBaseOnWidth, param.shape.width(), param.shape.height())

            //writer生成bin文件
            val writer = DialWriter(binSource, background, preview, param.position, param.isGUI)
            val temp = File(binSource.parent, "temp_" + binSource.name)
            writer.setCopyFile(temp)
            writer.setAutoScalePreview(true)
            val result = writer.execute()

            //开始升级
            viewBind.tvTitle.setText(R.string.ds_dial_syncing)
            viewBind.progressBar.progress = 0
            viewBind.progressBar.isIndeterminate = false
            dfuManager.upgradeDial(result.absolutePath, param.binFlag)
        } catch (e: Exception) {
            e.printStackTrace()
            toast(R.string.ds_dial_create_failed)
            dismissAllowingStateLoss()
        }
    }

    private val dfuCallback: DfuCallback = object : DfuCallback {
        override fun onError(errorType: Int, errorCode: Int) {
            DfuDialogFragment.toastError(context, errorType, errorCode)

            ConnectActivity.sendReconnectAction(requireContext())

            dismissAllowingStateLoss()
        }

        override fun onStateChanged(state: Int, cancelable: Boolean) {

        }

        override fun onProgressChanged(progress: Int) {
            viewBind.tvPercent.text = "$progress%"
            viewBind.progressBar.progress = progress
        }

        override fun onSuccess() {
            viewBind.tvTitle.setText(R.string.ds_dial_sync_success);
            viewBind.tvPercent.text = "100%"
            viewBind.progressBar.progress = 100
            isCancelable = true

            ConnectActivity.sendReconnectAction(requireContext())
            if (param.isGUI) {
                ConnectActivity.sendDialComponentAction(requireContext(), param.spaceIndex, param.styleIndex)
            }

            lifecycleScope.launch {
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
