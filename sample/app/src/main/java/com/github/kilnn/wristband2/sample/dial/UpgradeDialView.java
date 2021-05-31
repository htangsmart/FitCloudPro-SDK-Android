package com.github.kilnn.wristband2.sample.dial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.github.kilnn.wristband2.sample.R;

/**
 * 提供进度表展示的简单View
 */
public class UpgradeDialView extends AppCompatTextView {

    private int mProgress = 100;
    private Paint mPaint;
    private Rect mRect;

    public UpgradeDialView(Context context) {
        super(context);
        init();
    }

    public UpgradeDialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UpgradeDialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
        mRect = new Rect();
    }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mRect.set(0, 0, getWidth(), getHeight());
        mPaint.setAlpha(0x99);
        canvas.drawRect(mRect, mPaint);
        mRect.set(0, 0, (int) (mProgress / 100.0f * getWidth()), getHeight());
        mPaint.setAlpha(0xFF);
        canvas.drawRect(mRect, mPaint);
        super.onDraw(canvas);
    }
}
