package com.topstep.fitcloud.sample2.ui.device.alarm

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentAlarmListBinding
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.base.Fail
import com.topstep.fitcloud.sample2.ui.base.Loading
import com.topstep.fitcloud.sample2.ui.base.Success
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewLifecycleScope
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.settings.FcAlarm
import kotlinx.coroutines.launch

class AlarmListFragment : BaseFragment(R.layout.fragment_alarm_list) {

    private val viewBind: FragmentAlarmListBinding by viewBinding()
    private val viewModel: AlarmViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: AlarmListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    onBackPressed()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })

        (requireActivity() as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.ds_alarm)

        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBind.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        adapter = AlarmListAdapter(viewModel.helper)
        adapter.listener = object : AlarmListAdapter.Listener {

            override fun onItemModified(position: Int, alarmModified: FcAlarm) {
                viewModel.modifyAlarm(position, alarmModified)
            }

            override fun onItemClick(position: Int, alarm: FcAlarm) {
                findNavController().navigate(AlarmListFragmentDirections.toAlarmDetail(position))
            }

            override fun onItemDelete(position: Int) {
                viewModel.deleteAlarm(position)
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)
        viewBind.recyclerView.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.requestAlarms()
        }
        viewBind.loadingView.associateViews = arrayOf(viewBind.recyclerView)

        viewBind.fabAdd.setOnClickListener {
            viewLifecycleScope.launchWhenResumed {
                if ((adapter.sources?.size ?: 0) >= 5) {
                    promptToast.showInfo(R.string.ds_alarm_limit_count)
                } else {
                    findNavController().navigate(AlarmListFragmentDirections.toAlarmDetail(-1))
                }
            }
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect { state ->
                    when (state.requestAlarms) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                            viewBind.fabAdd.hide()
                        }
                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                            viewBind.fabAdd.hide()
                        }
                        is Success -> {
                            val alarms = state.requestAlarms()
                            if (alarms == null || alarms.isEmpty()) {
                                viewBind.loadingView.showError(R.string.ds_alarm_no_data)
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                            }
                            adapter.sources = alarms
                            adapter.notifyDataSetChanged()

                            viewBind.fabAdd.show()
                        }
                        else -> {}
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect { event ->
                    when (event) {
                        is AlarmEvent.RequestFail -> {
                            promptToast.showFailed(event.throwable)
                        }

                        is AlarmEvent.AlarmInserted -> {
                            adapter.notifyItemInserted(event.position)
                        }
                        is AlarmEvent.AlarmRemoved -> {
                            adapter.notifyItemRemoved(event.position)
                        }
                        is AlarmEvent.AlarmMoved -> {
                            adapter.notifyItemMoved(event.fromPosition, event.toPosition)
                        }

                        is AlarmEvent.NavigateUp -> {
                            navigateUpInNested()
                        }
                    }
                }
            }
        }
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            if (adapter.itemCount <= 0) {
                viewBind.loadingView.showError(R.string.ds_alarm_no_data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
    }

    private fun onBackPressed() {
        if (viewModel.setAlarmsAction.isSuccess()) {
            navigateUpInNested()
        } else {
            //If alarm changes not saved, wait!!!
            SetAlarmsDialogFragment().show(childFragmentManager, null)
        }
    }

    private fun navigateUpInNested() {
        parentFragment?.parentFragment?.findNavController()?.navigateUp()
    }
}