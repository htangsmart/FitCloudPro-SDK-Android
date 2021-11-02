package com.github.kilnn.wristband2.sample.sportpush

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.databinding.DialogFragmentSportPushBinding
import com.github.kilnn.wristband2.sample.dfu.DfuDialogFragment
import com.github.kilnn.wristband2.sample.sportpush.entity.SportBinItem
import com.github.kilnn.wristband2.sample.utils.DisplayUtil
import com.github.kilnn.wristband2.sample.widget.GridSpacingItemDecoration
import com.htsmart.wristband2.dfu.DfuCallback
import com.htsmart.wristband2.dfu.DfuManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SportPushDialogFragment : AppCompatDialogFragment() {
    companion object {
        private const val EXTRA_BIN_FLAG = "bin_flag"
        private const val EXTRA_BIN_ITEMS = "bin_items"

        fun newInstance(binFlag: Byte, items: ArrayList<SportBinItem>): SportPushDialogFragment {
            val fragment = SportPushDialogFragment()
            fragment.arguments = Bundle().apply {
                putByte(EXTRA_BIN_FLAG, binFlag)
                putParcelableArrayList(EXTRA_BIN_ITEMS, items)
            }
            return fragment
        }
    }

    private var binFlag: Byte = 0
    private lateinit var binItems: ArrayList<SportBinItem>
    private lateinit var adapter: InnerAdapter

    private var _viewBind: DialogFragmentSportPushBinding? = null
    private val viewBind get() = _viewBind!!

    private lateinit var dfuManager: DfuManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binFlag = requireArguments().getByte(EXTRA_BIN_FLAG)
        binItems = requireArguments().getParcelableArrayList(EXTRA_BIN_ITEMS)!!

        dfuManager = DfuManager(context)
        dfuManager.setDfuCallback(dfuCallback)
        dfuManager.init()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogFragmentSportPushBinding.inflate(LayoutInflater.from(context))

        viewBind.recyclerView.layoutManager = GridLayoutManager(context, 4)
        viewBind.recyclerView.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtil.dip2px(context, 12f), true))

        adapter = InnerAdapter(binItems)
        viewBind.recyclerView.adapter = adapter

        viewBind.btnSure.setOnClickListener {
            val select = adapter.getSelect()
            //升级开始
            isCancelable = false
            viewBind.btnSure.visibility = View.INVISIBLE
            viewBind.progress.visibility = View.VISIBLE
            viewBind.progress.progress = 0
            dfuManager.upgradeSportPush(select.binUrl, binFlag)
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

    override fun onDestroy() {
        super.onDestroy()
        //释放dfuManager
        dfuManager.release()
    }

    private val dfuCallback: DfuCallback = object : DfuCallback {
        override fun onError(errorType: Int, errorCode: Int) {
            DfuDialogFragment.toastError(context, errorType, errorCode)
            //TODO 如果当升级失败时，需要立即回连，那么你要在这里调用连接
            // manager.connect()
            isCancelable = true
            viewBind.btnSure.visibility = View.VISIBLE
            viewBind.progress.visibility = View.GONE
        }

        override fun onStateChanged(state: Int, cancelable: Boolean) {
            if (state != DfuManager.STATE_NONE) {
                viewBind.progress.isIndeterminate = state != DfuManager.STATE_DFU_ING
            }
        }

        override fun onProgressChanged(progress: Int) {
            viewBind.progress.progress = progress
        }

        override fun onSuccess() {
            lifecycleScope.launch {
                isCancelable = true
                viewBind.progress.progress = 100
                toast("升级成功")
                //TODO 如果当升级成功时，需要立即回连，那么你要在这里调用连接
                // manager.connect()

                delay(3000)

                dismissAllowingStateLoss()
            }
        }
    }

    private class InnerViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val viewSelect: View = itemView.findViewById(R.id.view_select)
    }

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun toast(@StringRes resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    private class InnerAdapter(val binItems: ArrayList<SportBinItem>) : RecyclerView.Adapter<InnerViewHolder>() {

        private var selectPosition = 0//默认选第一个运动

        fun getSelect(): SportBinItem {
            return binItems[selectPosition]
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
            return InnerViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_sport_push_select, parent, false)
            )
        }

        override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
            val requestOptions = RequestOptions.placeholderOf(R.drawable.ic_dial_load_failed)
            Glide.with(holder.itemView.context)
                .load(binItems[position].iconUrl)
                .apply(requestOptions)
                .into(holder.imageView)

            holder.imageView.setOnClickListener {
                selectPosition = holder.adapterPosition
                notifyDataSetChanged()
            }

            if (position == selectPosition) {
                holder.viewSelect.visibility = View.VISIBLE
            } else {
                holder.viewSelect.visibility = View.INVISIBLE
            }
        }

        override fun getItemCount(): Int {
            return binItems.size
        }
    }

}