package com.topstep.fitcloud.sample2.ui.device.dial.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.databinding.ItemDialComponentBinding
import com.topstep.fitcloud.sample2.model.dial.DialSpacePacket
import com.topstep.fitcloud.sample2.ui.device.dial.createDefaultShape
import com.topstep.fitcloud.sample2.utils.glideShowImage

class DialComponentListAdapter : RecyclerView.Adapter<DialComponentListAdapter.DialComponentViewHolder>() {

    var shape = createDefaultShape()
    var items: MutableList<DialSpacePacket>? = null
    var listener: Listener? = null
    var selectPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialComponentViewHolder {
        return DialComponentViewHolder(
            ItemDialComponentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: DialComponentViewHolder, position: Int) {
        val items = this.items ?: return
        val item = items[position]

        holder.viewBind.cardView.setShape(shape)
        holder.viewBind.imgView.clickTrigger {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION) {
                listener?.onItemClick(actionPosition, items[actionPosition])
            }
        }

        glideShowImage(holder.viewBind.imgView, item.imgUrl)

        holder.viewBind.imgSelect.isVisible = selectPosition == position

        if (item.components.isNullOrEmpty()) {
            holder.viewBind.imgEdit.visibility = View.INVISIBLE
            holder.viewBind.imgEdit.setOnClickListener(null)
        } else {
            holder.viewBind.imgEdit.visibility = View.VISIBLE
            holder.viewBind.imgEdit.clickTrigger {
                val actionPosition = holder.bindingAdapterPosition
                if (actionPosition != RecyclerView.NO_POSITION) {
                    listener?.onEditClick(actionPosition, items[actionPosition])
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class DialComponentViewHolder(val viewBind: ItemDialComponentBinding) : RecyclerView.ViewHolder(viewBind.root)

    interface Listener {
        fun onItemClick(position: Int, item: DialSpacePacket)
        fun onEditClick(position: Int, item: DialSpacePacket)
    }
}