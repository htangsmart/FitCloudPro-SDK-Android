package com.github.kilnn.wristband2.sample.dial.custom

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.github.kilnn.wristband2.sample.R

class DialCustomPagerAdapter(
    backgroundView: View,
    styleView: View,
    positionView: View,

    /**
     * 是否除去styleView
     */
    excludeStyleView: Boolean
) : PagerAdapter() {

    private val context: Context = backgroundView.context
    private val views: Array<View> = if (excludeStyleView) {
        arrayOf(backgroundView, positionView)
    } else {
        arrayOf(backgroundView, styleView, positionView)
    }

    override fun getCount(): Int {
        return views.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(
            views[position],
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        return views[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (`object` is View) {
            container.removeView(`object`)
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        val resId = if (views.size == 2) {
            when (position) {
                0 -> R.string.ds_dial_background
                1 -> R.string.ds_dial_position
                else -> throw IllegalArgumentException()
            }
        } else {
            when (position) {
                0 -> R.string.ds_dial_background
                1 -> R.string.ds_dial_style
                2 -> R.string.ds_dial_position
                else -> throw IllegalArgumentException()
            }
        }
        return context.resources.getString(resId)
    }
}