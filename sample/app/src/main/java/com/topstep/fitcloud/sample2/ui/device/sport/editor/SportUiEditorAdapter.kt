package com.topstep.fitcloud.sample2.ui.device.sport.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.databinding.ItemSportUiEditorBinding
import com.topstep.fitcloud.sample2.ui.device.sport.push.SportUiHelper

class SportUiEditorAdapter(
    private val helper: SportUiHelper,
) : RecyclerView.Adapter<SportUiEditorAdapter.ItemViewHolder>() {

    var listener: Listener? = null

    var items: List<Int> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemSportUiEditorBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val context = holder.itemView.context
        val sportType = items[position]

        holder.viewBind.tvName.text = helper.getTypeName(context, sportType)

        // Clear the previous click listener to avoid duplicate binding
        holder.viewBind.imgDelete.setOnClickListener(null)
        holder.viewBind.imgDelete.clickTrigger {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION && actionPosition < items.size) {
                listener?.onItemDelete(actionPosition, items[actionPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface Listener {
        fun onItemDelete(position: Int, sportType: Int)
    }

    class ItemViewHolder(val viewBind: ItemSportUiEditorBinding) : RecyclerView.ViewHolder(viewBind.root)
}
