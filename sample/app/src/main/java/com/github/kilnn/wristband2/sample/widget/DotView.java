package com.github.kilnn.wristband2.sample.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;

import com.github.kilnn.wristband2.sample.R;


/**
 * Created by Kilnn on 2017/10/30.
 */

public class DotView extends androidx.appcompat.widget.AppCompatTextView {

    public DotView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public DotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public DotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        // Styleables from XML
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DotView, defStyleAttr, 0);
        int color = a.getColor(R.styleable.DotView_dot_color, Color.WHITE);
        int size = a.getDimensionPixelSize(R.styleable.DotView_dot_size, 0);

        if (size != 0) {
            ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
            drawable.getPaint().setColor(color);
            drawable.setBounds(0, 0, size, size);
            setCompoundDrawablesRelative(
                    drawable,
                    getCompoundDrawables()[1],
                    getCompoundDrawables()[2],
                    getCompoundDrawables()[3]
            );
        }
        a.recycle();
    }
}
