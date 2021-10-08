package com.github.kilnn.wristband2.sample.dial.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.github.kilnn.wristband2.sample.MyApplication
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.databinding.FragmentDialLibraryListBinding
import com.github.kilnn.wristband2.sample.dial.DialFileHelper
import com.github.kilnn.wristband2.sample.dial.State
import com.github.kilnn.wristband2.sample.dial.entity.DialInfo
import com.github.kilnn.wristband2.sample.dial.task.UnSupportLcdException
import com.github.kilnn.wristband2.sample.utils.AndPermissionHelper
import com.github.kilnn.wristband2.sample.utils.DisplayUtil
import com.github.kilnn.wristband2.sample.utils.Utils
import com.github.kilnn.wristband2.sample.widget.GridSpacingItemDecoration
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.properties.Delegates

class DialLibraryListFragment : Fragment(), DialUpgradeFragment.Listener {

    companion object {
        private const val TAG = "DialLibraryListFragment"
        private const val EXTRA_IS_LOCAL = "is_local"

        fun newInstance(local: Boolean): DialLibraryListFragment {
            val fragment = DialLibraryListFragment()
            fragment.arguments = Bundle().apply { putBoolean(EXTRA_IS_LOCAL, local) }
            return fragment
        }
    }

    private var _viewBind: FragmentDialLibraryListBinding? = null
    private val viewBind get() = _viewBind!!

    private var isLocal: Boolean by Delegates.notNull()//展示本地列表，还是服务器列表
    private val viewModel: DialLibraryViewModel by activityViewModels()
    private lateinit var adapter: DialListAdapter

    private val appDatabase = MyApplication.getSyncDataDb()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isLocal = arguments?.getBoolean(EXTRA_IS_LOCAL) ?: false
        adapter = DialListAdapter(!isLocal)//显示服务器列表的时候，第一项显示自定义表盘的入口
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _viewBind = FragmentDialLibraryListBinding.inflate(inflater, container, false)
        return viewBind.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        viewBind.recyclerView.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtil.dip2px(requireContext(), 12F), true))
        adapter.listener = object : DialListAdapter.Listener {
            override fun onDialClick(dial: DialInfo) {
                val state = viewModel.liveDialList(isLocal).value
                //只有下面这两个时机，界面上才会展示列表，所有不需要对所有状态判断。
                if (state is State.Failed<*> && state.error is BleDisconnectedException) {
                    //设备断开连接提示
                    Toast.makeText(requireContext(), R.string.action_disconnect, Toast.LENGTH_SHORT).show()
                } else if (state is State.Success<*> && state.result != null) {
                    DialUpgradeFragment.newInstance(dial, state.result.param)
                        .show(childFragmentManager, null)
                } else {
                    Timber.tag(TAG).d("Can not upgrade ${state.toString()}")
                }
            }

            override fun onDialDelete(dial: DialInfo) {
                adapter.delete(dial)
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        //删除数据库和下载的文件
                        appDatabase.dialInfoDao().delete(dial)
                        DialFileHelper.getNormalFileByUrl(requireContext(), dial.imgUrl)?.delete()
                        DialFileHelper.getNormalFileByUrl(requireContext(), dial.deviceImgUrl)?.delete()
                        DialFileHelper.getNormalFileByUrl(requireContext(), dial.binUrl)?.delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        viewBind.recyclerView.adapter = adapter

        viewBind.lceView.setLoadingListener {
            viewModel.refreshDialList(isLocal)
        }

        if (isLocal) {
            viewBind.imgEdit.setOnClickListener {
                if (adapter.isEditMode) {
                    adapter.isEditMode = false
                    viewBind.imgEdit.setImageResource(R.drawable.ic_dial_edit)
                    viewBind.tvEdit.setText(R.string.action_edit)
                } else {
                    if (adapter.itemCount > 0) {
                        adapter.isEditMode = true
                        viewBind.imgEdit.setImageResource(R.drawable.ic_dial_completed)
                        viewBind.tvEdit.setText(R.string.action_completed)
                    }
                }
            }
        } else {
            viewBind.layoutEdit.visibility = View.GONE
        }

        viewModel.liveDialList(isLocal).observe(viewLifecycleOwner) { state ->
            when (state) {
                null, is State.Loading<*> -> {
                    viewBind.lceView.lceShowLoading()
                    adapter.sources = null
                    adapter.notifyDataSetChanged()
                }
                is State.Failed<*> -> {
                    if (state.error is BleDisconnectedException) {
                        Toast.makeText(requireContext(), R.string.action_disconnect, Toast.LENGTH_SHORT).show()
                        //当设备断开时，如果之前有list数据，那么就保持不变
                        if (adapter.sources.isNullOrEmpty()) {
                            viewBind.lceView.lceShowInfo(R.string.action_disconnect)
                        }
                    } else if (state.error is UnSupportLcdException) {
                        //lcd 不支持，图片等显示会变形，就不展示了，提示更新APP
                        viewBind.lceView.lceShowError(R.string.ds_dial_error_none_shape)
                    } else {
                        Toast.makeText(requireContext(), Utils.parserError(requireContext(), state.error), Toast.LENGTH_SHORT).show()
                        viewBind.lceView.lceShowError(R.string.tip_load_error)
                    }
                }
                is State.Success<*> -> state.result?.let {
                    adapter.shape = it.param.shape
                    adapter.sources = it.list
                    adapter.notifyDataSetChanged()
                    if (adapter.itemCount <= 0) {
                        viewBind.lceView.lceShowInfo(R.string.tip_current_no_data)
                    } else {
                        viewBind.lceView.lceShowContent()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        //退出编辑模式
        if (adapter.isEditMode) {
            adapter.isEditMode = false
            viewBind.imgEdit.setImageResource(R.drawable.ic_dial_edit)
            viewBind.tvEdit.setText(R.string.action_edit)
        }
    }

    override fun scheduleUpgrade(runnable: Runnable) {
        AndPermissionHelper.fileAndLocationRequest(this) {
            if (Utils.checkLocationEnabled(requireActivity(), R.string.feature_location_request_for_ble_scan)) {
                runnable.run()
            }
        }
    }
}