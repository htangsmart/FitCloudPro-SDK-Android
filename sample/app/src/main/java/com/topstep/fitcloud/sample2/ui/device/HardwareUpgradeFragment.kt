package com.topstep.fitcloud.sample2.ui.device

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentHardwareUpgradeBinding
import com.topstep.fitcloud.sample2.model.version.HardwareType
import com.topstep.fitcloud.sample2.model.version.hardwareInfoDisplay
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import kotlinx.coroutines.launch
import timber.log.Timber

class HardwareUpgradeFragment : BaseFragment(R.layout.fragment_hardware_upgrade) {

    private val viewBind: FragmentHardwareUpgradeBinding by viewBinding()
    private val dfuViewModel: DfuViewModel by viewModels()
    private val args: HardwareUpgradeFragmentArgs by navArgs()

    private var hasSuccess = false

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK) {
            if (uri == null) {
                promptToast.showFailed("Select file error!")
            } else {
                dfuViewModel.startDfu(
                    FcDfuManager.DfuType.FIRMWARE,
                    uri,
                    0
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //If it is a forced upgrade, the upgrade must be successful before exit is allowed
                if (args.info?.isForce == true && !hasSuccess) {
                    promptToast.showInfo(R.string.version_hardware_update)
                } else {
                    findNavController().navigateUp()
                }
            }
        })
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    //If it is a forced upgrade, the upgrade must be successful before exit is allowed
                    if (args.info?.isForce == true && !hasSuccess) {
                        promptToast.showInfo(R.string.version_hardware_update)
                    } else {
                        findNavController().navigateUp()
                    }
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)


        val info = args.info
        if (info == null) {
            viewBind.btnUpgrade.setText(R.string.version_upgrade)
        } else {
            viewBind.btnUpgrade.text = getString(R.string.version_upgrade_param, fileSizeStr(info.size))
            viewBind.tvVersion.text = info.hardwareInfo.hardwareInfoDisplay()
            viewBind.tvContent.text = info.remark
        }

        viewBind.btnUpgrade.clickTrigger {
            //ToNote:Request permission first before upgrade
            PermissionHelper.requestBle(this) { granted ->
                if (granted) {
                    upgrade()
                }
            }
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                dfuViewModel.flowDfuStateProgress().collect {
                    when (it.state) {
                        FcDfuManager.DfuState.NONE -> {
                            viewBind.btnUpgrade.isEnabled = true
                            viewBind.layoutUpgradeProgress.setStateNone()
                        }
                        FcDfuManager.DfuState.DFU_FAIL -> {
                            viewBind.btnUpgrade.isEnabled = true
                            viewBind.layoutUpgradeProgress.setStateStop(false)
                        }
                        FcDfuManager.DfuState.DFU_SUCCESS -> {
                            viewBind.btnUpgrade.isEnabled = true
                            viewBind.layoutUpgradeProgress.setStateStop(true)
                        }
                        FcDfuManager.DfuState.DOWNLOAD_FILE, FcDfuManager.DfuState.PREPARE_FILE, FcDfuManager.DfuState.PREPARE_DFU -> {
                            viewBind.btnUpgrade.isEnabled = false
                            viewBind.layoutUpgradeProgress.setStatePrepare()
                        }
                        FcDfuManager.DfuState.DFU_ING -> {
                            viewBind.btnUpgrade.isEnabled = false
                            viewBind.layoutUpgradeProgress.setStateProgress(it.progress)
                        }
                    }
                }
            }
            launch {
                dfuViewModel.flowDfuEvent.collect {
                    when (it) {
                        is DfuViewModel.DfuEvent.OnSuccess -> {
                            hasSuccess = true
                        }
                        is DfuViewModel.DfuEvent.OnFail -> {
                            promptToast.showDfuFail(requireContext(), it.error)
                        }
                    }
                }
            }
            //ToNote:On Android 11 or below, may be Location Service is required
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

    private fun upgrade() {
        val info = args.info
        if (info == null) {
            //Select local file
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            try {
                selectFileLauncher.launch(intent)
            } catch (e: Exception) {
                Timber.w(e)
                promptToast.showFailed("Select file error!")
            }
        } else {
            //Use HardwareUpgradeInfo's url
            if (info.type == HardwareType.FLASH) {
                promptToast.showFailed(R.string.version_dfu_unsupport_flash)
            } else {
                dfuViewModel.startDfu(
                    FcDfuManager.DfuType.FIRMWARE,
                    Uri.parse(info.url),
                    0
                )
            }
        }
    }

}
