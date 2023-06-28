package com.topstep.fitcloud.sample2.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.kilnn.tool.ui.DisplayUtil;
import com.topstep.fitcloud.sample2.R;


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

    private static final int DEFAULT_LINE_COLOR = 0xFFCCCCCC;
    private static final int DEFAULT_LINE_WIDTH = 1;//1dp
    private static final int DEFAULT_BOLD_LINE_GAP = 5;//Default interval of 5

    private int mLineColor;
    private float mLineWidth;
    private boolean mBoldLineEnabled;
    private int mBoldLineColor;
    private float mBoldLineWidth;
    private int mBoldLineGap;

    private int mGridVerticalCount = 40;
    private float mGridWidth;//The width of each small grid
    private float mGridHeight;//The height of each small grid

    private Paint mPaint;

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mLineColor = DEFAULT_LINE_COLOR;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mLineWidth = DEFAULT_LINE_WIDTH * metrics.density;
        mBoldLineEnabled = true;
        mBoldLineColor = mLineColor;
        mBoldLineWidth = mLineWidth * 1.5f;
        mBoldLineGap = DEFAULT_BOLD_LINE_GAP;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EcgGridView, defStyleAttr, 0);
            mLineColor = a.getColor(R.styleable.EcgGridView_grid_line_color, mLineColor);
            mLineWidth = a.getDimension(R.styleable.EcgGridView_grid_line_width, mLineWidth);
            mBoldLineEnabled = a.getBoolean(R.styleable.EcgGridView_grid_bold_line_enabled, mBoldLineEnabled);
            mBoldLineColor = a.getColor(R.styleable.EcgGridView_grid_bold_line_color, mLineColor);
            mBoldLineWidth = a.getDimension(R.styleable.EcgGridView_grid_bold_line_width, mLineWidth * 1.5f);
            mBoldLineGap = a.getInt(R.styleable.EcgGridView_grid_bold_line_gap, mBoldLineGap);
            mGridVerticalCount = a.getInt(R.styleable.EcgGridView_grid_vertical_count, mGridVerticalCount);
            a.recycle();
        }

        mGridWidth = DisplayUtil.get_x_pix_per_mm(getContext());
        mGridHeight = DisplayUtil.get_y_pix_per_mm(getContext());

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            float gridHeight = mGridVerticalCount * mGridHeight;
//            float bottomLineExtraHeight = mBoldLineEnabled ? mBoldLineWidth / 2 : mLineWidth / 2;//最下面线条为了显示完全需要的高度
//            float topLineExtraHeight = ((mBoldLineEnabled && (mGridVerticalCount % mBoldLineGap == 0)) ? mBoldLineWidth / 2 : mLineWidth / 2;//最上面线条为了显示完全需要的高度
//            setMeasuredDimension(getMeasuredWidth(), (int) (gridHeight + bottomLineExtraHeight + topLineExtraHeight));
            setMeasuredDimension(getMeasuredWidth(), (int) (gridHeight));
        }
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
//        final float offset = mBoldLineEnabled ? mBoldLineWidth / 2 : mLineWidth / 2;
        final float offset = 0;

        //draw horizontal lines
        int horizontalCount = (int) Math.ceil(height / mGridHeight) + 1;
        for (int i = 0; i < horizontalCount; i++) {
            boolean drawBold = mBoldLineEnabled && i % mBoldLineGap == 0;
            if (drawBold) {
                mPaint.setColor(mBoldLineColor);
                mPaint.setStrokeWidth(mBoldLineWidth);
            } else {
                mPaint.setColor(mLineColor);
                mPaint.setStrokeWidth(mLineWidth);
            }
            float y = height - (i * mGridHeight + offset);//从底部开始画
            canvas.drawLine(left, y, right, y, mPaint);
        }

        //draw vertical lines
        int verticalCount = (int) Math.ceil(width / mGridWidth) + 1;
        for (int i = 0; i < verticalCount; i++) {
            boolean drawBold = mBoldLineEnabled && i % mBoldLineGap == 0;
            if (drawBold) {
                mPaint.setColor(mBoldLineColor);
                mPaint.setStrokeWidth(mBoldLineWidth);
            } else {
                mPaint.setColor(mLineColor);
                mPaint.setStrokeWidth(mLineWidth);
            }
            float x = i * mGridWidth + offset;
            canvas.drawLine(x, top, x, bottom, mPaint);
        }
    }

    public void setLineColor(int color) {
        mLineColor = color;
    }

    public void setLineWidth(float width) {
        mLineWidth = width;
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