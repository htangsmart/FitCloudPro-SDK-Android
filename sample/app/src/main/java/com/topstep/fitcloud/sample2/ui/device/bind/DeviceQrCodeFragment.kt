package com.topstep.fitcloud.sample2.ui.device.bind

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.dialog.prompt.PromptDialogFragment
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentDeviceQrCodeBinding
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import java.net.URLDecoder
import java.util.regex.Pattern

class DeviceQrCodeFragment : BaseFragment(R.layout.fragment_device_qr_code), PromptDialogFragment.OnPromptListener {
    private /*const*/ val promptBindSuccessId = 1

    private val viewBind: FragmentDeviceQrCodeBinding by viewBinding()
    private val addressPattern = Pattern.compile("([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.tvContent.text = "https://fitcloud.hetangsmart.com/qrcode/cn?MAC=AF:3C:21:B1:EF:61&BtName=Fit%20Watch"
        viewBind.btnParser.setOnClickListener {
            parser(viewBind.tvContent.text.toString())
        }
    }

    private fun parser(from: String?) {
        if (from.isNullOrEmpty()) {
            return
        }
        val str = URLDecoder.decode(from, "UTF-8")
        val address = findAddress(str)
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            val name = findName(str)
            if (!name.isNullOrEmpty()) {
                //find success
                setFragmentResult(DeviceBindFragment.DEVICE_QR_CODE, Bundle().apply {
                    putString(DeviceBindFragment.EXTRA_ADDRESS, address)
                    putString(DeviceBindFragment.EXTRA_NAME, name)
                })
                promptToast.showSuccess(
                    "address:$address name:$name",
                    intercept = true,
                    promptId = promptBindSuccessId
                )
            }
        }
    }

    private fun findAddress(str: String): String? {
        val matcher = addressPattern.matcher(str)
        return if (matcher.find()) {
            matcher.group()
        } else {
            null
        }
    }

    private fun findName(str: String): String? {
        val index = str.indexOf(NAME_PREFIX)
        if (index == -1) return null
        val nameStart = index + NAME_PREFIX.length
        val nameEnd = str.indexOf("&", nameStart)
        return if (nameEnd != -1) {
            str.substring(nameStart, nameEnd)
        } else {
            str.substring(nameStart)
        }
    }

    companion object {
        private const val NAME_PREFIX = "BtName="
    }

    override fun onPromptCancel(promptId: Int, cancelReason: Int, tag: String?) {
        if (promptId == promptBindSuccessId) {
            findNavController().popBackStack()
        }
    }
}