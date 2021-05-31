package com.github.kilnn.wristband2.sample.syncdata;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.syncdata.db.SleepItem;
import com.github.kilnn.wristband2.sample.syncdata.db.SleepRecord;
import com.github.kilnn.wristband2.sample.utils.DisplayUtil;
import com.htsmart.wristband2.bean.data.SleepItemData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.htsmart.wristband2.bean.data.SleepItemData.SLEEP_STATUS_SOBER;


/**
 * 睡眠日数据的图
 * Created by taowencong on 15-11-18.
 */
public class SleepDayView extends View {
    private static final int INVALID_POSITION = -1;
    private DrawParams mDrawParams;
    private SleepDayData[] mDayDatas;
    private ActiveRectParams mAnimRectParams;

    private int mAnimIndex;

    private GestureDetector mDetector;

    private SimpleDateFormat mHourFormat;

    public SleepDayView(Context context) {
        super(context);
        init();
    }

    public SleepDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SleepDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SleepDayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mHourFormat = new SimpleDateFormat("H:mm", Locale.getDefault());

        mDrawParams = new DrawParams();
        // 创建手势检测器
        mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                processClick(e);
                return super.onSingleTapUp(e);
            }

        });
        setLongClickable(true);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    processClick(event);
                }
                return mDetector.onTouchEvent(event);
            }
        });
    }

    private void processClick(MotionEvent e) {
        if (!mDrawParams.isValid() || mDayDatas == null || mDayDatas.length == 0)
            return;//无效，和OnDraw判断一致

        float x = e.getX();
        float y = e.getY();
        if (x < mOffsetX || x > getWidth() - mOffsetX ||
                y < mOffsetY || y > getHeight() - mOffsetY) {//不在矩形区域内点击的
            return;
        }
        /*判断点击在哪一个矩形*/
        int clickIndex = -1;
        float centerX = 0;
        float width = 0;
        float startX = mOffsetX;
        for (int i = 0; i < mDayDatas.length; i++) {
            SleepDayData data = mDayDatas[i];
            width = mRectWidth * data.percent;
            if (x >= startX && x <= startX + width) {
                clickIndex = i;
                centerX = startX + width / 2;
                break;
            }
            startX += width;
        }
        if (clickIndex == INVALID_POSITION) return;//没有找到(容错判断)
        if (clickIndex == mAnimIndex) return;//和最后一次执行动画的Index一样，那么不用在执行了

        /*开始执行这一次的动画*/
        mAnimIndex = clickIndex;

        if (mAnimRectParams == null) {
            mAnimRectParams = new ActiveRectParams();
        }
        mAnimRectParams.mCenterX = centerX;
        mAnimRectParams.mRectColor = mDrawParams.getRectColorWithValue(mDayDatas[clickIndex].value);
        mAnimRectParams.mRectWidth = width;
        mAnimRectParams.mRectHeight = mActiveHeight;//最终高度为激活高度
        mAnimRectParams.mTextAlpha = 255;//最终的alpha为255
        mAnimRectParams.mStartTime = mDayDatas[clickIndex].startTime;
        mAnimRectParams.mEndTime = mDayDatas[clickIndex].endTime;

        invalidate();
    }

    private float mOffsetX;
    private float mOffsetY;
    private float mRectWidth;
    private float mRectHeight;
    private float mActiveHeight;

    private Date mTempDate = new Date();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mDrawParams.isValid() || mDayDatas == null || mDayDatas.length == 0) return;

        /*计算X坐标绘制数据*/
        float offsetX = (getWidth() - mDrawParams.mActualWidth) / 2.0f;//矩形图在X坐标上的偏移量
        /*因为要绘制文字，保证文字不超出视图所以要调整这个偏移量*/
        float textMaxHalfWidth = mDrawParams.mSmallTextWidth > mDrawParams.mLargeTextWidth ? mDrawParams.mSmallTextWidth / 2.0f : mDrawParams.mLargeTextWidth / 2.0f;
        if (offsetX < textMaxHalfWidth) {
            offsetX = textMaxHalfWidth;
        }
        float rectWidth = getWidth() - offsetX * 2;//矩形的宽度

        /*计算Y坐标绘制数据*/
        float offsetY = (getHeight() - mDrawParams.mActualHeight) / 2.0f;
        float fitHeight = mDrawParams.mTextHeight * 2 + mDrawParams.mVerticalSpace * 2;//已固定元素的高度(上下文字，间距)
        float activeHeight = mDrawParams.mActualHeight - fitHeight;//剩下的高度，需要分配给激活的矩形区域，矩形图的按照比例缩小
        float rectHeight = activeHeight / DrawParams.ACTIVE_RATIO;////矩形的高度
        offsetY += ((mDrawParams.mActualHeight - rectHeight) / 2.0f);

        float startX = offsetX;
        float p = 0;
        for (int i = 0; i < mDayDatas.length; i++) {
            SleepDayData data = mDayDatas[i];
            float width = rectWidth * data.percent;
            if (i != mAnimIndex)
                canvas.drawRect(startX, offsetY, startX + width, offsetY + rectHeight, mDrawParams.getRectPaintWithValue(data.value));
            startX += width;
            p += data.percent;
        }

        //绘制起始时刻
        mTempDate.setTime(mDayDatas[0].startTime * 1000L);
        String startText = mHourFormat.format(mTempDate);
        canvas.drawText(startText, offsetX - mDrawParams.mLargeTextWidth / 2.0f, (getHeight() - mDrawParams.mActualHeight) / 2 + mDrawParams.mActualHeight, mDrawParams.mPaintLarge);

        mTempDate.setTime(mDayDatas[mDayDatas.length - 1].endTime * 1000L);
        String endText = mHourFormat.format(mTempDate);
        canvas.drawText(endText, offsetX + rectWidth - mDrawParams.mLargeTextWidth / 2.0f, (getHeight() - mDrawParams.mActualHeight) / 2 + mDrawParams.mActualHeight, mDrawParams.mPaintLarge);

        //赋值下，用于事件判断的全局变量(这些在其他地方都可以算的出来，直接赋值，免的其他地方算了)
        mOffsetX = offsetX;
        mOffsetY = offsetY;
        mRectWidth = rectWidth;
        mRectHeight = rectHeight;
        mActiveHeight = activeHeight;

        if (mAnimRectParams != null) {//绘制激活的矩阵
            canvas.drawRect(mAnimRectParams.mCenterX - mAnimRectParams.mRectWidth / 2,
                    getHeight() / 2 - mAnimRectParams.mRectHeight / 2,
                    mAnimRectParams.mCenterX + mAnimRectParams.mRectWidth / 2,
                    getHeight() / 2 + mAnimRectParams.mRectHeight / 2,
                    mDrawParams.getRectPaintWithColor(mAnimRectParams.mRectColor)
            );
            mTempDate.setTime(mAnimRectParams.mStartTime * 1000L);
            startText = mHourFormat.format(mTempDate);
            mTempDate.setTime(mAnimRectParams.mEndTime * 1000L);
            endText = mHourFormat.format(mTempDate);
            String timeText = startText + "-" + endText;

            mDrawParams.mPaintSmall.setAlpha(mAnimRectParams.mTextAlpha);
            canvas.drawText(timeText, mAnimRectParams.mCenterX - mDrawParams.mSmallTextWidth / 2, (getHeight() - mDrawParams.mActualHeight) / 2 + mDrawParams.mTextHeight, mDrawParams.mPaintSmall);
        }
    }

    private class ActiveRectParams {
        float mCenterX;
        float mRectWidth;
        float mRectHeight;
        int mRectColor;
        int mTextAlpha;
        int mStartTime;
        int mEndTime;

    }

    private class SleepDayData {
        /**
         * 代表时间段的开始时间
         */
        private int startTime;
        /**
         * 代表时间段的结束时间
         */
        private int endTime;
        /**
         * 代表睡眠等级
         */
        private int value;
        /**
         * 代表占睡眠时间的百分比
         */
        private float percent;
    }

    /**
     * 设置睡眠数据
     */
    public void setSleepDayDatas(SleepRecord record) {
        SleepDayData[] datas = null;
        if (record != null && record.getDetail() != null
                && record.getDetail().size() > 0) {
            List<SleepItem> items = adjustSleepItems(record.getDetail());

            //Convert SleepItem to SleepDayData
            if (items.size() > 0) {
                datas = new SleepDayData[items.size()];
                int totalTime = (int) ((items.get(items.size() - 1).getEndTime().getTime()
                        - items.get(0).getStartTime().getTime()) / 1000);
                for (int i = 0; i < items.size(); i++) {
                    SleepItem item = items.get(i);
                    SleepDayData data = new SleepDayData();
                    data.value = item.getStatus();
                    data.startTime = (int) (item.getStartTime().getTime() / 1000);
                    data.endTime = (int) (item.getEndTime().getTime() / 1000);
                    data.percent = (data.endTime - data.startTime) / (float) totalTime;
                    datas[i] = data;
                }
            }
        }

        mAnimRectParams = null;
        mAnimIndex = INVALID_POSITION;

        mDayDatas = datas;

        postInvalidate();
    }

    /**
     * Adjust SleepItem to remove the soberness at the beginning and end.
     * And handle the time-staggered data caused by the abnormality, so that the sleep data can be continuously displayed
     */
    private List<SleepItem> adjustSleepItems(@NonNull List<SleepItem> items) {
        Collections.sort(items, new Comparator<SleepItem>() {
            @Override
            public int compare(SleepItem o1, SleepItem o2) {
                return (int) (o1.getStartTime().getTime() - o2.getStartTime().getTime());
            }
        });

        List<SleepItem> resultList = new ArrayList<>(items.size());
        List<SleepItem> endSoberList = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            SleepItem itemData = items.get(i);
            int status = itemData.getStatus();
            if (status == SLEEP_STATUS_SOBER && resultList.size() <= 0) {
                continue;
            }
            if (resultList.size() <= 0) {
                resultList.add(itemData);
                continue;
            }

            long duration = itemData.getEndTime().getTime() - itemData.getStartTime().getTime();
            if (duration > 0) {
                SleepItem previousItemData;
                if (endSoberList.size() > 0) {
                    previousItemData = endSoberList.get(endSoberList.size() - 1);
                } else {
                    previousItemData = resultList.get(resultList.size() - 1);
                }
                itemData.getStartTime().setTime(previousItemData.getEndTime().getTime());
                itemData.getEndTime().setTime(previousItemData.getEndTime().getTime() + duration);
                if (status == SLEEP_STATUS_SOBER) {
                    endSoberList.add(itemData);
                } else {
                    resultList.addAll(endSoberList);
                    endSoberList.clear();
                    resultList.add(itemData);
                }
            }
        }

        return resultList;
    }

    private class DrawParams {

        private static final int MAX_WIDTH = 680;
        private static final int MAX_HEIGHT = 340;

        private static final int VERTICAL_SPACE = 5;//dp垂直方向上，几个元素的距离
        static final float ACTIVE_RATIO = 1.2f;//激活的矩阵与未激活的比例

        int mActualWidth;//防止图形过大，给予的限制，绘制在正中间
        int mActualHeight;//防止图形过大，给予的限制，绘制在正中间

        Paint mPaintLarge;
        Paint mPaintSmall;

        int mSmallTextWidth;
        int mLargeTextWidth;
        int mTextHeight;//因为除了两个文字大小不一样，图形几乎对称，所以这里将两个文字高度统一，作为对称处理，这样容易处理

        int mColor1;
        int mColor2;
        int mColor3;

        Paint mRectPaint;

        float mVerticalSpace;

        Paint getRectPaintWithValue(int value) {
            if (value == SleepItemData.SLEEP_STATUS_DEEP) {
                mRectPaint.setColor(mColor1);
            } else if (value == SleepItemData.SLEEP_STATUS_LIGHT) {
                mRectPaint.setColor(mColor2);
            } else if (value == SLEEP_STATUS_SOBER) {
                mRectPaint.setColor(mColor3);
            }
            return mRectPaint;
        }

        int getRectColorWithValue(int value) {
            if (value == SleepItemData.SLEEP_STATUS_DEEP) {
                return mColor1;
            } else if (value == SleepItemData.SLEEP_STATUS_LIGHT) {
                return mColor2;
            } else if (value == SLEEP_STATUS_SOBER) {
                return mColor3;
            }
            return mColor1;
        }

        Paint getRectPaintWithColor(int color) {
            mRectPaint.setColor(color);
            return mRectPaint;
        }

        public DrawParams() {
            mPaintLarge = new Paint();
            mPaintLarge.setAntiAlias(true);
            mPaintLarge.setDither(true);
            mPaintLarge.setColor(Color.BLACK);
            mPaintLarge.setTextSize(DisplayUtil.sp2px(getContext(), 12));
            mLargeTextWidth = (int) mPaintLarge.measureText("00:00");
            mTextHeight = (int) (-mPaintLarge.getFontMetrics().ascent);

            mPaintSmall = new Paint();
            mPaintSmall.setAntiAlias(true);
            mPaintSmall.setDither(true);
            mPaintSmall.setColor(Color.BLACK);
            mPaintSmall.setTextSize(DisplayUtil.sp2px(getContext(), 10));
            mSmallTextWidth = (int) mPaintSmall.measureText("00:00-00:00");
            //            mSmallTextHeight = (int) (mPaintSmall.getFontMetrics().descent - mPaintSmall.getFontMetrics().ascent);


            mColor1 = getResources().getColor(R.color.sleep_chart_color_deep);
            mColor2 = getResources().getColor(R.color.sleep_chart_color_light);
            mColor3 = getResources().getColor(R.color.sleep_chart_color_sober);

            mRectPaint = new Paint();
            mRectPaint.setAntiAlias(true);
            mRectPaint.setDither(true);

            mVerticalSpace = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, VERTICAL_SPACE, getResources().getDisplayMetrics());
        }

        public void updateSize(int width, int height) {
            //            mActualWidth = width > MAX_WIDTH ? MAX_WIDTH : width;
            //            mActualHeight = height > MAX_HEIGHT ? MAX_HEIGHT : height;
            mActualWidth = width;
            mActualHeight = height;
        }

        boolean isValid() {
            return mActualWidth != 0 && mActualHeight != 0;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mDrawParams.updateSize(width, height);
    }
}
