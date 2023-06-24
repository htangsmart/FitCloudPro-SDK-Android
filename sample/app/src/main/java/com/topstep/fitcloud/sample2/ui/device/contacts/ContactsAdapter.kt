package com.topstep.fitcloud.sample2.ui.device.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topstep.fitcloud.sample2.databinding.ItemContactsListBinding
import com.topstep.fitcloud.sdk.v2.model.settings.FcContacts

class ContactsAdapter : RecyclerView.Adapter<ContactsAdapter.ItemViewHolder>() {

    var sources: ArrayList<FcContacts>? = null
    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemContactsListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val items = this.sources ?: return
        val item = items[position]
        holder.viewBind.tvName.text = item.name
        holder.viewBind.tvNumber.text = item.number
        holder.viewBind.imgDelete.setOnClickListener {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION) {
                listener?.onItemDelete(actionPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    interface Listener {
        fun onItemDelete(position: Int)
    }

    class ItemViewHolder(val viewBind: ItemContactsListBinding) : RecyclerView.ViewHolder(viewBind.root)
}