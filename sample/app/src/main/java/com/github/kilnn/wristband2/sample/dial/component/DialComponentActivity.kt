package com.github.kilnn.wristband2.sample.dial.component

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.github.kilnn.wristband2.sample.BaseActivity
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.databinding.ActivityDialComponentBinding
import com.github.kilnn.wristband2.sample.dial.DialParamViewModule
import com.github.kilnn.wristband2.sample.dial.State
import com.github.kilnn.wristband2.sample.dial.task.DialBinParam
import com.github.kilnn.wristband2.sample.dial.task.UnSupportLcdException
import com.github.kilnn.wristband2.sample.utils.DisplayUtil
import com.github.kilnn.wristband2.sample.utils.Utils
import com.github.kilnn.wristband2.sample.widget.GridSpacingItemDecoration
import com.htsmart.wristband2.WristbandApplication
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext

class DialComponentActivity : BaseActivity() {

    private val manager = WristbandApplication.getWristbandManager()
    private val viewModel: DialParamViewModule by viewModels()
    private lateinit var adapter: DialComponentAdapter

    private val viewBind: ActivityDialComponentBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityDialComponentBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBind.root)

        supportActionBar?.title = getString(R.string.ds_dial_component_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewBind.recyclerView.layoutManager = GridLayoutManager(this, 3)
        viewBind.recyclerView.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtil.dip2px(this, 12f), true))
        adapter = DialComponentAdapter()
        adapter.listener = object : DialComponentAdapter.Listener {
            override fun onItemClick(position: Int, param: DialBinParam) {
                toast(R.string.tip_please_wait)
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            manager.setDialComponents(position, null).await()
                        }
                        adapter.selectPosition = position
                        adapter.notifyDataSetChanged()
                    } catch (e: Exception) {
                        val error = Utils.parserErrorBLE(this@DialComponentActivity, e)
                        toast(error)
                    }
                }
            }

            override fun onEditClick(position: Int, param: DialBinParam) {
                val intent = Intent(this@DialComponentActivity, DialComponentEditActivity::class.java)
                intent.putExtra(DialComponentEditActivity.EXTRA_POSITION, position)
                intent.putExtra(DialComponentEditActivity.EXTRA_DIAL_BIN_PARAM, param)
                intent.putExtra(DialComponentEditActivity.EXTRA_LCD, adapter.lcd)
                startActivityForResult(intent, 1001)
            }
        }
        viewBind.recyclerView.adapter = adapter

        viewBind.lceView.setLoadingListener { viewModel.refreshDialParam() }

        viewModel.liveDialParam().observe(this) { state ->
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
                    } else if (state.error is UnSupportLcdException) {
                        //lcd 不支持，图片等显示会变形，就不展示了，提示更新APP
                        viewBind.lceView.lceShowError(R.string.ds_dial_error_none_shape)
                    } else {
                        Toast.makeText(this, Utils.parserError(this, state.error), Toast.LENGTH_SHORT).show()
                        viewBind.lceView.lceShowError(R.string.tip_load_error)
                    }
                }
                is State.Success -> state.result?.let {
                    adapter.lcd = it.lcd
                    adapter.sources = it.dialBinParams?.toMutableList()
                    adapter.selectPosition = it.currentDialPosition
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            val position = data.getIntExtra(DialComponentEditActivity.EXTRA_POSITION, 0)
            val dialBinParam: DialBinParam? = data.getParcelableExtra(DialComponentEditActivity.EXTRA_DIAL_BIN_PARAM)
            dialBinParam?.let {
                adapter.sources?.runCatching {
                    this[position] = dialBinParam
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

}