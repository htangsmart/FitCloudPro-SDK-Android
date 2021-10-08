package com.github.kilnn.wristband2.sample.dial.custom

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.dial.createDefaultShape
import com.github.kilnn.wristband2.sample.utils.DisplayUtil
import com.htsmart.wristband2.dial.DialDrawer
import com.htsmart.wristband2.dial.DialView


class DialGridItem(val backgroundUri: Uri, val style: DialCustomCompat.Style, val position: DialDrawer.Position)

private class AddViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val dialView: DialView = itemView.findViewById(R.id.dial_view)
}

private class PreviewViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val dialView: DialView = itemView.findViewById(R.id.dial_view)
    val deleteView: ImageView = itemView.findViewById(R.id.img_delete)
}

class DialGridItemAdapter(private val editEnabled: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        fun setupRecyclerView(recyclerView: RecyclerView, adapterEditEnabled: Boolean): DialGridItemAdapter {
            //这个Padding是根据 R.layout.item_dial_grid 计算的固定值
            val paddingLeft = DisplayUtil.dip2px(recyclerView.context, 16f)
            recyclerView.setPadding(paddingLeft, 0, 0, 0)
            recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 3)
            return DialGridItemAdapter(adapterEditEnabled).also { recyclerView.adapter = it }
        }

        private const val TYPE_ADD = 0
        private const val TYPE_PREVIEW = 1

    }

    abstract class Listener {
        open fun onItemSelect(item: DialGridItem, position: Int) {}
        open fun onItemDelete(item: DialGridItem, position: Int) {}
        open fun onAddClick() {}
    }

    var listener: Listener? = null

    //默认创建一个Shape。免得出错
    var shape = createDefaultShape()

    var sources: MutableList<DialGridItem>? = null
        set(value) {
            field = value
            //纠正因数据改变，可能出错的选中项
            if (value == null || selectPosition >= value.size) {
                selectPosition = 0
            }
        }

    var selectPosition = 0 //默认选择第一个数据
        private set

    var isEditMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ADD) {
            AddViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_dial_add, parent, false)
            )
        } else {
            PreviewViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_dial_preview, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddViewHolder) {
            holder.dialView.shape = shape
            holder.dialView.setOnClickListener {
                if (isEditMode) {
                    isEditMode = false
                    notifyDataSetChanged()
                } else {
                    listener?.onAddClick()
                }
            }
        } else if (holder is PreviewViewHolder) {
            val dataPosition = if (editEnabled) position - 1 else position
            val data: DialGridItem = sources!![dataPosition]

            holder.dialView.shape = shape
            holder.dialView.setBackgroundSource(data.backgroundUri)
            holder.dialView.setStyleSource(data.style.styleUri, data.style.styleBaseOnWidth)
            holder.dialView.stylePosition = data.position
            holder.dialView.isChecked = dataPosition == selectPosition
            holder.dialView.setOnClickListener {
                if (selectPosition != dataPosition) {
                    selectPosition = dataPosition
                    listener?.onItemSelect(data, selectPosition)
                    notifyDataSetChanged()
                }
            }

            if (editEnabled) {
                holder.dialView.isLongClickable = true
                holder.dialView.setOnLongClickListener(OnLongClickListener {
                    if (isEditMode) {
                        false
                    } else {
                        isEditMode = true
                        notifyDataSetChanged()
                        true
                    }
                })
            } else {
                holder.dialView.isLongClickable = false
            }

            if (isEditMode) {
                val animation = AnimationUtils.loadAnimation(holder.deleteView.context, R.anim.scale_in)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        holder.deleteView.visibility = View.VISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                holder.deleteView.startAnimation(animation)
                holder.deleteView.setOnClickListener {
                    sources?.removeAt(dataPosition)?.let {
                        val sourceSize = sources?.size ?: 0//移除数据后，数据源的长度
                        if (selectPosition >= sourceSize) {
                            //如果长度超过了选择的位置，那么把选择位置设置为0
                            selectPosition = 0
                        }
                        if (sourceSize <= 0) {
                            //如果数据删除空了，那么退出编辑模式
                            isEditMode = false
                        }
                        notifyDataSetChanged()
                        listener?.onItemDelete(it, dataPosition)
                    }
                }
            } else {
                holder.deleteView.clearAnimation()
                holder.deleteView.visibility = View.INVISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        val count = sources?.size ?: 0
        return if (editEnabled) count + 1 else count
    }

    override fun getItemViewType(position: Int): Int {
        return if (editEnabled && position == 0) {
            TYPE_ADD
        } else {
            TYPE_PREVIEW
        }
    }
}