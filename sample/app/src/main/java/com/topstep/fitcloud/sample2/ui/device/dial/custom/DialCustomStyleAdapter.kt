package com.topstep.fitcloud.sample2.ui.device.dial.custom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.databinding.ItemDialCustomStyleBinding
import com.topstep.fitcloud.sample2.model.dial.DialCustomParams
import com.topstep.fitcloud.sample2.utils.glideShowImage

class DialCustomStyleAdapter : RecyclerView.Adapter<DialCustomStyleAdapter.StyleViewHolder>() {

    var selectPosition = 0
    var items: List<DialCustomParams.Style>? = null
    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleViewHolder {
        return StyleViewHolder(
            ItemDialCustomStyleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: StyleViewHolder, position: Int) {
        val items = this.items ?: return
        glideShowImage(holder.viewBind.imgView, items[position].styleUri)
        holder.viewBind.dotView.isInvisible = selectPosition != position
        holder.itemView.clickTrigger {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION) {
                selectPosition = actionPosition
                notifyDataSetChanged()
                listener?.onItemSelect(actionPosition, items[actionPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class StyleViewHolder(val viewBind: ItemDialCustomStyleBinding) : RecyclerView.ViewHolder(viewBind.root)

    interface Listener {
        fun onItemSelect(position: Int, item: DialCustomParams.Style)
    }

}