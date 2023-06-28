package com.topstep.fitcloud.sample2.ui.device.sport.push

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.databinding.ItemSportUiItemBinding
import com.topstep.fitcloud.sample2.model.sport.push.SportPacket
import com.topstep.fitcloud.sample2.utils.glideShowImage

class SportUiItemAdapter(
    private val helper: SportUiHelper
) : RecyclerView.Adapter<SportUiItemAdapter.ItemViewHolder>() {

    var listener: Listener? = null

    var items: List<SportPacket>? = null

    private var categoryItems: List<SportPacket>? = null

    var category: Int = helper.categoryAll
        set(value) {
            field = value
            categoryItems = null
            if (category != helper.categoryAll) {
                items?.let {
                    val sports = helper.getCategorySports(category)
                    val list = ArrayList<SportPacket>()
                    for (item in it) {
                        for (show in sports) {
                            if (item.sportUiType == show) {
                                list.add(item)
                                break
                            }
                        }
                    }
                    categoryItems = list
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemSportUiItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val context = holder.itemView.context
        val items = if (category == helper.categoryAll) {
            items
        } else {
            categoryItems
        } ?: return
        val item = items[position]

        glideShowImage(holder.viewBind.img, item.iconUrl)

        holder.viewBind.tvName.text = helper.getTypeName(context, item)

        holder.itemView.clickTrigger {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION) {
                listener?.onItemSelect(items[actionPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        return if (category == helper.categoryAll) {
            items?.size ?: 0
        } else {
            categoryItems?.size ?: 0
        }
    }

    interface Listener {
        fun onItemSelect(packet: SportPacket)
    }

    class ItemViewHolder(val viewBind: ItemSportUiItemBinding) : RecyclerView.ViewHolder(viewBind.root)
}