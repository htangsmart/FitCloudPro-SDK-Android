package com.topstep.fitcloud.sample2.ui.device.dial

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.util.ResourceUtil
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.databinding.ItemDialSpacePacketBinding
import com.topstep.fitcloud.sample2.model.dial.DialSpacePacket
import com.topstep.fitcloud.sample2.utils.KB
import com.topstep.fitcloud.sample2.utils.fileSizeStr
import com.topstep.fitcloud.sample2.utils.glideLoadDialBackground
import com.topstep.fitcloud.sample2.utils.glideLoadDialStyle
import com.topstep.fitcloud.sdk.v2.model.settings.dial.FcDialSpace
import com.topstep.fitcloud.sdk.v2.model.settings.dial.FcShape
import com.topstep.fitcloud.sdk.v2.utils.dial.DialDrawer

class DialSpacePacketAdapter(
    private val items: List<DialSpacePacket>,
    private val binSize: Long,
    private val shape: FcShape
) : RecyclerView.Adapter<DialSpacePacketAdapter.DialSpacePacketViewHolder>() {

    private var selectPosition = -1//Not selectable by default

    init {
        for (i in items.indices) {
            //By default, the first position where the dial space is larger than binSize is selected, if not, then the default value is -1
            if (isSelectable(items[i])) {
                selectPosition = i
                break
            }
        }
    }

    private fun isSelectable(item: DialSpacePacket): Boolean {
        return if (binSize <= 0 || item.spaceSize <= 0) {
            //Unknown size, allowing it to choose.
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
     * Selected position is available
     */
    fun hasSelectedItem(): Boolean {
        return selectPosition != -1
    }

    fun getSelectedItem(): DialSpacePacket {
        return items[selectPosition]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialSpacePacketViewHolder {
        return DialSpacePacketViewHolder(
            ItemDialSpacePacketBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: DialSpacePacketViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        //设置shape
        val dialView = holder.viewBind.dialView
        dialView.shape = shape

        if (item.dialType == FcDialSpace.DIAL_TYPE_NORMAL) {
            if (item.imgUrl.isNullOrEmpty()) {
                dialView.backgroundBitmap = null
            } else {
                glideLoadDialBackground(context, dialView, item.imgUrl)
            }
            dialView.setStyleBitmap(null, DialDrawer.STYLE_BASE_ON_WIDTH)
        } else {
            glideLoadDialBackground(
                context, dialView, ResourceUtil.getUriFromDrawableResId(
                    context,
                    com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_default_bg
                )
            )

            val styleResId = when (item.dialType) {
                FcDialSpace.DIAL_TYPE_CUSTOM_STYLE_WHITE -> com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_style_white
                FcDialSpace.DIAL_TYPE_CUSTOM_STYLE_BLACK -> com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_style_black
                FcDialSpace.DIAL_TYPE_CUSTOM_STYLE_YELLOW -> com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_style_yellow
                FcDialSpace.DIAL_TYPE_CUSTOM_STYLE_GREEN -> com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_style_green
                FcDialSpace.DIAL_TYPE_CUSTOM_STYLE_GRAY -> com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_style_gray
                else -> 0
            }
            if (styleResId != 0) {
                glideLoadDialStyle(
                    context, dialView, ResourceUtil.getUriFromDrawableResId(
                        context,
                        styleResId
                    ), DialDrawer.STYLE_BASE_ON_WIDTH
                )
            } else {
                dialView.setStyleBitmap(null, DialDrawer.STYLE_BASE_ON_WIDTH)
            }
            dialView.stylePosition = DialDrawer.Position.TOP
        }

        val isSelectable = isSelectable(item)

        dialView.isChecked = position == selectPosition
        if (isSelectable) {
            dialView.setLayerType(View.LAYER_TYPE_HARDWARE, paintSaturationMax)
            dialView.clickTrigger {
                selectPosition = holder.bindingAdapterPosition
                notifyDataSetChanged()
            }
        } else {
            dialView.setLayerType(View.LAYER_TYPE_HARDWARE, paintSaturationMin)
            dialView.setOnClickListener(null)
        }

        holder.viewBind.tvSpaceSize.isEnabled = isSelectable
        holder.viewBind.tvSpaceSize.text = fileSizeStr(item.spaceSize * KB)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class DialSpacePacketViewHolder(val viewBind: ItemDialSpacePacketBinding) : RecyclerView.ViewHolder(viewBind.root)

}
