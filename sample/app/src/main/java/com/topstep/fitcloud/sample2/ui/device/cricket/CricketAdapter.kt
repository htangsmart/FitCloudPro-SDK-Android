package com.topstep.fitcloud.sample2.ui.device.cricket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.databinding.ItemCricketBinding

class CricketAdapter(val sources: List<CricketInfo>) : RecyclerView.Adapter<CricketAdapter.ItemViewHolder>() {

    val selected: ArrayList<Int> = ArrayList(10)
    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemCricketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = sources[position]
        holder.viewBind.tvState.text = when (item.state) {
            CricketInfo.State.UPCOMING -> "Upcoming"
            CricketInfo.State.LIVE -> "Live"
            else -> "Result"
        }
        holder.viewBind.tvMatchId.text = item.matchId.toString()
        holder.viewBind.tvMatchName.text = item.matchName
        holder.itemView.clickTrigger {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION) {
                listener?.onItemClick(item, actionPosition)
            }
        }
        holder.viewBind.checkbox.setOnCheckedChangeListener(null)
        val selectedIndex = selected.indexOf(position)
        holder.viewBind.checkbox.isChecked = selectedIndex != -1
        holder.viewBind.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION && buttonView.isPressed) {
                if (isChecked) {
                    selected.add(actionPosition)
                } else {
                    selected.remove(actionPosition)
                }
                notifyDataSetChanged()
            }
        }
        if (selectedIndex == -1) {
            holder.viewBind.tvIndex.text = null
        } else {
            holder.viewBind.tvIndex.text = (selectedIndex + 1).toString()
        }
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    interface Listener {
        fun onItemClick(item: CricketInfo, position: Int)
    }

    class ItemViewHolder(val viewBind: ItemCricketBinding) : RecyclerView.ViewHolder(viewBind.root)

}