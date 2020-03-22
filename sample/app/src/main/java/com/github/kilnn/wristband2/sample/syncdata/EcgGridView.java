package com.github.kilnn.wristband2.sample.syncdata;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.github.kilnn.wristband2.sample.R;


/**
 * Created by Kilnn on 2017/5/12.
 * Ecg background grid view
 */
public class EcgGridView extends View {

    public EcgGridView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public EcgGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public EcgGridView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EcgGridView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private static final int DEFAULT_LINE_COLOR = 0xFFCCCCCC;
    private static final int DEFAULT_LINE_WIDTH = 1;//1dp
    private static final int DEFAULT_LINE_GAP = 5;//5dp
    private static final int DEFAULT_BOLD_LINE_GAP = 5;//默认间隔5个

    private int mLineColor;
    private float mLineWidth;
    private float mLineGap;
    private boolean mBoldLineEnabled;
    private int mBoldLineColor;
    private float mBoldLineWidth;
    private int mBoldLineGap;

    private Paint mPaint;

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mLineColor = DEFAULT_LINE_COLOR;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mLineWidth = DEFAULT_LINE_WIDTH * metrics.density;
        mLineGap = DEFAULT_LINE_GAP * metrics.density;
        mBoldLineEnabled = true;
        mBoldLineColor = mLineColor;
        mBoldLineWidth = mLineWidth * 1.5f;
        mBoldLineGap = DEFAULT_BOLD_LINE_GAP;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EcgGridView, defStyleAttr, 0);
            mLineColor = a.getColor(R.styleable.EcgGridView_grid_line_color, mLineColor);
            mLineWidth = a.getDimension(R.styleable.EcgGridView_grid_line_width, mLineWidth);
            mLineGap = a.getDimension(R.styleable.EcgGridView_grid_line_gap, mLineGap);

            mBoldLineEnabled = a.getBoolean(R.styleable.EcgGridView_grid_bold_line_enabled, mBoldLineEnabled);
            mBoldLineColor = a.getColor(R.styleable.EcgGridView_grid_bold_line_color, mLineColor);
            mBoldLineWidth = a.getDimension(R.styleable.EcgGridView_grid_bold_line_width, mLineWidth * 1.5f);
            mBoldLineGap = a.getInt(R.styleable.EcgGridView_grid_bold_line_gap, mBoldLineGap);
            a.recycle();
        }

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        int right = left + width;
        int bottom = top + height;

        //set a offset width half line width, so the first line can display all.
        final float offset = mBoldLineEnabled ? mBoldLineWidth / 2 : mLineWidth / 2;

        //draw horizontal lines
        int horizontalCount = (int) Math.ceil(height / mLineGap) + 1;
        for (int i = 0; i < horizontalCount; i++) {
            boolean drawBold = mBoldLineEnabled && i % mBoldLineGap == 0;
            if (drawBold) {
                mPaint.setColor(mBoldLineColor);
                mPaint.setStrokeWidth(mBoldLineWidth);
            } else {
                mPaint.setColor(mLineColor);
                mPaint.setStrokeWidth(mLineWidth);
            }
            float y = i * mLineGap + offset;
            canvas.drawLine(left, y, right, y, mPaint);
        }

        //draw vertical lines
        int verticalCount = (int) Math.ceil(width / mLineGap) + 1;
        for (int i = 0; i < verticalCount; i++) {
            boolean drawBold = mBoldLineEnabled && i % mBoldLineGap == 0;
            if (drawBold) {
                mPaint.setColor(mBoldLineColor);
                mPaint.setStrokeWidth(mBoldLineWidth);
            } else {
                mPaint.setColor(mLineColor);
                mPaint.setStrokeWidth(mLineWidth);
            }
            float x = i * mLineGap + offset;
            canvas.drawLine(x, top, x, bottom, mPaint);
        }
    }

    public void setLineColor(int color) {
        mLineColor = color;
    }

    public void setLineWidth(float width) {
        mLineWidth = width;
    }

    public void setLineGap(float gap) {
        mLineGap = gap;
    }

    public void setBoldLineEnabled(boolean enabled) {
        mBoldLineEnabled = enabled;
    }

    public void setBoldLineColor(int color) {
        mBoldLineColor = color;
    }

    public void setBoldLineWidth(float width) {
        mBoldLineWidth = width;
    }

    public void setBoldLineGap(int gap) {
        mBoldLineGap = gap;
    }

}