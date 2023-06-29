package com.topstep.fitcloud.sample2.ui.device.dial.component

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentDialComponentEditBinding
import com.topstep.fitcloud.sample2.databinding.LayoutDialComponentStyleBinding
import com.topstep.fitcloud.sample2.model.dial.DialComponent
import com.topstep.fitcloud.sample2.model.dial.DialPushParams
import com.topstep.fitcloud.sample2.ui.base.AsyncEvent
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

class DialComponentEditFragment : BaseFragment(R.layout.fragment_dial_component_edit) {

    private val viewBind: FragmentDialComponentEditBinding by viewBinding()
    private val viewModel: DialComponentViewModel by viewModels({ requireParentFragment() })
    private val args: DialComponentEditFragmentArgs by navArgs()

    private var cacheParams: DialPushParams? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    findNavController().navigateUp()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)

        (requireActivity() as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.ds_dial_component_edit)

        cacheParams = null
        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    val params = it.getParams()
                    //If device reconnect, the params will refresh and may be changed
                    if (cacheParams != params) {//params changed
                        cacheParams = params
                        val spaces = params?.dialSpacePackets
                        val spaceCount = spaces?.size ?: 0
                        if (params == null || spaces == null || args.position !in 0 until spaceCount) {
                            findNavController().navigateUp()
                        } else {
                            val space = spaces[args.position]
                            space.components?.toMutableList()?.let { components ->
                                viewBind.componentView.init(params.shape, space.previewImgUrl, components)
                                createStyleLayouts(components)
                            }
                        }
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect {
                    if (it is AsyncEvent.OnSuccess<*> && it.property == DialComponentViewModel.State::setComponents) {
                        findNavController().navigateUp()
                    }
                }
            }
        }

        viewBind.btnSave.clickTrigger {
            viewModel.setComponents(args.position, viewBind.componentView.getComponents())
        }
    }

    private fun createStyleLayouts(components: List<DialComponent>) {
        if (viewBind.layoutContent.childCount - components.size > 0) {
            for (i in components.size until viewBind.layoutContent.childCount) {
                viewBind.layoutContent.removeViewAt(i)
            }
        }
        for (i in components.indices) {
            val addedChild = viewBind.layoutContent.getChildAt(i)
            val layout = if (addedChild != null) {
                LayoutDialComponentStyleBinding.bind(addedChild)
            } else {
                LayoutDialComponentStyleBinding.inflate(layoutInflater).apply {
                    tvTitle.text = getString(R.string.ds_dial_component) + (i + 1).toString()
                    recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    viewBind.layoutContent.addView(root)
                }
            }
            val adapter = layout.recyclerView.adapter as? DialComponentStyleAdapter ?: DialComponentStyleAdapter().apply {
                layout.recyclerView.adapter = this
            }
            adapter.items = components[i].styleUrls
            adapter.selectPosition = components[i].styleCurrent
            adapter.listener = object : DialComponentStyleAdapter.Listener {
                override fun onItemSelect(position: Int) {
                    viewBind.componentView.setComponentStyle(i, position)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

}