package com.topstep.fitcloud.sample2.ui.device.sport.push

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.util.ResourceUtil
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.google.android.material.color.MaterialColors
import com.topstep.fitcloud.sample2.databinding.ItemSportUiCategoryBinding

class SportUiCategoryAdapter(
    private val helper: SportUiHelper
) : RecyclerView.Adapter<SportUiCategoryAdapter.CategoryViewHolder>() {

    private val items = helper.getCategories()

    var listener: Listener? = null
    var selectPosition = 0

    private var colorPrimary: Int? = null
    private var textColorPrimary: ColorStateList? = null

    private fun getColorPrimary(context: Context): Int {
        return colorPrimary ?: MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, 0).also {
            colorPrimary = it
        }
    }

    private fun getTextColorPrimary(context: Context): ColorStateList {
        return textColorPrimary ?: ResourceUtil.getTextColorPrimary(context).also {
            textColorPrimary = it
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemSportUiCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val context = holder.itemView.context
        val textView = holder.viewBind.tvName
        textView.text = helper.getCategoryName(context, items[position])
        if (selectPosition == position) {
            textView.setTextColor(getColorPrimary(context))
//            textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        } else {
            textView.setTextColor(getTextColorPrimary(context))
//            textView.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        }
        textView.clickTrigger {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION && selectPosition != actionPosition) {
                selectPosition = actionPosition
                notifyDataSetChanged()
                listener?.onItemSelect(items[selectPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getSelectCategory(): Int {
        return items[selectPosition]
    }

    interface Listener {
        fun onItemSelect(category: Int)
    }

    class CategoryViewHolder(val viewBind: ItemSportUiCategoryBinding) : RecyclerView.ViewHolder(viewBind.root)

}