package com.topstep.fitcloud.sample2.ui.device.dial.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.databinding.ItemDialLibraryBinding
import com.topstep.fitcloud.sample2.model.dial.DialPacket
import com.topstep.fitcloud.sample2.ui.device.dial.createDefaultShape
import com.topstep.fitcloud.sample2.utils.glideShowImage
import com.topstep.fitcloud.sdk.v2.model.settings.dial.FcShape

class DialLibraryAdapter : RecyclerView.Adapter<DialLibraryAdapter.DialLibraryViewHolder>() {

    //Create a Shape by default. To avoid error
    var shape: FcShape = createDefaultShape()

    var listener: Listener? = null

    var items: List<DialPacket>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialLibraryViewHolder {
        return DialLibraryViewHolder(
            ItemDialLibraryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: DialLibraryViewHolder, position: Int) {
        val items = this.items ?: return
        val item = items[position]
        holder.viewBind.cardView.setShape(shape)
        holder.itemView.clickTrigger {
            listener?.onItemClick(item)
        }
        glideShowImage(holder.viewBind.imgView, item.imgUrl)
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class DialLibraryViewHolder(val viewBind: ItemDialLibraryBinding) : RecyclerView.ViewHolder(viewBind.root)

    interface Listener {
        fun onItemClick(packet: DialPacket)
    }
}