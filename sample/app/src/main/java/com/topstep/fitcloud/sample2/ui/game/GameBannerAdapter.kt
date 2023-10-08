package com.topstep.fitcloud.sample2.ui.game

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.github.kilnn.tool.ui.DisplayUtil
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.ItemGameBannerBinding
import com.topstep.fitcloud.sdk.v2.model.sg.FcSensorGameBanner
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder

class GameBannerAdapter : BaseBannerAdapter<FcSensorGameBanner>() {

    private var cornerRadius: Int? = null

    private fun getCornerRadius(context: Context): Int {
        return DisplayUtil.dip2px(context, 16f).also {
            this.cornerRadius = it
        }
    }

    override fun createViewHolder(parent: ViewGroup, itemView: View, viewType: Int): BaseViewHolder<FcSensorGameBanner> {
        return BannerViewHolder(ItemGameBannerBinding.bind(itemView))
    }

    override fun bindData(holder: BaseViewHolder<FcSensorGameBanner>, data: FcSensorGameBanner, position: Int, pageSize: Int) {
        if (holder is BannerViewHolder) {
            val context = holder.viewBinding.imageView.context
            Glide.with(context)
                .load(data.image)
                .placeholder(R.drawable.ic_default_image_place_holder)
                .transform(
                    CenterCrop(),
                    RoundedCorners(getCornerRadius(context))
                )
                .into(holder.viewBinding.imageView)
        }
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_game_banner
    }

    internal class BannerViewHolder(var viewBinding: ItemGameBannerBinding) : BaseViewHolder<FcSensorGameBanner>(viewBinding.root)

}
