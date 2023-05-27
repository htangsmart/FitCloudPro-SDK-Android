package com.topstep.fitcloud.sample2.ui.combine.wh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.kilnn.tool.ui.DisplayUtil;
import com.topstep.fitcloud.sample2.R;
import com.topstep.fitcloud.sample2.model.wh.MenstruationResult;
import com.topstep.fitcloud.sample2.model.wh.PregnancyDateType;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WhCalendarView extends View {

    public static final SparseIntArray mWeekDaysRes;

    static {
        mWeekDaysRes = new SparseIntArray();
        mWeekDaysRes.put(Calendar.SUNDAY, R.string.ds_alarm_repeat_06_simple);
        mWeekDaysRes.put(Calendar.MONDAY, R.string.ds_alarm_repeat_00_simple);
        mWeekDaysRes.put(Calendar.TUESDAY, R.string.ds_alarm_repeat_01_simple);
        mWeekDaysRes.put(Calendar.WEDNESDAY, R.string.ds_alarm_repeat_02_simple);
        mWeekDaysRes.put(Calendar.THURSDAY, R.string.ds_alarm_repeat_03_simple);
        mWeekDaysRes.put(Calendar.FRIDAY, R.string.ds_alarm_repeat_04_simple);
        mWeekDaysRes.put(Calendar.SATURDAY, R.string.ds_alarm_repeat_05_simple);
    }

    private Locale mLocale;
    private Date mToday;
    private Paint mPaint;
    private int[] mWeekDays;

    private float mTitleHeight;
    private float mItemHeight;
    private float mItemWidth;
    private float mItemPaddingHorizontal;
    private float mItemPaddingVertical;

    private float mTitleTextSize;
    private float mItemTextSize;

    private Calendar mCalendar;
    private Date mCurrentDate;
    private Date mSelectDate;

    public OnDateSelectListener mListener;

    public WhCalendarView(Context context) {
        super(context);
        init();
    }

    public WhCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WhCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WhCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mLocale = Locale.getDefault();
        mToday = new Date();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        mCalendar = Calendar.getInstance();
        mCurrentDate = new Date();

        mWeekDays = new int[7];
        mWeekDays[0] = mCalendar.getFirstDayOfWeek();
        for (int i = 1; i < mWeekDays.length; i++) {
            mWeekDays[i] = mWeekDays[i - 1] + 1;
            if (mWeekDays[i] > Calendar.SATURDAY) {
                mWeekDays[i] = Calendar.SUNDAY;
            }
        }

        //Temp
        mTempRect = new RectF();
        mItemRoundRadius = DisplayUtil.dip2px(getContext(), 2);
        mTodayMarkRadiusBase = DisplayUtil.dip2px(getContext(), 4);
        mPaintStroke = DisplayUtil.dip2px(getContext(), 1.5f);
        mDefaultItemSize = DisplayUtil.dip2px(getContext(), 36);
        mDefaultItemPadding = DisplayUtil.dip2px(getContext(), 4);

        mOvulationDayDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_wh_legend_ovulation_day);
    }

    private float mDefaultItemSize;
    private float mDefaultItemPadding;

    public interface OnDateSelectListener {
        void onDateSelect(@Nullable Date date);

        /**
         * @param isCurrentMonthNow Is it the current month
         */
        void onMonthChanged(boolean isCurrentMonthNow);
    }

    public void setOnDateSelectListener(OnDateSelectListener listener) {
        mListener = listener;
    }

    public Date getYearMonth() {
        return mCurrentDate;
    }

    public void gotoToday() {
        setYearMonth(mToday);
        if (mSelectDate == null || !dateEquals(mSelectDate, mToday)) {
            mSelectDate = mToday;
            if (mListener != null) {
                mListener.onDateSelect(mSelectDate);
            }
        }
    }

    public void gotoTodayForce() {
        setYearMonth(mToday);
        mSelectDate = mToday;
        if (mListener != null) {
            mListener.onDateSelect(mSelectDate);
        }
    }

    public void setYearMonth(Date date) {
        boolean isMonthChanged = !monthEquals(mCurrentDate, date);
        mCurrentDate = date;
        invalidate();
        if (isMonthChanged && mListener != null) {
            mListener.onMonthChanged(monthEquals(mCurrentDate, mToday));
        }
    }

    public void previousMonth() {
        mCalendar.setTime(mCurrentDate);
        mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) - 1);
        setYearMonth(mCalendar.getTime());
    }

    public void nextMonth() {
        mCalendar.setTime(mCurrentDate);
        mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) + 1);
        setYearMonth(mCalendar.getTime());
    }

    @Nullable
    public Date getSelectDate() {
        return mSelectDate;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float width;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
            calculateActualWidthParam(width);
        } else {
            mItemWidth = getItemWidth();
            mItemPaddingHorizontal = getItemPaddingHorizontalRatio() > 0 ? mItemWidth * getItemPaddingHorizontalRatio() : getItemPaddingHorizontalSize();
            width = mItemWidth * 7 + mItemPaddingHorizontal * 8;
            if (widthMode == MeasureSpec.AT_MOST
                    && width > widthSize) {
                width = widthSize;
                calculateActualWidthParam(width);
            }
        }

        float height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
            calculateActualHeightParam(height);
        } else {
            mItemHeight = getItemHeight();
            mTitleHeight = getTitleHeightRatio() > 0 ? mItemHeight * getTitleHeightRatio() : getTitleHeightSize();
            mItemPaddingVertical = getItemPaddingVerticalRatio() > 0 ? mItemHeight * getItemPaddingVerticalRatio() : getItemPaddingVerticalSize();
            height = mTitleHeight + mItemHeight * 6 + mItemPaddingVertical * 8;
            if (heightMode == MeasureSpec.AT_MOST
                    && height > heightSize) {
                height = heightSize;
                calculateActualHeightParam(height);
            }
        }
        setMeasuredDimension((int) width, (int) height);

        mTitleTextSize = Math.min(mTitleHeight, mItemWidth) * 0.4f;
        mItemTextSize = Math.min(mItemHeight, mItemWidth) * 0.4f;
    }

    private void calculateActualWidthParam(float width) {
        float paddingRatio = getItemPaddingHorizontalRatio();
        if (paddingRatio > 0) {
            float count = 7 + 8 * paddingRatio;
            mItemWidth = width / count;
            mItemPaddingHorizontal = mItemWidth * paddingRatio;
        } else {
            mItemPaddingHorizontal = getItemPaddingHorizontalSize();
            mItemWidth = (width - 8 * mItemPaddingHorizontal) / 7;
        }
    }

    private void calculateActualHeightParam(float height) {
        float titleRatio = getTitleHeightRatio();
        float paddingRatio = getItemPaddingVerticalRatio();

        float remainingCount = 6 + (titleRatio > 0 ? titleRatio : 0) + (paddingRatio > 0 ? 8 * paddingRatio : 0);
        float remainingSize = height - (titleRatio > 0 ? 0 : getTitleHeightSize()) + (paddingRatio > 0 ? 0 : 8 * getItemPaddingVerticalSize());

        mItemHeight = remainingSize / remainingCount;
        mTitleHeight = titleRatio > 0 ? mItemHeight * titleRatio : getTitleHeightSize();
        mItemPaddingVertical = paddingRatio > 0 ? mItemHeight * paddingRatio : getItemPaddingVerticalSize();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTitle(canvas);
        calculateDate();

        mPaint.setTextSize(mItemTextSize);
        for (int i = 0; i < 7 * 6; i++) {
            int row = i / 7;
            int column = i % 7;
            drawDate(canvas, row, column, i >= mCurrentMonthStartIndex && i <= mCurrentMonthEndIndex);
            mCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH) + 1);
        }
    }

    private int mCurrentMonthStartIndex;
    private int mCurrentMonthEndIndex;

    private void calculateDate() {
        mCalendar.setTime(mCurrentDate);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        for (int i = 0; i < mWeekDays.length; i++) {
            if (dayOfWeek == mWeekDays[i]) {
                mCurrentMonthStartIndex = i;
                break;
            }
        }
        mCurrentMonthEndIndex = mCurrentMonthStartIndex + mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) - 1;
        mCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH) - mCurrentMonthStartIndex);
    }

    private float mDownX;
    private float mDownY;
    private Date mDownDate;

    private void checkSelectedDate(float x, float y) {
        mDownX = x;
        mDownY = y;

        float firstXStart = mItemPaddingHorizontal;
        float firstYStart = mTitleHeight + mItemPaddingVertical * 2;
        if (x < firstXStart || y < firstYStart) {
            return;
        }

        int row = (int) ((y - firstYStart) / (mItemHeight + mItemPaddingVertical));
        boolean inItem = ((int) (y - firstYStart)) % ((int) ((mItemHeight + mItemPaddingVertical))) > mItemPaddingVertical;
        if (!inItem) {
            return;
        }

        int column = (int) ((x - firstXStart) / (mItemWidth + mItemPaddingHorizontal));
        inItem = ((int) (x - firstXStart)) % ((int) (mItemWidth + mItemPaddingHorizontal)) > mItemPaddingHorizontal;
        if (!inItem) {
            return;
        }

        int downIndex = row * 7 + column;
        if (downIndex < mCurrentMonthStartIndex || downIndex > mCurrentMonthEndIndex) {
            return;
        }
        mCalendar.setTime(mCurrentDate);
        mCalendar.set(Calendar.DAY_OF_MONTH, downIndex - mCurrentMonthStartIndex + 1);
        mDownDate = mCalendar.getTime();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                checkSelectedDate(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                if (mDownDate != null) {
                    double distance = Math.sqrt(Math.pow(Math.abs(event.getX()) - Math.abs(mDownX), 2)
                            + Math.pow(Math.abs(event.getY()) - Math.abs(mDownY), 2));
                    if (distance < 30) {
                        if (mSelectDate == null || !dateEquals(mSelectDate, mDownDate)) {
                            mSelectDate = mDownDate;
                            if (mListener != null) {
                                mListener.onDateSelect(mSelectDate);
                            }
                        }
                    }
                    mDownDate = null;
                    invalidate();
                }
                break;
        }
        return true;
    }

    /**
     * When the view has no definite width, the item width cannot be calculated, use this parameter as the item width
     */
    protected float getItemWidth() {
        return mDefaultItemSize;
    }

    /**
     * When the view has no definite height, the item height cannot be calculated, use this parameter as the item height
     */
    protected float getItemHeight() {
        return mDefaultItemSize;
    }

    /**
     * Get the ratio of of head Item height to ItemHeight. The valid value is [0-1].
     * If a negative number is returned, it means that this method is invalid, please use {@link #getTitleHeightSize()}
     */
    protected float getTitleHeightRatio() {
        return 1;
    }

    /**
     * Get the head Item height
     */
    protected float getTitleHeightSize() {
        return 0;
    }

    /**
     * Get the ratio of the horizontal padding of each Item to the width of the Item. The valid value is [0-1].
     * If a negative number is returned, it means this method is invalid, please use {@link #getItemPaddingHorizontalSize()}
     */
    protected float getItemPaddingHorizontalRatio() {
        return -1;
    }

    /**
     * Get the horizontal padding of each Item
     *
     * @return unit px
     */
    protected float getItemPaddingHorizontalSize() {
        return mDefaultItemPadding;
    }

    /**
     * Get the ratio of the vertical padding of each Item to the height of the Item. The valid value is [0-1].
     * If a negative number is returned, it means this method is invalid, please use {@link #getItemPaddingVerticalSize()}
     */
    protected float getItemPaddingVerticalRatio() {
        return -1;
    }

    /**
     * Get the vertical padding of each Item
     *
     * @return unit px
     */
    protected float getItemPaddingVerticalSize() {
        return mDefaultItemPadding;
    }

    @SuppressWarnings("deprecation")
    private boolean dateEquals(Date date1, Date date2) {
        return date1.getYear() == date2.getYear()
                && date1.getMonth() == date2.getMonth()
                && date1.getDate() == date2.getDate();
    }

    @SuppressWarnings("deprecation")
    private boolean monthEquals(Date date1, Date date2) {
        return date1.getYear() == date2.getYear()
                && date1.getMonth() == date2.getMonth();
    }

    private float getTextCenterBaseY(float height, Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return height / 2.0f - fm.descent + (fm.bottom - fm.top) / 2.0f;
    }

    private float getTextCenterBaseX(float width, Paint paint, String text) {
        return (width - paint.measureText(text)) / 2.0f;
    }

    private void drawTitle(Canvas canvas) {
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(mTitleTextSize);
        float titleTextBaseLine = getTextCenterBaseY(mTitleHeight, mPaint);
        for (int i = 0; i < mWeekDays.length; i++) {
            String text = getResources().getString(mWeekDaysRes.get(mWeekDays[i]));
            float offsetX = i * (mItemWidth + mItemPaddingHorizontal);
            float textX = offsetX + getTextCenterBaseX(mItemWidth, mPaint, text);
            canvas.drawText(text, textX, titleTextBaseLine, mPaint);
        }
    }

    private RectF mTempRect;
    private float mItemRoundRadius;
    private float mTodayMarkRadiusBase;
    private float mPaintStroke;

    private DateHolder mHolder;
    private Drawable mOvulationDayDrawable;

    public interface DateHolder {
        boolean isPregnancyMode();

        @PregnancyDateType
        @Nullable
        Integer getPregnancyDateType(@NonNull Date date);

        @MenstruationResult.DateType
        @Nullable
        Integer getMenstruationDateType(@NonNull Date date);
    }

    public void setDataHolder(DateHolder holder) {
        mHolder = holder;
    }

    private void drawDate(Canvas canvas, int row, int column, boolean isCurrentMonth) {
        String text = String.format(mLocale, "%d", mCalendar.get(Calendar.DAY_OF_MONTH));

        float offsetX = mItemPaddingHorizontal + column * (mItemWidth + mItemPaddingHorizontal);
        float textX = offsetX + getTextCenterBaseX(mItemWidth, mPaint, text);

        float offsetY = mTitleHeight + mItemPaddingVertical * 2 + row * (mItemHeight + mItemPaddingVertical);
        float textBaseLine = getTextCenterBaseY(mItemHeight, mPaint);
        float textY = offsetY + textBaseLine;

        mTempRect.set(offsetX, offsetY, offsetX + mItemWidth, offsetY + mItemHeight);
        mPaint.setColor(isCurrentMonth ? Color.WHITE : 0xFFFCFCFC);
        canvas.drawRoundRect(mTempRect, mItemRoundRadius, mItemRoundRadius, mPaint);
        if (!isCurrentMonth) return;

        Date date = mCalendar.getTime();

        if (mHolder == null) {
            mPaint.setColor(Color.BLACK);
        } else if (mHolder.isPregnancyMode()) {
            Integer type = mHolder.getPregnancyDateType(date);
            if (type == null) {
                mPaint.setColor(Color.BLACK);
            } else {
                switch (type) {
                    case PregnancyDateType.EARLY:
                        mPaint.setColor(getResources().getColor(R.color.wh_pregnancy_color_early));
                        break;
                    case PregnancyDateType.MIDDLE:
                        mPaint.setColor(getResources().getColor(R.color.wh_pregnancy_color_middle));
                        break;
                    case PregnancyDateType.LATER:
                        mPaint.setColor(getResources().getColor(R.color.wh_pregnancy_color_later));
                        break;
                    default:
                        mPaint.setColor(Color.BLACK);
                        break;
                }
            }
        } else {
            Integer type = mHolder.getMenstruationDateType(date);
            if (type == null) {
                mPaint.setColor(Color.BLACK);
            } else {
                switch (type) {
                    case MenstruationResult.DateType.MENSTRUATION:
                        mPaint.setColor(getResources().getColor(R.color.wh_menstruation_color_menstruation));
                        canvas.drawRoundRect(mTempRect, mItemRoundRadius, mItemRoundRadius, mPaint);
                        mPaint.setColor(Color.WHITE);
                        break;
                    case MenstruationResult.DateType.SAFE_BEFORE_OVULATION:
                    case MenstruationResult.DateType.SAFE_AFTER_OVULATION:
                        mPaint.setColor(getResources().getColor(R.color.wh_menstruation_color_safe));
                        break;
                    case MenstruationResult.DateType.OVULATION:
                        mPaint.setColor(getResources().getColor(R.color.wh_menstruation_color_ovulation));
                        break;
                    case MenstruationResult.DateType.OVULATION_DAY: {
                        float drawableWidth = mOvulationDayDrawable.getIntrinsicWidth();
                        float drawableHeight = mOvulationDayDrawable.getIntrinsicHeight();
                        float textWidth = mPaint.measureText(text);
                        Paint.FontMetrics fm = mPaint.getFontMetrics();
                        float textHeight = -fm.descent + (fm.bottom - fm.top) / 2.0f;

                        float remainingWidth = (mItemWidth - textWidth) / 2.0f;
                        float left = textX + textWidth + (remainingWidth - drawableWidth) / 2.0f;

                        float remainingHeight = textBaseLine - textHeight;
                        float top = offsetY + (remainingHeight - drawableHeight) / 2.0f;

                        mOvulationDayDrawable.setBounds((int) left, (int) top, (int) (left + drawableWidth), (int) (top + drawableHeight));
                        mOvulationDayDrawable.draw(canvas);
                        mPaint.setColor(getResources().getColor(R.color.wh_menstruation_color_ovulation));
                    }
                    break;
                    default:
                        mPaint.setColor(Color.BLACK);
                        break;
                }
            }
        }
        canvas.drawText(text, textX, textY, mPaint);

        if (dateEquals(date, mToday)) {
            mPaint.setColor(Color.RED);
            float markRadius = Math.min(mItemWidth / 6.0f, (mItemHeight - textBaseLine) / 6.0f);
            markRadius = Math.min(mTodayMarkRadiusBase, markRadius);
            canvas.drawCircle(offsetX + mItemWidth / 2.0f, textY + (mItemHeight - textBaseLine) / 2.0f, markRadius, mPaint);
        }
        if (mSelectDate != null && dateEquals(date, mSelectDate)) {
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mPaintStroke);
            canvas.drawRoundRect(mTempRect, mItemRoundRadius, mItemRoundRadius, mPaint);
            //reset
            mPaint.setStyle(Paint.Style.FILL);
        }
    }

}
