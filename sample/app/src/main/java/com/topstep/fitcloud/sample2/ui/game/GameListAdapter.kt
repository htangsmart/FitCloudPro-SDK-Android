package com.topstep.fitcloud.sample2.ui.game

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.github.kilnn.tool.ui.DisplayUtil
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.ItemGameListBinding
import com.topstep.fitcloud.sdk.v2.model.sg.FcSensorGameInfo

class GameListAdapter : RecyclerView.Adapter<GameListAdapter.ItemViewHolder>() {

    var sources: List<FcSensorGameInfo>? = null
    var listener: ((item: FcSensorGameInfo) -> Unit)? = null

    private var cornerRadius: Int? = null

    private fun getCornerRadius(context: Context): Int {
        return DisplayUtil.dip2px(context, 16f).also {
            this.cornerRadius = it
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemGameListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val sources = this.sources ?: return
        val item = sources[position]
        val context = holder.itemView.context
        holder.viewBind.tvName.text = item.name
        holder.viewBind.tvTitle.text = item.title

        Glide.with(context)
            .load(item.background)
            .placeholder(R.drawable.ic_default_image_place_holder)
            .transform(
                CenterCrop(),
                RoundedCorners(getCornerRadius(context))
            )
            .into(holder.viewBind.imageView)
        holder.itemView.clickTrigger {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION) {
                listener?.invoke(sources[actionPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    class ItemViewHolder(val viewBind: ItemGameListBinding) : RecyclerView.ViewHolder(viewBind.root)

}