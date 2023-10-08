package com.topstep.fitcloud.sample2.ui.game

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.ui.DisplayUtil
import com.topstep.fitcloud.sample2.databinding.ItemGameCategoryBinding
import com.topstep.fitcloud.sample2.ui.widget.GridSimpleSpaceDecoration
import com.topstep.fitcloud.sdk.v2.model.sg.FcSensorGameCategory
import com.topstep.fitcloud.sdk.v2.model.sg.FcSensorGameInfo

class GameCategoryAdapter : RecyclerView.Adapter<GameCategoryAdapter.ItemViewHolder>() {

    var sources: List<FcSensorGameCategory>? = null
    var listener: ((item: FcSensorGameInfo) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val viewBind = ItemGameCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val spaceVertical = DisplayUtil.dip2px(parent.context, 16f)
        viewBind.recyclerView.addItemDecoration(GridSimpleSpaceDecoration(spaceVertical, 0))
        viewBind.recyclerView.layoutManager = LinearLayoutManager(parent.context)
        viewBind.recyclerView.setHasFixedSize(true)
        viewBind.recyclerView.isNestedScrollingEnabled = false
        return ItemViewHolder(viewBind)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val sources = this.sources ?: return
        val item = sources[position]
        holder.viewBind.tvName.text = item.name

        val adapter = GameListAdapter()
        adapter.sources = item.games
        holder.viewBind.recyclerView.adapter = adapter
        adapter.listener = listener
    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    class ItemViewHolder(val viewBind: ItemGameCategoryBinding) : RecyclerView.ViewHolder(viewBind.root)
}