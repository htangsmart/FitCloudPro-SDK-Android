package com.topstep.fitcloud.sample2.ui.device.sport.editor

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentSportUiEditorBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.device.sport.push.SportUiHelper
import com.topstep.fitcloud.sample2.utils.viewLifecycleScope
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.model.file.FcSportUIEditor
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await

class SportUiEditorFragment : BaseFragment(R.layout.fragment_sport_ui_editor) {

    private val viewBind: FragmentSportUiEditorBinding by viewBinding()
    private val fcSDK = Injector.getDeviceManager().fcSDK
    private val helper = SportUiHelper()
    private lateinit var adapter: SportUiEditorAdapter
    private var editor: FcSportUIEditor? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewBind.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        adapter = SportUiEditorAdapter(helper)
        adapter.listener = object : SportUiEditorAdapter.Listener {
            override fun onItemDelete(position: Int, sportType: Int) {
                editor?.let {
                    it.current.removeAt(position)
                    adapter.items = ArrayList(it.current)
                }
            }
        }
        viewBind.recyclerView.adapter = adapter

        viewBind.btnAdd.setOnClickListener {
            showAddDialog()
        }

        viewBind.btnSave.setOnClickListener {
            saveEditor()
        }

        loadEditor()
    }

    private fun loadEditor() {
        viewBind.loadingView.isVisible = true
        viewLifecycleScope.launch {
            try {
                editor = fcSDK.sportUIAbility.requestSportUIEditor().await()
                editor?.let {
                    adapter.items = ArrayList(it.current)
                }
            } catch (e: Exception) {
                promptToast.showFailed("Failed to load: ${e.message}")
            } finally {
                viewBind.loadingView.isVisible = false
            }
        }
    }

    private fun showAddDialog() {
        val currentEditor = editor ?: return

        // 检查是否已达到最大数量
        if (currentEditor.current.size >= currentEditor.max) {
            promptToast.showFailed("The maximum quantity limit has been reached (${currentEditor.max})")
            return
        }

        // 获取运动类型名称列表
        val sportNames = currentEditor.all.map { type ->
            helper.getTypeName(this.requireContext(), type)
        }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select sport type")
            .setItems(sportNames) { _, which ->
                val selectedType = currentEditor.all[which]
                currentEditor.current.add(selectedType)
                adapter.items = ArrayList(currentEditor.current)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun saveEditor() {
        val currentEditor = editor ?: return
        promptProgress.showProgress("Saving")
        viewLifecycleScope.launch {
            try {
                fcSDK.sportUIAbility.setSportUIEditor(currentEditor).await()
                promptToast.showSuccess("Success")
            } catch (e: Exception) {
                promptToast.showFailed("Fail: ${e.message}")
            } finally {
                promptProgress.dismiss()
            }
        }
    }
}