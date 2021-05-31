package com.github.kilnn.wristband2.sample.dial.library

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.dial.DialCustomView
import com.github.kilnn.wristband2.sample.dial.DialFileHelper
import com.github.kilnn.wristband2.sample.dial.DialInfoView
import com.github.kilnn.wristband2.sample.dial.adjustRecommendCorners
import com.github.kilnn.wristband2.sample.dial.custom.DialCustomActivity
import com.github.kilnn.wristband2.sample.dial.entity.DialInfo
import com.htsmart.wristband2.dial.DialDrawer


class DialListAdapter(
    /**
     * 是否显示自定义View
     */
    private val isShowCustomView: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_CUSTOM = 0
        private const val TYPE_INFO = 1
    }

    interface Listener {
        fun onDialClick(dial: DialInfo)
        fun onDialDelete(dial: DialInfo)
    }

    private class DialCustomViewHolder(itemView: DialCustomView) : RecyclerView.ViewHolder(itemView) {
        val view: DialCustomView = itemView
    }

    private class DialInfoViewHolder constructor(itemView: DialInfoView) : RecyclerView.ViewHolder(itemView) {
        val view: DialInfoView = itemView
    }

    var lcd = 0
        set(value) {
            if (field != value && DialDrawer.Shape.isLcdSupport(lcd)) {
                field = value
                shape = DialDrawer.Shape.createFromLcd(lcd)!!.adjustRecommendCorners()
            }
        }

    private var shape = DialDrawer.Shape.createFromLcd(lcd)!!.adjustRecommendCorners()

    var sources: MutableList<DialInfo>? = null
    var listener: Listener? = null
    var isEditMode = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    fun delete(dial: DialInfo) {
        if (sources?.remove(dial) == true) {
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        val count = sources?.size ?: 0
        return if (isShowCustomView) count + 1 else count
    }

    override fun getItemViewType(position: Int): Int {
        return if (isShowCustomView && position == 0) {
            TYPE_CUSTOM
        } else {
            TYPE_INFO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_CUSTOM) {
            DialCustomViewHolder(DialCustomView(parent.context))
        } else {
            DialInfoViewHolder(DialInfoView(parent.context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context

        if (holder is DialCustomViewHolder) {
            holder.view.setShape(shape)
            holder.view.setOnClickListener {
                context.startActivity(Intent(context, DialCustomActivity::class.java))
            }
        } else if (holder is DialInfoViewHolder) {
            val sources = this.sources ?: return
            val dialInfo: DialInfo = if (isShowCustomView) sources[position - 1] else sources[position]
            holder.view.setShape(shape)
            holder.view.imageView.setOnClickListener {
                if (!isEditMode) listener?.onDialClick(dialInfo)
            }

            val requestOptions: RequestOptions = RequestOptions.placeholderOf(R.drawable.ic_dial_load_failed)
            val imgFile = DialFileHelper.getNormalFileByUrl(context, dialInfo.imgUrl)
            if (imgFile?.exists() == true) {
                Glide.with(context)
                    .load(imgFile)
                    .apply(requestOptions)
                    .into(holder.view.imageView)
            } else {
                Glide.with(context)
                    .load(dialInfo.imgUrl)
                    .apply(requestOptions)
                    .into(holder.view.imageView)
            }

            if (isEditMode) {
                holder.view.deleteView.visibility = View.VISIBLE
                holder.view.deleteView.setOnClickListener {
                    listener?.onDialDelete(dialInfo)
                }
            } else {
                holder.view.deleteView.visibility = View.INVISIBLE
            }
        }

    }

}