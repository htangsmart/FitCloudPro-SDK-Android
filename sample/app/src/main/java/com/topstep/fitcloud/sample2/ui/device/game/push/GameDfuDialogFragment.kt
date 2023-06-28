package com.topstep.fitcloud.sample2.ui.device.game.push

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.pro.ui.device.game.push.GameSpaceSkinAdapter
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.DialogGameDfuBinding

import com.topstep.fitcloud.sample2.model.game.push.GameSkin
import com.topstep.fitcloud.sample2.model.game.push.GameSpaceSkin
import com.topstep.fitcloud.sample2.ui.device.DfuViewModel
import com.topstep.fitcloud.sample2.ui.device.showDfuFail
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameDfuDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val EXTRA_NAME = "name"
        private const val EXTRA_SKIN = "skin"
        private const val EXTRA_SPACES = "spaces"

        fun newInstance(name: String, skin: GameSkin, spaces: ArrayList<GameSpaceSkin>): GameDfuDialogFragment {
            val fragment = GameDfuDialogFragment()
            fragment.arguments = Bundle().apply {
                putString(EXTRA_NAME, name)
                putParcelable(EXTRA_SKIN, skin)
                putParcelableArrayList(EXTRA_SPACES, spaces)
            }
            return fragment
        }
    }

    private lateinit var name: String
    private lateinit var skin: GameSkin
    private var adapter: GameSpaceSkinAdapter? = null

    private var _viewBind: DialogGameDfuBinding? = null
    private val viewBind get() = _viewBind!!

    private val dfuViewModel: DfuViewModel by viewModels({ requireParentFragment() })
    private val toast by promptToast()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pushableSpaceSkins: ArrayList<GameSpaceSkin>
        requireArguments().let {
            name = it.getString(EXTRA_NAME)!!
            skin = it.getParcelableCompat(EXTRA_SKIN)!!
            pushableSpaceSkins = it.getParcelableArrayListCompat(EXTRA_SPACES)!!
        }
        if (pushableSpaceSkins.isNotEmpty()) {
            adapter = GameSpaceSkinAdapter(pushableSpaceSkins, skin.binSize)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogGameDfuBinding.inflate(LayoutInflater.from(context))

        viewBind.tvName.text = name

        glideShowImage(viewBind.imgView, skin.imgUrl, false)

        if (adapter == null) {
            viewBind.layoutSelect.visibility = View.GONE
        } else {
            viewBind.layoutSelect.visibility = View.VISIBLE
            viewBind.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            viewBind.recyclerView.setHasFixedSize(true)
            viewBind.recyclerView.isNestedScrollingEnabled = false
            viewBind.recyclerView.adapter = adapter
        }

        resetStateView()

        viewBind.stateView.clickTrigger {
            if (!dfuViewModel.isDfuIng()) {
                PermissionHelper.requestBle(this) { granted ->
                    if (granted) {
                        val binFlag = adapter?.getSelectBinFlag() ?: 0
                        dfuViewModel.startDfu(FcDfuManager.DfuType.GAME, Uri.parse(skin.binUrl), binFlag)
                    }
                }
            }
        }

        lifecycle.launchRepeatOnStarted {
            launch {
                dfuViewModel.flowDfuStateProgress().collect {
                    when (it.state) {
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
                    viewBind.stateView.progress = it.progress
                }
            }
            launch {
                dfuViewModel.flowDfuEvent.collect {
                    when (it) {
                        is DfuViewModel.DfuEvent.OnSuccess -> {
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
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(viewBind.root)
            .setCancelable(true)
            .create()
    }

    private fun resetStateView() {
        viewBind.stateView.isEnabled = adapter?.hasSelectBinFlag() ?: false
        viewBind.stateView.text = getString(R.string.ds_push_start, fileSizeStr(skin.binSize))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

}