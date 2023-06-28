package com.topstep.fitcloud.sample2.ui.device.game.push

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.databinding.ItemGameSkinBinding
import com.topstep.fitcloud.sample2.model.game.push.GameSkin
import com.topstep.fitcloud.sample2.utils.glideShowImage

class GameSkinAdapter : RecyclerView.Adapter<GameSkinAdapter.GameSkinViewHolder>() {

    var listener: Listener? = null
    var items: List<GameSkin>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameSkinViewHolder {
        return GameSkinViewHolder(
            ItemGameSkinBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: GameSkinViewHolder, position: Int) {
        val items = this.items ?: return
        val item = items[position]

        glideShowImage(holder.viewBind.img, item.imgUrl)
        if (item.existLocally) {
            holder.viewBind.tvExist.visibility = View.VISIBLE
            holder.itemView.setOnClickListener(null)
        } else {
            holder.viewBind.tvExist.visibility = View.GONE
            holder.itemView.clickTrigger {
                val actionPosition = holder.bindingAdapterPosition
                if (actionPosition != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(items[actionPosition])
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    interface Listener {
        fun onItemClick(skin: GameSkin)
    }

    class GameSkinViewHolder(val viewBind: ItemGameSkinBinding) : RecyclerView.ViewHolder(viewBind.root)

}