package com.github.kilnn.wristband2.sample.sportpush

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.github.kilnn.wristband2.sample.BaseActivity
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.databinding.ActivityDialComponentBinding
import com.github.kilnn.wristband2.sample.dial.State
import com.github.kilnn.wristband2.sample.sportpush.entity.SportPushWithIcon
import com.github.kilnn.wristband2.sample.utils.DisplayUtil
import com.github.kilnn.wristband2.sample.utils.Utils
import com.github.kilnn.wristband2.sample.widget.GridSpacingItemDecoration
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException

class SportPushActivity : BaseActivity() {

    private val viewModel: SportPushViewModel by viewModels()
    private lateinit var adapter: SportPushAdapter

    private val viewBind: ActivityDialComponentBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityDialComponentBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBind.root)

        supportActionBar?.title = "运动推送"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewBind.recyclerView.layoutManager = GridLayoutManager(this, 3)
        viewBind.recyclerView.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtil.dip2px(this, 12f), true))
        adapter = SportPushAdapter()
        adapter.listener = object : SportPushAdapter.Listener {
            override fun onItemClick(position: Int, param: SportPushWithIcon) {
                if (param.pushEnabled) {
                    val binItems = viewModel.liveSportPushParam().value?.result?.listNotExist ?: return
                    if (binItems.isEmpty()) {
                        toast("无可推送的运动")
                    } else {
                        SportPushDialogFragment.newInstance(param.binFlag, binItems).show(supportFragmentManager, null)
                    }
                } else {
                    toast("此位置不能被推送")
                }
            }
        }
        viewBind.recyclerView.adapter = adapter

        viewBind.lceView.setLoadingListener { viewModel.refreshSportPushParam() }

        viewModel.liveSportPushParam().observe(this) { state ->
            when (state) {
                null, is State.Loading -> {
                    viewBind.lceView.lceShowLoading()
                    adapter.sources = null
                    adapter.notifyDataSetChanged()
                }
                is State.Failed -> {
                    if (state.error is BleDisconnectedException) {
                        Toast.makeText(this, R.string.action_disconnect, Toast.LENGTH_SHORT).show()
                        //当设备断开时，如果之前有list数据，那么就保持不变
                        if (adapter.sources.isNullOrEmpty()) {
                            viewBind.lceView.lceShowInfo(R.string.action_disconnect)
                        }
                    } else {
                        Toast.makeText(this, Utils.parserError(this, state.error), Toast.LENGTH_SHORT).show()
                        viewBind.lceView.lceShowError(R.string.tip_load_error)
                    }
                }
                is State.Success -> state.result?.let {
                    adapter.sources = it.listExist
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

}