package com.topstep.fitcloud.pro.ui.device.game.push

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.databinding.ItemGameSpaceSkinBinding
import com.topstep.fitcloud.sample2.model.game.push.GameSpaceSkin
import com.topstep.fitcloud.sample2.utils.KB
import com.topstep.fitcloud.sample2.utils.fileSizeStr
import com.topstep.fitcloud.sample2.utils.glideShowImage

class GameSpaceSkinAdapter(
    private val items: ArrayList<GameSpaceSkin>,
    private val binSize: Long,
) : RecyclerView.Adapter<GameSpaceSkinAdapter.GameSpaceSkinViewHolder>() {

    private var selectPosition = -1//默认不可选

    init {
        for (i in items.indices) {
            //默认选择第一个表盘空间大小binSize的位置，如果没有，那么就是默认值-1
            if (isSelectable(items[i])) {
                selectPosition = i
                break
            }
        }
    }

    private fun isSelectable(item: GameSpaceSkin): Boolean {
        return if (binSize <= 0 || item.spaceSize <= 0) {//未知的大小
            true
        } else {
            return item.spaceSize * KB > binSize
        }
    }

    private val paintSaturationMin: Paint by lazy {
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0.0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        paint
    }

    private val paintSaturationMax: Paint by lazy {
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(1.0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        paint
    }

    /**
     * 选择位置是否可用
     */
    fun hasSelectBinFlag(): Boolean {
        return selectPosition != -1
    }

    fun getSelectBinFlag(): Byte {
        return items[selectPosition].binFlag
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameSpaceSkinViewHolder {
        return GameSpaceSkinViewHolder(
            ItemGameSpaceSkinBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: GameSpaceSkinViewHolder, position: Int) {
        val item = items[position]

        glideShowImage(holder.viewBind.img, item.imgUrl)

        val isSelectable = isSelectable(item)

        holder.viewBind.viewSelect.isInvisible = position != selectPosition
        if (isSelectable) {
            holder.itemView.setLayerType(View.LAYER_TYPE_HARDWARE, paintSaturationMax)
            holder.itemView.clickTrigger {
                selectPosition = holder.bindingAdapterPosition
                notifyDataSetChanged()
            }
        } else {
            holder.itemView.setLayerType(View.LAYER_TYPE_HARDWARE, paintSaturationMin)
            holder.itemView.setOnClickListener(null)
        }

        holder.viewBind.tvSpaceSize.isEnabled = isSelectable
        holder.viewBind.tvSpaceSize.text = fileSizeStr(item.spaceSize * KB)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class GameSpaceSkinViewHolder(val viewBind: ItemGameSpaceSkinBinding) : RecyclerView.ViewHolder(viewBind.root)

}
