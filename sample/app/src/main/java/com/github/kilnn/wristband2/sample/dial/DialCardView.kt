package com.github.kilnn.wristband2.sample.dial

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.CallSuper
import androidx.cardview.widget.CardView
import com.github.kilnn.wristband2.sample.R
import com.htsmart.wristband2.dial.DialDrawer
import com.htsmart.wristband2.dial.DialView


/**
 * 以CardView wrap的表盘等View
 */
abstract class DialCardView : CardView {

    private val contentView: View
    private var aspectRatio = 1.0f //宽高比

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        contentView = initAndGetContentView(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val lp = contentView.layoutParams as LayoutParams
        val imgWidth = widthSize - contentPaddingLeft - contentPaddingRight - lp.leftMargin - lp.rightMargin
        val imgHeight = (imgWidth / aspectRatio).toInt()
        val heightSize = imgHeight + contentPaddingTop + contentPaddingBottom + lp.topMargin + lp.bottomMargin
        setMeasuredDimension(widthSize, heightSize)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST))
    }

    @CallSuper
    open fun setShape(shape: DialDrawer.Shape) {
        val aspectRatio = shape.width().toFloat() / shape.height() //宽高比
        if (aspectRatio != this.aspectRatio) {
            this.aspectRatio = aspectRatio
            requestLayout()
        }
    }

    protected abstract fun initAndGetContentView(context: Context): View

}

class DialCustomView(context: Context) : DialCardView(context) {
    lateinit var dialView: DialView

    override fun initAndGetContentView(context: Context): View {
        LayoutInflater.from(context).inflate(R.layout.item_dial_custom, this)
        dialView = findViewById(R.id.dial_view)
        return dialView
    }

    override fun setShape(shape: DialDrawer.Shape) {
        super.setShape(shape)
        dialView.shape = shape
    }
}

class DialInfoView(context: Context) : DialCardView(context) {
    lateinit var imageView: ImageView
    lateinit var deleteView: ImageView

    override fun initAndGetContentView(context: Context): View {
        LayoutInflater.from(context).inflate(R.layout.item_dial_style, this)
        imageView = findViewById(R.id.img_view)
        deleteView = findViewById(R.id.img_delete)
        return imageView
    }

}

class DialComponentItemView(context: Context) : DialCardView(context) {
    lateinit var imageView: ImageView
    lateinit var editView: ImageView
    lateinit var selectView: ImageView

    override fun initAndGetContentView(context: Context): View {
        LayoutInflater.from(context).inflate(R.layout.item_dial_component, this)
        imageView = findViewById(R.id.img_view)
        editView = findViewById(R.id.img_edit)
        selectView = findViewById(R.id.img_select)
        return imageView
    }
}

fun DialDrawer.Shape.adjustRecommendCorners(): DialDrawer.Shape {
    if (this.isShapeRectangle && this.width() != this.height()) {
        this.setCorners(48)
    }
    return this
}