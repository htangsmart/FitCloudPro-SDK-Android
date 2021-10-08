package com.github.kilnn.wristband2.sample.dial.component

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.dial.DialComponentItemView
import com.github.kilnn.wristband2.sample.dial.DialFileHelper
import com.github.kilnn.wristband2.sample.dial.createDefaultShape
import com.github.kilnn.wristband2.sample.dial.task.DialBinParam
import com.htsmart.wristband2.dial.DialDrawer

class DialComponentAdapter : RecyclerView.Adapter<DialComponentAdapter.DialComponentViewHolder>() {

    interface Listener {
        fun onItemClick(position: Int, param: DialBinParam)
        fun onEditClick(position: Int, param: DialBinParam)
    }

    class DialComponentViewHolder(itemView: DialComponentItemView) : RecyclerView.ViewHolder(itemView) {
        var componentView: DialComponentItemView = itemView
    }

    //默认创建一个Shape。免得出错
    var shape: DialDrawer.Shape = createDefaultShape()

    var sources: MutableList<DialBinParam>? = null
    var listener: Listener? = null

    var selectPosition = 0

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialComponentViewHolder {
        return DialComponentViewHolder(DialComponentItemView(parent.context))
    }

    override fun onBindViewHolder(holder: DialComponentViewHolder, position: Int) {
        val param = sources?.get(position) ?: return
        val context: Context = holder.componentView.context

        holder.componentView.setShape(shape)
        holder.componentView.imageView.setOnClickListener {
            listener?.onItemClick(position, param)
        }

        val requestOptions = RequestOptions.placeholderOf(R.drawable.ic_dial_load_failed)
        val imgFile = DialFileHelper.getNormalFileByUrl(context, param.imgUrl)
        if (imgFile != null && imgFile.exists()) {
            Glide.with(context)
                .load(imgFile)
                .apply(requestOptions)
                .into(holder.componentView.imageView)
        } else {
            Glide.with(context)
                .load(param.imgUrl)
                .apply(requestOptions)
                .into(holder.componentView.imageView)
        }

        if (selectPosition == position) {
            holder.componentView.selectView.visibility = View.VISIBLE
        } else {
            holder.componentView.selectView.visibility = View.INVISIBLE
        }

        if (param.components.isNullOrEmpty()) {
            holder.componentView.editView.visibility = View.INVISIBLE
            holder.componentView.editView.setOnClickListener(null)
        } else {
            holder.componentView.editView.visibility = View.VISIBLE
            holder.componentView.editView.setOnClickListener {
                listener?.onEditClick(position, param)
            }
        }
    }
}