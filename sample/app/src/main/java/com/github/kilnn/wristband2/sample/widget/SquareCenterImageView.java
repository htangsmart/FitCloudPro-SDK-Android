package com.github.kilnn.wristband2.sample.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by Kilnn on 2017/10/31.
 */

@TargetApi(9)
public class SquareCenterImageView extends AppCompatImageView {

    public SquareCenterImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SquareCenterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCenterImageView(Context context) {
        super(context);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.max(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }
}
