package com.topstep.fitcloud.sample2.ui.device.dial.component

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.databinding.ItemDialComponentStyleBinding
import com.topstep.fitcloud.sample2.utils.glideShowImage

class DialComponentStyleAdapter : RecyclerView.Adapter<DialComponentStyleAdapter.StyleViewHolder>() {

    var selectPosition = 0
    var items: List<String>? = null//组件的图片urls
    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleViewHolder {
        return StyleViewHolder(
            ItemDialComponentStyleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: StyleViewHolder, position: Int) {
        val items = this.items ?: return
        glideShowImage(holder.viewBind.imgView, items[position])
        holder.viewBind.dotView.isInvisible = selectPosition != position
        holder.itemView.clickTrigger {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION) {
                selectPosition = actionPosition
                notifyDataSetChanged()
                listener?.onItemSelect(actionPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class StyleViewHolder(val viewBind: ItemDialComponentStyleBinding) : RecyclerView.ViewHolder(viewBind.root)

    interface Listener {
        fun onItemSelect(position: Int)
    }

}