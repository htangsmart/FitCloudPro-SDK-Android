package com.github.kilnn.wristband2.sample.dial.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.widget.DotView

class DialComponentSelectAdapter : RecyclerView.Adapter<DialComponentSelectAdapter.DialComponentSelectViewHolder>() {

    interface Listener {
        fun onItemSelect(position: Int)
    }

    var selectPosition = 0
    var sources: List<String>? = null//组件的图片urls
    var listener: Listener? = null

    class DialComponentSelectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgView: ImageView = itemView.findViewById(R.id.img_view)
        var dotView: DotView = itemView.findViewById(R.id.dot_view)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialComponentSelectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dial_component_select, parent, false)
        return DialComponentSelectViewHolder(view)
    }

    override fun onBindViewHolder(holder: DialComponentSelectViewHolder, position: Int) {
        val requestOptions = RequestOptions.placeholderOf(R.drawable.ic_dial_load_failed)
        Glide.with(holder.itemView.context)
            .load(sources?.get(position))
            .apply(requestOptions)
            .into(holder.imgView)
        if (selectPosition == position) {
            holder.dotView.visibility = View.VISIBLE
        } else {
            holder.dotView.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener {
            selectPosition = position
            notifyDataSetChanged()
            listener?.onItemSelect(position)
        }
    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

}