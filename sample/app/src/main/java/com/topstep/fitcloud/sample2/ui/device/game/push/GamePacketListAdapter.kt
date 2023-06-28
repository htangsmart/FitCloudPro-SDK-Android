package com.topstep.fitcloud.sample2.ui.device.game.push

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.databinding.ItemGamePacketBinding
import com.topstep.fitcloud.sample2.model.game.push.GamePacket
import com.topstep.fitcloud.sample2.utils.glideShowImage

class GamePacketListAdapter : RecyclerView.Adapter<GamePacketListAdapter.GamePacketViewHolder>() {

    var listener: Listener? = null
    var items: List<GamePacket>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamePacketViewHolder {
        return GamePacketViewHolder(
            ItemGamePacketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: GamePacketViewHolder, position: Int) {
        val items = this.items ?: return
        val item = items[position]

        glideShowImage(holder.viewBind.img, item.imgUrl)
        holder.viewBind.tvName.text = item.name
        holder.itemView.clickTrigger {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION) {
                listener?.onItemClick(items[actionPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class GamePacketViewHolder(val viewBind: ItemGamePacketBinding) : RecyclerView.ViewHolder(viewBind.root)

    interface Listener {
        fun onItemClick(packet: GamePacket)
    }

}