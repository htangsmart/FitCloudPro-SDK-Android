package com.topstep.fitcloud.sample2.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class SignalView extends View {
    private static final float DEFAULT_SIZE = 18;//18dp
    private static final float DEFAULT_MARGIN = 1.5f;//1.5dp
    private int mDefaultSize;
    private Paint mPaint;

    private int mMaxSignal = 5;
    private int mCurrentSignal = 0;
    private int mSignalColorDisabled = Color.LTGRAY;
    private int mSignalColorEnabled = Color.GRAY;
    private int mSignalMargin;

    public SignalView(Context context) {
        super(context);
        init();
    }

    public SignalView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SignalView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SignalView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        final float density = getContext().getResources().getDisplayMetrics().density;
        mDefaultSize = (int) (density * DEFAULT_SIZE);
        mSignalMargin = (int) (density * DEFAULT_MARGIN);
        mPaint = new Paint();
    }

    public void setMaxSignal(int max) {
        mMaxSignal = max;
    }

    public void setCurrentSignal(int current) {
        mCurrentSignal = current;
    }

    public void setSignalColorDisabled(int color) {
        mSignalColorDisabled = color;
    }

    public void setSignalColorEnabled(int color) {
        mSignalColorEnabled = color;
    }

    public void setSignalMargin(int margin) {
        mSignalMargin = margin;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int signalEachHeight = getHeight() / mMaxSignal;
        int signalEachWidth = (getWidth() - mSignalMargin * (mMaxSignal - 1)) / mMaxSignal;
        for (int i = 1; i <= mMaxSignal; i++) {
            int color;
            if (mCurrentSignal >= i) {
                color = mSignalColorEnabled;
            } else {
                color = mSignalColorDisabled;
            }
            int left = signalEachWidth * (i - 1) + mSignalMargin * (i - 1);
            int top = signalEachHeight * (mMaxSignal - i);
            int right = left + signalEachWidth;
            int bottom = getHeight();

            mPaint.setColor(color);
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mDefaultSize, mDefaultSize);
        } else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mDefaultSize, heightSpecSize);
        } else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSpecSize, mDefaultSize);
        }
    }

}
