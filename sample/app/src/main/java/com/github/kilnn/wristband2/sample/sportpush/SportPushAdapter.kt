package com.github.kilnn.wristband2.sample.sportpush

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.sportpush.entity.SportPushWithIcon

class SportPushAdapter : RecyclerView.Adapter<SportPushAdapter.ViewHolder>() {

    interface Listener {
        fun onItemClick(position: Int, param: SportPushWithIcon)
    }

    class ViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView
    }

    var sources: List<SportPushWithIcon>? = null
    var listener: Listener? = null

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportPushAdapter.ViewHolder {
        return ViewHolder(ImageView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val param = sources?.get(position) ?: return
        val context: Context = holder.itemView.context

        holder.imageView.setOnClickListener {
            listener?.onItemClick(position, param)
        }

        val requestOptions = RequestOptions.placeholderOf(R.drawable.ic_dial_load_failed)
        Glide.with(context)
            .load(param.iconUrl)
            .apply(requestOptions)
            .into(holder.imageView)
    }

}