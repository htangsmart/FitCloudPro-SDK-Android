package com.github.kilnn.wristband2.sample.dial

import android.app.Dialog
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.databinding.DialogBinSelectBinding
import com.github.kilnn.wristband2.sample.dial.task.DialBinParam
import com.github.kilnn.wristband2.sample.dial.task.DialParam
import com.github.kilnn.wristband2.sample.utils.Utils
import com.htsmart.wristband2.bean.DialSubBinInfo
import com.htsmart.wristband2.dial.DialDrawer
import com.htsmart.wristband2.dial.DialView

class DialBinSelectFragment : AppCompatDialogFragment() {
    companion object {
        init {
            DialView.setEngine(MyDialViewEngine.INSTANCE)
        }

        private const val EXTRA_DIAL_PARAM = "dial_param"
        private const val EXTRA_BIN_SIZE = "bin_size"

        /**
         * @param param 表盘参数
         * @param binSize 将要升级的bin文件大小
         */
        fun newInstance(param: DialParam, binSize: Long): DialBinSelectFragment {
            val fragment = DialBinSelectFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(EXTRA_DIAL_PARAM, param)
                putLong(EXTRA_BIN_SIZE, binSize)
            }
            return fragment
        }
    }

    interface Listener {
        fun onDialBinSelect(binFlag: Byte)
    }

    private var listener: Listener? = null
    private lateinit var param: DialParam
    private var binSize = 0L
    private lateinit var adapter: InnerAdapter

    private var _viewBind: DialogBinSelectBinding? = null
    private val viewBind get() = _viewBind!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment != null && parentFragment is Listener) {
            listener = parentFragment as Listener?
        } else if (context is Listener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        param = requireArguments().getParcelable(EXTRA_DIAL_PARAM)!!
        binSize = requireArguments().getLong(EXTRA_BIN_SIZE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogBinSelectBinding.inflate(LayoutInflater.from(context))

        viewBind.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = InnerAdapter(param, binSize)
        viewBind.recyclerView.adapter = adapter

        //确认按钮是否可用，只需设置一次即可。因为会自动查找第一个可用的选择。如果被设置为false，那么表盘所有表盘都不能被选择，状态也不会再改变了。
        viewBind.btnSure.isEnabled = adapter.hasSelectBinFlag()
        viewBind.btnSure.text = getString(R.string.action_sure) + "（" + Utils.fileSizeStr(binSize) + "）"
        viewBind.btnSure.setOnClickListener {
            listener?.onDialBinSelect(adapter.getSelectBinFlag())
            dismissAllowingStateLoss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(viewBind.root)
            .setCancelable(true)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

    private class InnerViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dialView: DialView = itemView.findViewById(R.id.dial_view)
        val tvDialSpace: TextView = itemView.findViewById(R.id.tv_dial_space)
    }

    private class InnerAdapter(param: DialParam, private val binSize: Long) : RecyclerView.Adapter<InnerViewHolder>() {
        private val dialBinParams = param.filterSelectableDialBinParams()//获取可被选择的表盘
        private val shape = DialDrawer.Shape.createFromLcd(param.lcd)!!.adjustRecommendCorners()//一定支持这个shape，要不然这个dialog不会显示
        private var selectPosition = -1//默认不可选

        private val paintSaturationMin: Paint by lazy {
            val paint = Paint()
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0.0f)
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            paint
        }

        private val paintSaturationMax: Paint by lazy {
            val paint = Paint()
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(1.0f)
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            paint
        }

        init {
            for (i in dialBinParams.indices) {
                //默认选择第一个表盘空间大小binSize的位置，如果没有，那么就是默认值-1
                if (isDialSelectable(dialBinParams[i])) {
                    selectPosition = i
                    break
                }
            }
        }

        private fun isDialSelectable(dial: DialBinParam): Boolean {
            return dial.dialSpace * Utils.KB > binSize
        }

        /**
         * 选择位置是否可用
         */
        fun hasSelectBinFlag(): Boolean {
            return selectPosition != -1
        }

        fun getSelectBinFlag(): Byte {
            return dialBinParams[selectPosition].binFlag
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
            return InnerViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_dial_bin_select, parent, false)
            )
        }

        override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
            val dialBinParam = dialBinParams[position]
            val context = holder.dialView.context
            //设置shape
            holder.dialView.shape = shape

            if (dialBinParam.dialType == DialSubBinInfo.TYPE_NORMAL) {
                //普通的表盘，那么就显示背景图即可
                if (dialBinParam.imgUrl.isNullOrEmpty()) {
                    holder.dialView.clearBackgroundBitmap()
                } else {
                    holder.dialView.setBackgroundSource(Uri.parse(dialBinParam.imgUrl))
                }
                holder.dialView.clearStyleBitmap()
            } else {
                holder.dialView.setBackgroundSource(Utils.getUriFromDrawableResId(context, R.drawable.dial_default_bg))
                when (dialBinParam.dialType) {
                    //旧协议的这几个本地样式图片，都是基于800px设计的
                    DialSubBinInfo.TYPE_CUSTOM_STYLE_WHITE -> holder.dialView.setStyleSource(Utils.getUriFromDrawableResId(context, R.drawable.dial_style1), DialDrawer.STYLE_BASE_ON_WIDTH)
                    DialSubBinInfo.TYPE_CUSTOM_STYLE_BLACK -> holder.dialView.setStyleSource(Utils.getUriFromDrawableResId(context, R.drawable.dial_style2), DialDrawer.STYLE_BASE_ON_WIDTH)
                    DialSubBinInfo.TYPE_CUSTOM_STYLE_YELLOW -> holder.dialView.setStyleSource(Utils.getUriFromDrawableResId(context, R.drawable.dial_style3), DialDrawer.STYLE_BASE_ON_WIDTH)
                    DialSubBinInfo.TYPE_CUSTOM_STYLE_GREEN -> holder.dialView.setStyleSource(Utils.getUriFromDrawableResId(context, R.drawable.dial_style4), DialDrawer.STYLE_BASE_ON_WIDTH)
                    DialSubBinInfo.TYPE_CUSTOM_STYLE_GRAY -> holder.dialView.setStyleSource(Utils.getUriFromDrawableResId(context, R.drawable.dial_style5), DialDrawer.STYLE_BASE_ON_WIDTH)
                    else -> holder.dialView.clearStyleBitmap()
                }
                holder.dialView.stylePosition = DialDrawer.Position.TOP
            }

            val isDialSelectable = isDialSelectable(dialBinParam)

            holder.dialView.isChecked = position == selectPosition
            if (isDialSelectable) {
                holder.dialView.setLayerType(View.LAYER_TYPE_HARDWARE, paintSaturationMax)
                holder.dialView.setOnClickListener { //默认选择第一个
                    selectPosition = holder.adapterPosition
                    notifyDataSetChanged()
                }
            } else {
                holder.dialView.setLayerType(View.LAYER_TYPE_HARDWARE, paintSaturationMin)
                holder.dialView.setOnClickListener(null)
            }

            holder.tvDialSpace.isEnabled = isDialSelectable
            holder.tvDialSpace.text = Utils.fileSizeStr(dialBinParam.dialSpace * Utils.KB)
        }

        override fun getItemCount(): Int {
            return dialBinParams.size
        }
    }
}