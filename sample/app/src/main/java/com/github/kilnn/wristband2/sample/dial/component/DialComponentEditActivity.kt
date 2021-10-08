package com.github.kilnn.wristband2.sample.dial.component

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kilnn.wristband2.sample.BaseActivity
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.databinding.ActivityDialComponentEditBinding
import com.github.kilnn.wristband2.sample.databinding.LayoutDialComponentSelectBinding
import com.github.kilnn.wristband2.sample.dial.task.DialBinParam
import com.github.kilnn.wristband2.sample.dial.task.DialComponentParam
import com.github.kilnn.wristband2.sample.utils.Utils
import com.github.kilnn.wristband2.sample.widget.CustomDividerItemDecoration
import com.htsmart.wristband2.WristbandApplication
import com.htsmart.wristband2.dial.DialDrawer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class DialComponentEditActivity : BaseActivity() {

    companion object {
        const val EXTRA_POSITION = "position"
        const val EXTRA_DIAL_BIN_PARAM = "dial_bin_param"
        const val EXTRA_SHAPE = "shape"
    }

    private var position by Delegates.notNull<Int>()
    private lateinit var dialBinParam: DialBinParam
    private lateinit var shape: DialDrawer.Shape
    private lateinit var components: List<DialComponentParam>

    private val manager = WristbandApplication.getWristbandManager()

    private val viewBind: ActivityDialComponentEditBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityDialComponentEditBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBind.root)

        supportActionBar?.title = getString(R.string.ds_dial_component_edit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //认为传进来的数据一定正确，否则不会进入这个页面
        position = intent.getIntExtra(EXTRA_POSITION, 0)
        dialBinParam = intent.getParcelableExtra(EXTRA_DIAL_BIN_PARAM)!!
        shape = intent.getParcelableExtra(EXTRA_SHAPE)!!
        components = dialBinParam.components!!

        createSelectViews()
        viewBind.componentView.init(shape, dialBinParam.previewImgUrl, components)
    }

    private fun createSelectViews() {
        for (i in components.indices) {
            val layout = LayoutDialComponentSelectBinding.inflate(layoutInflater)
            layout.tvTitle.text = getString(R.string.ds_dial_component, i + 1)
            layout.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val decoration = CustomDividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)
            ResourcesCompat.getDrawable(resources, R.drawable.shape_dial_component_item_divider, theme)?.let {
                decoration.setDrawable(it)
            }
            layout.recyclerView.addItemDecoration(decoration)

            val adapter = DialComponentSelectAdapter()
            adapter.sources = components[i].styleUrls
            adapter.selectPosition = components[i].styleCurrent
            adapter.listener = object : DialComponentSelectAdapter.Listener {
                override fun onItemSelect(position: Int) {
                    viewBind.componentView.setComponentStyle(i, position)
                }
            }
            layout.recyclerView.adapter = adapter
            viewBind.layoutContent.addView(layout.root)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_id_done) {
            toast(R.string.tip_please_wait)
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val select = ByteArray(components.size)
                        for (i in components.indices) {
                            select[i] = components[i].styleCurrent.toByte()
                        }
                        manager.setDialComponents(position, select).await()
                    }
                    toast(R.string.tip_save_success)
                    delay(1000)
                    val intent = Intent()
                    intent.putExtra(EXTRA_POSITION, position)
                    //把更改后的信息也传回去，替换原来的list数据
                    intent.putExtra(EXTRA_DIAL_BIN_PARAM, dialBinParam)
                    setResult(RESULT_OK, intent)
                    finish()
                } catch (e: Exception) {
                    val error = Utils.parserErrorBLE(this@DialComponentEditActivity, e)
                    toast(error)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}