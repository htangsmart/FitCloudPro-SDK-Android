package com.topstep.fitcloud.sample2.ui.device.card

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.topstep.fitcloud.sample2.databinding.ItemCustomCardListBinding
import com.topstep.fitcloud.sdk.v2.model.settings.FcCustomCard
import java.util.Collections

class CustomCardAdapter : RecyclerView.Adapter<CustomCardAdapter.ItemViewHolder>() {

    private val sources = ArrayList<FcCustomCard>()

    var listener: Listener? = null

    fun getItems(): List<FcCustomCard> {
        return sources
    }

    fun setItems(s: List<FcCustomCard>?) {
        sources.clear()
        if (s != null) {
            sources.addAll(s)
        }
        notifyDataSetChanged()
    }

    fun addItem(card: FcCustomCard) {
        sources.add(card)
        notifyDataSetChanged()
    }

    fun editItem(card: FcCustomCard, position: Int) {
        sources[position] = card
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        sources.removeAt(position)
        notifyDataSetChanged()
    }

    fun swipeItem(from: Int, to: Int) {
        Collections.swap(sources, from, to)
        notifyDataSetChanged()
    }

    fun findNextId(): Int {
        val list = sources.toMutableList()
        list.sortBy { it.id }
        var id = 0//Id从0开始
        for (l in list) {
            if (l.id > id) {
                return id
            } else {
                id++
            }
        }
        return id
    }

    private val callback = object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            var dragFlag = 0
            if (viewHolder is ItemViewHolder) {
                dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            }
            return makeMovementFlags(dragFlag, 0)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            listener?.onItemSwipe(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

        override fun isLongPressDragEnabled(): Boolean {
            return false
        }
    }

    val itemTouchHelper = ItemTouchHelper(callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemCustomCardListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = sources[position]
        holder.viewBind.tvTitle.text = item.title
        holder.viewBind.tvContent.text = item.content

        holder.viewBind.imgSort.setOnTouchListener { _, event -> //注意：这里down和up都会回调该方法
            if (event.action == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(holder)
            }
            false
        }

        holder.viewBind.tvDelete.setOnClickListener {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION) {
                listener?.onItemDelete(actionPosition)
            }
        }
        holder.viewBind.layoutContent.setOnClickListener {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION) {
                listener?.onItemClick(sources[actionPosition], actionPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    interface Listener {
        fun onItemClick(card: FcCustomCard, position: Int)
        fun onItemDelete(position: Int)
        fun onItemSwipe(from: Int, to: Int)
    }

    class ItemViewHolder(val viewBind: ItemCustomCardListBinding) : RecyclerView.ViewHolder(viewBind.root)

}