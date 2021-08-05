package com.github.kilnn.wristband2.sample.dial.custom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.databinding.ActivityDialCustomBinding
import com.github.kilnn.wristband2.sample.dial.*
import com.github.kilnn.wristband2.sample.dial.custom.*
import com.github.kilnn.wristband2.sample.dial.task.UnSupportLcdException
import com.github.kilnn.wristband2.sample.utils.AndPermissionHelper
import com.github.kilnn.wristband2.sample.utils.Utils
import com.htsmart.wristband2.dial.DialDrawer
import com.htsmart.wristband2.dial.DialView
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class DialCustomActivity : BaseSelectPictureActivity(), DialBinSelectFragment.Listener {

    companion object {
        init {
            DialView.setEngine(MyDialViewEngine.INSTANCE)
        }

        private const val TAG = "DialCustomActivity"
    }

    private val viewBind: ActivityDialCustomBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityDialCustomBinding.inflate(layoutInflater)
    }

    private lateinit var bgGridView: RecyclerView
    private lateinit var styleGridView: RecyclerView
    private lateinit var positionGridView: RecyclerView

    private lateinit var bgAdapter: DialGridItemAdapter
    private lateinit var styleAdapter: DialGridItemAdapter
    private lateinit var positionAdapter: DialGridItemAdapter

    private val viewModel: DialCustomViewModel by viewModels()

    private var groupCustomResult: GroupCustomResult? = null
    private var bgRawSources: MutableList<Uri>? = null //bgAdapter的原始数据
    private val positionRawSources: List<DialDrawer.Position> = listOf(
        DialDrawer.Position.BOTTOM,
        DialDrawer.Position.TOP,
        DialDrawer.Position.LEFT,
        DialDrawer.Position.RIGHT,
    )//positionAdapter的原始数据

    private var selectBinSize = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBind.root)

        supportActionBar?.title = getString(R.string.ds_dial_custom)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewBind.tabLayout.setupWithViewPager(viewBind.viewPager)
        viewBind.viewPager.offscreenPageLimit = 3

        bgGridView = RecyclerView(this)
        bgAdapter = DialGridItemAdapter.setupRecyclerView(bgGridView, true).also { it.listener = bgListener }

        styleGridView = RecyclerView(this)
        styleAdapter = DialGridItemAdapter.setupRecyclerView(styleGridView, false).also { it.listener = otherListener }

        positionGridView = RecyclerView(this)
        positionAdapter = DialGridItemAdapter.setupRecyclerView(positionGridView, false).also { it.listener = otherListener }

        viewBind.lceView.setLoadingListener { viewModel.refreshDialCustom() }

        viewBind.btnSet.setOnClickListener {
            val state = viewModel.liveDialCustom().value
            //只有下面这两个时机，界面上才会展示列表，所有不需要对所有状态判断。
            if (state is State.Failed && state.error is BleDisconnectedException) {
                //设备断开连接提示
                Toast.makeText(this, R.string.action_disconnect, Toast.LENGTH_SHORT).show()
            } else if (state is State.Success && state.result != null) {
                //检查权限，弹出升级对话框
                AndPermissionHelper.fileAndLocationRequest(this) {
                    if (Utils.checkLocationEnabled(this, R.string.feature_location_request_for_ble_scan)) {
                        if (state.result.param.isSelectableDialBinParams()) {
                            //有多表盘，先选择升级位置
                            DialBinSelectFragment.newInstance(state.result.param, selectBinSize).show(supportFragmentManager, null)
                        } else {
                            //没有多表盘信息，直接升级
                            showDialCustomDialog(0.toByte())
                        }
                    }
                }
            } else {
                Timber.tag(TAG).d("Can not upgrade ${state?.toString()}")
            }
        }

        viewModel.liveDialCustom().observe(this) { state ->
            when (state) {
                null, is State.Loading -> {
                    groupCustomResult = null
                    viewBind.lceView.lceShowLoading()
                }
                is State.Failed -> {
                    when (state.error) {
                        is BleDisconnectedException -> {
                            Toast.makeText(this, R.string.action_disconnect, Toast.LENGTH_SHORT).show()
                            //没有数据，显示未连接
                            if (groupCustomResult == null) {
                                viewBind.lceView.lceShowInfo(R.string.action_disconnect)
                            }
                        }
                        is UnSupportLcdException -> {
                            //lcd 不支持，图片等显示会变形，就不展示了，提示更新APP
                            viewBind.lceView.lceShowError(R.string.ds_dial_error_none_shape)
                        }
                        is UnSupportCustomException -> {
                            //没有可用的组件或样式
                            viewBind.lceView.lceShowError(R.string.ds_dial_error_none_style)
                        }
                        else -> {
                            Toast.makeText(this, Utils.parserError(this, state.error), Toast.LENGTH_SHORT).show()
                            viewBind.lceView.lceShowError(R.string.tip_load_error)
                        }
                    }
                }
                is State.Success -> state.result?.let {
                    //数据赋值
                    groupCustomResult = it
                    val shape = DialDrawer.Shape.createFromLcd(it.param.lcd)!!.adjustRecommendCorners()
                    viewBind.dialView.shape = shape
                    bgAdapter.shape = shape
                    styleAdapter.shape = shape
                    positionAdapter.shape = shape
                    bgRawSources = DialFileHelper.loadDialCustomBgFiles(this, shape)
                    viewBind.viewPager.adapter = DialCustomPagerAdapter(bgGridView, styleGridView, positionGridView, it.param.isGUI)//GUI协议时，不显示styles选择
                    viewBind.lceView.lceShowContent()
                    adjustData()
                }
            }
        }
    }

    private fun adjustData() {
        val dialCustomCompat = this.groupCustomResult?.custom ?: return

        val selectedBackground = bgRawSources?.getOrNull(bgAdapter.selectPosition) ?: dialCustomCompat.defaultBackgroundUri
        val selectedStyle = dialCustomCompat.styles.getOrNull(styleAdapter.selectPosition) ?: dialCustomCompat.styles[0]
        val selectedPosition = positionRawSources.getOrNull(positionAdapter.selectPosition) ?: positionRawSources[0]

        //背景数据
        val bgItems = bgRawSources?.map { DialGridItem(it, selectedStyle, selectedPosition) } as MutableList?
        bgAdapter.sources = bgItems
        bgAdapter.notifyDataSetChanged()

        //样式数据
        val styleItems = dialCustomCompat.styles.map { DialGridItem(selectedBackground, it, selectedPosition) } as MutableList
        styleAdapter.sources = styleItems
        styleAdapter.notifyDataSetChanged()

        //位置数据
        val positionItems = positionRawSources.map { DialGridItem(selectedBackground, selectedStyle, it) } as MutableList
        positionAdapter.sources = positionItems
        positionAdapter.notifyDataSetChanged()

        //DialView设置
        viewBind.dialView.setBackgroundSource(selectedBackground)
        viewBind.dialView.setStyleSource(selectedStyle.styleUri, selectedStyle.styleBaseOnWidth)
        viewBind.dialView.stylePosition = selectedPosition

        selectBinSize = selectedStyle.binSize
        viewBind.btnSet.text = getString(R.string.ds_dial_set_dial) + "（" + Utils.fileSizeStr(selectBinSize) + "）"
    }

    private val bgListener: DialGridItemAdapter.Listener = object : DialGridItemAdapter.Listener() {

        override fun onItemSelect(item: DialGridItem, position: Int) {
            adjustData()
        }

        override fun onItemDelete(item: DialGridItem, position: Int) {
            if (bgRawSources?.remove(item.backgroundUri) == true) {
                GlobalScope.launch(Dispatchers.IO) {
                    //删除文件,背景图里的文件都是路径转换的uri，如file:\\等形式的
                    item.backgroundUri.path?.let {
                        File(it).delete()
                    }
                }
            }
            adjustData()
        }

        /**
         * 点击去添加背景图
         */
        override fun onAddClick() {
            val count = bgRawSources?.size ?: 0
            if (count >= 5) {
                Toast.makeText(this@DialCustomActivity, R.string.ds_dial_img_over_tips, Toast.LENGTH_SHORT).show()
                return
            }
            selectPicture()
        }
    }

    private val otherListener: DialGridItemAdapter.Listener = object : DialGridItemAdapter.Listener() {
        override fun onItemSelect(item: DialGridItem, position: Int) {
            adjustData()
        }
    }

    override fun onDialBinSelect(binFlag: Byte) {
        showDialCustomDialog(binFlag)
    }

    private fun showDialCustomDialog(binFlag: Byte) {
        val groupCustomResult = this.groupCustomResult ?: return
        val dialCustomCompat = groupCustomResult.custom

        val selectedBackground = bgRawSources?.getOrNull(bgAdapter.selectPosition) ?: dialCustomCompat.defaultBackgroundUri
        val selectedStyle = dialCustomCompat.styles.getOrNull(styleAdapter.selectPosition) ?: dialCustomCompat.styles[0]

        val param = DialCustomFragment.Param(
            groupCustomResult.param.isGUI,
            selectedStyle.binUrl,
            selectedBackground,
            selectedStyle.styleUri,
            viewBind.dialView.shape,
            viewBind.dialView.backgroundScaleType,
            viewBind.dialView.stylePosition,
            selectedStyle.styleBaseOnWidth,
            binFlag
        )
        DialCustomFragment.newInstance(param).show(supportFragmentManager, null)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //背景Grid处于编辑模式，当触摸时在外部时，退出编辑模式
        if (ev.action == MotionEvent.ACTION_DOWN && bgAdapter.isEditMode) {
            val viewLocation = IntArray(2)
            bgGridView.getLocationInWindow(viewLocation)
            val left = viewLocation[0]
            val right = left + bgGridView.width
            val top = viewLocation[1]
            val bottom = top + bgGridView.height
            if (ev.x < left || ev.x > right || ev.y < top || ev.y > bottom) {
                //点击事件不在bgGridView内部，退出编辑模式
                bgAdapter.isEditMode = false
                bgAdapter.notifyDataSetChanged()
                return true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun getPictureCropFile(): File? {
        return DialFileHelper.newDialCustomBgFile(this, viewBind.dialView.shape)
    }

    override fun getPictureCropIntent(): Intent {
        return DialFileHelper.newDialCustomBgCropIntent(viewBind.dialView.shape)
    }

    override fun onPictureSelect(uri: Uri) {
        val list = bgRawSources ?: ArrayList<Uri>().also { bgRawSources = it }
        list.add(uri)
        adjustData()
    }
}