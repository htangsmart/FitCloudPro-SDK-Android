package com.github.kilnn.wristband2.sample.syncdata;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import com.github.kilnn.wristband2.sample.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Kilnn on 2017/5/8.
 */

public class EcgView extends View {
    private static final String TAG = "EcgView";

    public interface OnEcgClickListener {
        void onClick();
    }

    public EcgView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public EcgView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public EcgView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EcgView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    public static final int MODE_NORMAL = 1;//正常模式，展示数据，允许滑动
    public static final int MODE_REALTIME = 2;//展示实时数据，不允许滑动
    public static final int MODE_PLAYBACK = 3;//回放数据，不允许滑动

    @IntDef({MODE_NORMAL, MODE_REALTIME, MODE_PLAYBACK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    private static final int DEFAULT_LINE_COLOR = Color.RED;
    private static final int DEFAULT_LINE_WIDTH = 2;//2dp
    private static final int DEFAULT_GRID_WIDTH = 5;//5dp
    private static final int DEFAULT_SAMPLING_RATE = 4;//默认4ms，即一个格子包含了10个点

    private static final int DEFAULT_REAL_TIME_REFRESH_INTERVAL = 400;//400ms
    private static final int DEFAULT_PLAY_BACK_REFRESH_INTERVAL = 400;//400ms

    private List<Integer> mEcgDatas;//数据点
    private int mEcgMaxValue;//心电值的最大值
    private int mEcgMinValue;//心电值的最小值
    private boolean mEcgAutoScope;//是否自动确定范围
    private int mRealTimeRefreshInterval = 0;
    private int mPlayBackRefreshInterval = 0;

    private int mSamplingRate;
    private float mPointGap;//每个点中间间隔多少个像素
    private boolean mNewDataAlignLeft;//新的数据是否绘制在左边
    private boolean mLessDataAlignLeft;//较少的数据（少于一屏）时，是否绘制在左边
    private Path mPath;
    private Paint mPaint;

    @Mode private volatile int mMode = MODE_NORMAL;

    private OverScroller mScroller;
    private EdgeEffect mEdgeGlowLeft;
    private EdgeEffect mEdgeGlowRight;

    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mOverScrollDistance;
    private int mOverFlingDistance;

    private int mDefaultMaxValue = 5120;
    private int mDefaultMinValue = 0;

    private float mGridWidth;

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mEcgDatas = new ArrayList<>(10000);

        //set default value
        mEcgAutoScope = false;

        mRealTimeRefreshInterval = DEFAULT_REAL_TIME_REFRESH_INTERVAL;
        mPlayBackRefreshInterval = DEFAULT_PLAY_BACK_REFRESH_INTERVAL;

        int lineColor = DEFAULT_LINE_COLOR;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float lineWidth = DEFAULT_LINE_WIDTH * metrics.density;
        mGridWidth = DEFAULT_GRID_WIDTH * metrics.density;
        mSamplingRate = DEFAULT_SAMPLING_RATE;
        mNewDataAlignLeft = true;
        mLessDataAlignLeft = true;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EcgView, defStyleAttr, 0);

            mDefaultMaxValue = a.getInt(R.styleable.EcgView_ecg_data_max_value, mDefaultMaxValue);
            mDefaultMinValue = a.getInt(R.styleable.EcgView_ecg_data_min_value, mDefaultMinValue);
            if (mDefaultMaxValue <= mDefaultMinValue) {
                mDefaultMaxValue = mDefaultMinValue + 1;//防止出错
            }
            mEcgAutoScope = a.getBoolean(R.styleable.EcgView_ecg_data_auto_scope, mEcgAutoScope);

            mRealTimeRefreshInterval = a.getInt(R.styleable.EcgView_ecg_real_time_refresh_interval, mRealTimeRefreshInterval);
            mPlayBackRefreshInterval = a.getInt(R.styleable.EcgView_ecg_play_back_refresh_interval, mPlayBackRefreshInterval);

            lineColor = a.getColor(R.styleable.EcgView_ecg_line_color, lineColor);
            lineWidth = a.getDimension(R.styleable.EcgView_ecg_line_width, lineWidth);
            mGridWidth = a.getDimension(R.styleable.EcgView_ecg_grid_width, mGridWidth);
            mSamplingRate = a.getInt(R.styleable.EcgView_ecg_sampling_rate, mSamplingRate);
            mNewDataAlignLeft = a.getBoolean(R.styleable.EcgView_ecg_new_data_align_left, mNewDataAlignLeft);
            mLessDataAlignLeft = a.getBoolean(R.styleable.EcgView_ecg_less_data_align_left, mLessDataAlignLeft);
            a.recycle();
        }

        mEcgMaxValue = mDefaultMaxValue;
        mEcgMinValue = mDefaultMinValue;

        mPointGap = mGridWidth / (40.0f / mSamplingRate);//心电图一个格子代表40ms
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(lineColor);
        mPaint.setStrokeWidth(lineWidth);
        CornerPathEffect cornerPathEffect = new CornerPathEffect(200);
        mPaint.setPathEffect(cornerPathEffect);

        mScroller = new OverScroller(context);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverScrollDistance = configuration.getScaledOverscrollDistance();
        mOverFlingDistance = configuration.getScaledOverflingDistance();

        //禁用硬件加速，防止Path太大，不能绘制
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    //    /**
//     * 是否正在回放
//     */
//    public boolean isPlayBack() {
//        return mMode == MODE_PLAYBACK;
//    }
//
//    public void togglePlayBack() {
//        if (isPlayBack()) {
//            stopPlayBack();
//        } else {
//            startPlayBack();
//        }
//    }
//
//    public void stopPlayBack() {
//        mMode = MODE_NORMAL;
//        if (mOnPlayBackListener != null) {
//            mOnPlayBackListener.onStopPlayBack();
//        }
//    }
//
//    public void startPlayBack() {
//        mMode = MODE_PLAYBACK;
//        if (mOnPlayBackListener != null) {
//            mOnPlayBackListener.onStartPlayBack();
//        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                stopPlayBack();
//            }
//        }, 2000);
//    }
//
    public interface OnPlayBackListener {
        void onStartPlayBack();

        void onStopPlayBack();
    }

    private OnPlayBackListener mOnPlayBackListener;

    public void setOnPlayBackListener(OnPlayBackListener listener) {
        mOnPlayBackListener = listener;
    }
//
//    public boolean isRealTime() {
//        return mMode == MODE_REALTIME;
//    }
//
//    public void startRealTime() {
//        mEcgDatas.clear();
//        invalidate();
//        startWave();
//    }
//
//    public void stopRealTime() {
//        stopWave();
//    }
//
//    /**
//     * 回到常规模式，但是不清楚数据
//     */
//    public void setNormal() {
//        mMode = MODE_NORMAL;
//        invalidate();
//    }
//
//    /**
//     * 回到常规模式，并重新设置新的数据
//     */
//    public void resetNormal(int[] newDatas) {
//        mMode = MODE_NORMAL;
//        mEcgDatas.clear();
//        addData(newDatas);
//        invalidate();
//    }

    private void autoScope(int data) {
        mEcgMaxValue = data > mEcgMaxValue ? data : mEcgMaxValue;
        mEcgMinValue = data < mEcgMinValue ? data : mEcgMinValue;
    }

    public void clearData() {
        mEcgDatas.clear();
        //reset mScrollX
        mScrollX = 0;
        if (mEcgAutoScope) {
            mEcgMaxValue = mDefaultMaxValue;
            mEcgMinValue = mDefaultMinValue;
        }
        invalidate();
    }

    /**
     * 添加一个数据
     *
     * @param data 1个心电数据
     */
    public void addData(int data) {
        mEcgDatas.add(data);
        if (mEcgAutoScope) {
            autoScope(data);
        }
        if (mMode == MODE_NORMAL) {
            invalidate();
        }
    }

    /**
     * 添加一组数据
     *
     * @param datas 1组心电数据
     */
    public void addData(int[] datas) {
        if (datas == null) return;
        if (mEcgAutoScope) {
            for (int d : datas) {
                mEcgDatas.add(d);
                autoScope(d);
            }
        } else {
            for (int d : datas) {
                mEcgDatas.add(d);
            }
        }
        if (mMode == MODE_NORMAL) {
            invalidate();
        }
    }

    /**
     * 添加一组数据
     *
     * @param datas 1组心电数据
     */
    public void addDataAndScrollToLast(int[] datas) {
        if (datas == null) return;
        if (mEcgAutoScope) {
            for (int d : datas) {
                mEcgDatas.add(d);
                autoScope(d);
            }
        } else {
            for (int d : datas) {
                mEcgDatas.add(d);
            }
        }
        post(new Runnable() {
            @Override
            public void run() {
                if (getWidth() != 0 && getHeight() != 0) {
                    if (mMode == MODE_NORMAL) {
                        if (mNewDataAlignLeft) {
                            mScrollX = 0;
                        } else {
                            mScrollX = getScrollRange();
                        }
                        invalidate();
                    }
                } else {
                    post(this);
                }
            }
        });
    }

    /**
     * 手动滑动到最后一个数据
     * //TODO 当视图未完全加载时候，也许会有问题，多测试一下
     */
    public void scrollToLastData() {
        post(new Runnable() {
            @Override
            public void run() {
                if (mMode == MODE_NORMAL) {
                    if (mNewDataAlignLeft) {
                        mScrollX = 0;
                    } else {
                        mScrollX = getScrollRange();
                    }
                    invalidate();
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopRefreshThreadIfNecessary();
    }

    private void startRealTimeRefreshThread() {
        stopRefreshThreadIfNecessary();
        if (mMode != MODE_REALTIME) return;
        mRealTimeRefreshThread = new RealTimeRefreshThread();
        mRealTimeRefreshThread.start();
    }

    private void startPlayBackRefreshThread() {
        stopRefreshThreadIfNecessary();
        if (mMode != MODE_PLAYBACK) return;
        mPlayBackRefreshThread = new PlayBackRefreshThread();
        mPlayBackRefreshThread.start();
    }

    private void stopRefreshThreadIfNecessary() {
        if (mRealTimeRefreshThread != null && mRealTimeRefreshThread.isAlive()) {
            mRealTimeRefreshThread.interrupt();
        }
        mRealTimeRefreshThread = null;
        if (mPlayBackRefreshThread != null && mPlayBackRefreshThread.isAlive()) {
            mPlayBackRefreshThread.interrupt();
        }
        mPlayBackRefreshThread = null;
    }

    private RealTimeRefreshThread mRealTimeRefreshThread;
    private PlayBackRefreshThread mPlayBackRefreshThread;

    private class RealTimeRefreshThread extends Thread {
        @Override
        public void run() {
            while (mMode == MODE_REALTIME) {
                postInvalidate();
                try {
                    Thread.sleep(mRealTimeRefreshInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private volatile int mPlayBackRefreshCount = 0;//回放刷新的次数

    private class PlayBackRefreshThread extends Thread {
        @Override
        public void run() {
            mPlayBackRefreshCount = 0;
            while (mMode == MODE_PLAYBACK) {
                postInvalidate();
                try {
                    Thread.sleep(mPlayBackRefreshInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mPlayBackRefreshCount++;
            }
        }
    }

    //外部接口
    public int getMode() {
        return mMode;
    }

    public void setMode(@Mode int mode) {
        if (mMode == mode) return;
        //如果之前是回放模式，那么通知结束回放模式
        if (mMode == MODE_PLAYBACK && mOnPlayBackListener != null) {
            mOnPlayBackListener.onStopPlayBack();
        }

        mMode = mode;
        stopRefreshThreadIfNecessary();
        mScrollX = 0;        //reset mScrollX

        if (mMode == MODE_NORMAL) {
            if (!mNewDataAlignLeft) {//Auto Scroll to the last data
                mScrollX = getScrollRange();
            }
            invalidate();
        } else if (mMode == MODE_PLAYBACK) {
            startPlayBackRefreshThread();
        } else {
            startRealTimeRefreshThread();
        }
    }

    public void setSampleRate(int rate) {
        if (rate <= 0) {
//            rate = EcgRecord.DEFAULT_SAMPLE_BASE;//default
            rate = 100;//default
        }
        mSamplingRate = 1000 / rate;//因为外部设置的为采样率，EcgView内部使用的是每隔多少毫秒一个点。正好相反，所以用1000ms除一下
        mPointGap = mGridWidth / (40.0f / mSamplingRate);
        invalidate();
    }

    //视图绘制
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (mMode) {
            case MODE_NORMAL:
                drawNormal(canvas);
                break;
            case MODE_PLAYBACK:
                drawPlayBack(canvas);
                break;
            case MODE_REALTIME:
                drawRealTime(canvas);
                break;
        }
    }

    private void drawNormal(Canvas canvas) {
        drawLimitEcgDatas(canvas, mEcgDatas.size());
    }

    private void drawPlayBack(Canvas canvas) {
        //最开始的时候，不绘制，给心电图一个进入的感觉。
        if (mPlayBackRefreshCount == 0) {
            if (mOnPlayBackListener != null) {
                mOnPlayBackListener.onStartPlayBack();
            }
            return;
        }

        //每次刷新一次移动多少个点，根据采样率和刷新间隔时间确定
        int perPointCount = Math.max(mPlayBackRefreshInterval / mSamplingRate, 1);

        int limit = perPointCount * mPlayBackRefreshCount;

        boolean playBackDelayFinish = false;
        if (limit >= mEcgDatas.size()) {
            limit = mEcgDatas.size();
            playBackDelayFinish = true;
        }

        if (!mNewDataAlignLeft) {
            mScrollX = getScrollRangeLimit(limit);
        }
        drawLimitEcgDatas(canvas, limit);

        if (playBackDelayFinish) { //finish play back
            setMode(MODE_NORMAL);//切换到普通模式
            if (mOnPlayBackListener != null) {
                mOnPlayBackListener.onStopPlayBack();
            }
        }
    }

    private void drawRealTime(Canvas canvas) {
        if (!mNewDataAlignLeft) {
            mScrollX = getScrollRange();
        }
        drawNormal(canvas);
    }

    /**
     * @param canvas
     * @param limit  限制可话点的数量，如mEcgDatas的Size是100，limit是50，那么只允许话前面50个点。
     */
    private void drawLimitEcgDatas(Canvas canvas, int limit) {
        if (limit > mEcgDatas.size()) {
            limit = mEcgDatas.size();
        }
        if (limit == 0) return;

        float pointsWidth = (limit - 1) * mPointGap;//所有点占的宽度，因为是两个点才有1个间隔，所以要减1
        if (pointsWidth < getWidth()) {//不可以滑动，数据小于一个屏幕
            int count = limit;//点的多少就是limit
            int startIndex;
            //数据小于一屏，那么肯定是从第一个点或者最后一个点开始绘制
            if (mNewDataAlignLeft) {
                startIndex = limit - 1;
            } else {
                startIndex = 0;
            }

            float offset;
            //主要就是计算offset，来觉得绘制的点在左边还是右边
            if (mLessDataAlignLeft) {
                offset = 0;
            } else {
                offset = getWidth() - pointsWidth;
            }

            drawRangeEcgDatas(canvas, startIndex, offset, count);
        } else {//数据大于一个屏幕
            int startX = mScrollX;//draw start x
            int endX = mScrollX + getWidth();//draw end x
            // 最左边点的位置要<=startX ,最右边点的位置要>=endX，这样才能显示完整的可是区域，并且点的数量正好合适
            int leftIndex = (int) Math.floor(startX / mPointGap);
            int rightIndex = (int) Math.ceil(endX / mPointGap);

            //leftIndex和rightIndex是可以绘制的点序号范围。
            //要注意，leftIndex可能小于0(因为overscroll的原因, mScrollX 可能小于0)
            //rightIndex可能大于mEcgDatas的size
            if (leftIndex < 0) leftIndex = 0;
            if (rightIndex > limit - 1) rightIndex = limit - 1;

            //因为绘制的方式始终是从左边的点连接到右边，例如leftIndex=10,rightIndex=30，mEcgDatas.size=100，

            //如果mNewDataAlignLeft=true,那么leftIndex代表倒数第10个点，即startIndex=(mEcgDatas.size-1-leftIndex)=89,绘制时，index--来连接点。
            //如果mNewDataAlignLeft=false,那么leftIndex代表第10个点，即startIndex=leftIndex=10,绘制时，index++来连接点。
            //绘制点的数量count=rightIndex-leftIndex+1（+1是因为10-30这是21个点）

            int startIndex;
            if (mNewDataAlignLeft) {
                startIndex = limit - 1 - leftIndex;
            } else {
                startIndex = leftIndex;
            }
            int count = rightIndex - leftIndex + 1;

            //因为视图从最左边的点开始绘制，所以最左边的点超出了屏幕多少
            float offset = leftIndex * mPointGap - mScrollX;

            drawRangeEcgDatas(canvas, startIndex, offset, count);
        }
    }

    /**
     * @param canvas
     * @param startIndex 从哪个点开始绘制
     * @param offset     开始绘制偏移多少
     * @param count      绘制点的数量
     */
    private void drawRangeEcgDatas(Canvas canvas, int startIndex, float offset, int count) {
//        if (BuildConfig.DEBUG) {
//            Log.e(TAG, "startIndex:" + startIndex + "   count:" + count + "   offset:" + offset);
//        }
        mPath.reset();
        mPath.moveTo(offset, getPointY(mEcgDatas.get(startIndex)));
        if (count > 1) {
            if (mNewDataAlignLeft) {
                //因为新的数据在左边，所以startIndex代表mEcgDatas后面的数据
                for (int i = startIndex - 1; i > startIndex - count; i--) {
                    mPath.lineTo(offset + mPointGap * (startIndex - i), getPointY(mEcgDatas.get(i)));
                }
            } else {
                //因为新的数据在右边边，所以startIndex代表mEcgDatas前面的数据
                for (int i = startIndex + 1; i < startIndex + count; i++) {
                    mPath.lineTo(offset + mPointGap * (i - startIndex), getPointY(mEcgDatas.get(i)));
                }
            }
        }
        canvas.drawPath(mPath, mPaint);
    }

    private float getPointY(int value) {
        return (1 - (value - mEcgMinValue) / (float) (mEcgMaxValue - mEcgMinValue)) * getHeight();
    }

    //以下部分为事件处理
    private int mScrollX;

    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    /**
     * True if the user is currently dragging this ScrollView around. This is
     * not the same as 'is being flinged', which can be checked by
     * mScroller.isFinished() (flinging begins when the user lifts his finger).
     */
    private boolean mIsBeingDragged = false;

    /**
     * Position of the last motion event.
     */
    private int mLastMotionX;
    private int mLastMotionY;

    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;


    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private int getScrollRangeLimit(int limit) {
        int scrollRange = 0;
        if (limit > 0) {
            scrollRange = (int) Math.max(0, (limit - 1) * mPointGap - getWidth());
        }
        return scrollRange;
    }

    private int getScrollRange() {
        int scrollRange = 0;
        if (mEcgDatas.size() > 0) {
            scrollRange = (int) Math.max(0, (mEcgDatas.size() - 1) * mPointGap - getWidth());
        }
        return scrollRange;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mMode != MODE_NORMAL) return false;

        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if ((mIsBeingDragged = !mScroller.isFinished())) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }

                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                // Remember where the motion event started
                mLastMotionX = (int) ev.getX();
                mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                    break;
                }

                final int x = (int) ev.getX(activePointerIndex);
                int deltaX = mLastMotionX - x;
                if (!mIsBeingDragged && Math.abs(deltaX) > mTouchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                    if (deltaX > 0) {
                        deltaX -= mTouchSlop;
                    } else {
                        deltaX += mTouchSlop;
                    }
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionX = x;

                    final int oldX = mScrollX;
                    final int range = getScrollRange();
                    final int overscrollMode = getOverScrollMode();
                    final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS ||
                            (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);

                    if (overScrollBy(deltaX, 0, mScrollX, 0,
                            range, 0, mOverScrollDistance, 0, true)) {
                        // Break our velocity if we hit a scroll barrier.
                        mVelocityTracker.clear();
                    }
//                    onScrollChanged(mScrollX, 0, oldX, 0);//TODO 不需要ScrollListener回调

                    if (canOverscroll) {
                        final int pulledToX = oldX + deltaX;
                        if (pulledToX < 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                mEdgeGlowLeft.onPull((float) deltaX / getWidth(),
                                        1.f - ev.getY(activePointerIndex) / getHeight());
                            } else {
                                mEdgeGlowLeft.onPull((float) deltaX / getWidth());
                            }
                            if (!mEdgeGlowRight.isFinished()) {
                                mEdgeGlowRight.onRelease();
                            }
                        } else if (pulledToX > range) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                mEdgeGlowRight.onPull((float) deltaX / getWidth(),
                                        ev.getY(activePointerIndex) / getHeight());
                            } else {
                                mEdgeGlowRight.onPull((float) deltaX / getWidth());
                            }
                            if (!mEdgeGlowLeft.isFinished()) {
                                mEdgeGlowLeft.onRelease();
                            }
                        }
                        if (mEdgeGlowLeft != null
                                && (!mEdgeGlowLeft.isFinished() || !mEdgeGlowRight.isFinished())) {
                            postInvalidateOnAnimation();
                        }

                    }
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);

                    if (mEcgDatas.size() > 0) {
                        if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                            fling(-initialVelocity);
                        } else {
                            if (mScroller.springBack(mScrollX, 0, 0, getScrollRange(), 0, 0)) {
                                postInvalidateOnAnimation();
                            }
                        }
                    }

                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                } else {
                    final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                    if (activePointerIndex == -1) {
                        Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                        break;
                    }
                    int deltaX = Math.abs((int) ev.getX(activePointerIndex) - mLastMotionX);
                    int deltaY = Math.abs((int) ev.getY(activePointerIndex) - mLastMotionY);
                    if (deltaX < mTouchSlop && deltaY < mTouchSlop) {
                        if (mEcgClickListener != null) {
                            mEcgClickListener.onClick();
                        }
                    }
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL: {
                if (mIsBeingDragged && mEcgDatas.size() > 0) {
                    if (mScroller.springBack(mScrollX, 0, 0, getScrollRange(), 0, 0)) {
                        postInvalidateOnAnimation();
                    }
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
            }
            break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionX = (int) ev.getX(index);
                mLastMotionY = (int) ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
            }
            break;
            case MotionEvent.ACTION_POINTER_UP: {
                onSecondaryPointerUp(ev);
                mLastMotionX = (int) ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = (int) ev.getY(ev.findPointerIndex(mActivePointerId));
            }
            break;
        }
        return true;
    }

    /**
     * Fling the scroll view
     *
     * @param velocityX The initial velocity in the X direction. Positive
     *                  numbers mean that the finger/cursor is moving down the screen,
     *                  which means we want to scroll towards the top.
     */
    public void fling(int velocityX) {
        if (mEcgDatas.size() > 0) {
            int width = getWidth();
            int right = (int) ((mEcgDatas.size() - 1) * mPointGap);
            mScroller.fling(mScrollX, 0, velocityX, 0, 0, Math.max(0, right - width), 0, 0, width / 2, 0);
            postInvalidateOnAnimation();
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int oldX = mScrollX;
            int x = mScroller.getCurrX();

            if (oldX != x) {
                final int range = getScrollRange();
                final int overscrollMode = getOverScrollMode();
                final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS ||
                        (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);

                overScrollBy(x - oldX, 0, oldX, 0, range, 0,
                        mOverFlingDistance, 0, false);
//                onScrollChanged(mScrollX, mScrollY, oldX, oldY);//TODO 不需要ScrollListener回调

                if (canOverscroll) {
                    if (x < 0 && oldX >= 0) {
                        mEdgeGlowLeft.onAbsorb((int) mScroller.getCurrVelocity());
                    } else if (x > range && oldX <= range) {
                        mEdgeGlowRight.onAbsorb((int) mScroller.getCurrVelocity());
                    }
                }
            }

            if (!awakenScrollBars()) {
                // Keep on drawing until the animation has finished.
                postInvalidateOnAnimation();
            }
        }
    }

    @Override
    protected int computeHorizontalScrollRange() {
        final int count = mEcgDatas.size();
        final int contentWidth = getWidth();
        if (count == 0) {
            return contentWidth;
        }

        int scrollRange = (int) ((count - 1) * mPointGap);
        final int scrollX = mScrollX;
        final int overscrollRight = Math.max(0, scrollRange - contentWidth);
        if (scrollX < 0) {
            scrollRange -= scrollX;
        } else if (scrollX > overscrollRight) {
            scrollRange += scrollX - overscrollRight;
        }
        return scrollRange;
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (mScrollX != scrollX) {
            mScrollX = scrollX;
        }
        if (!mScroller.isFinished()) {
            if (clampedX) {
                mScroller.springBack(mScrollX, 0, 0, getScrollRange(), 0, 0);
            }
        }
        invalidate();
    }

    private void endDrag() {
        mIsBeingDragged = false;

        recycleVelocityTracker();

        if (mEdgeGlowLeft != null) {
            mEdgeGlowLeft.onRelease();
            mEdgeGlowRight.onRelease();
        }
    }

    //TODO  EdgeEffect不知道为什么效果很淡
    @SuppressWarnings({"SuspiciousNameCombination"})
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mEdgeGlowLeft != null) {
            if (!mEdgeGlowLeft.isFinished()) {
                final int restoreCount = canvas.save();
                final int height = getHeight();

                canvas.rotate(270);
                canvas.translate(-height, 0);
                mEdgeGlowLeft.setSize(height, getWidth());
                if (mEdgeGlowLeft.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount);
            }
            if (!mEdgeGlowRight.isFinished()) {
                final int restoreCount = canvas.save();
                final int width = getWidth();
                final int height = getHeight();
                canvas.translate(width, 0);
                canvas.rotate(90);
                mEdgeGlowRight.setSize(height, width);
                if (mEdgeGlowRight.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount);
            }
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = (int) ev.getX(newPointerIndex);
            mLastMotionY = (int) ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    @Override
    public void setOverScrollMode(int mode) {
        if (mode != OVER_SCROLL_NEVER) {
            if (mEdgeGlowLeft == null) {
                Context context = getContext();
                mEdgeGlowLeft = new EdgeEffect(context);
                mEdgeGlowRight = new EdgeEffect(context);
            }
        } else {
            mEdgeGlowLeft = null;
            mEdgeGlowRight = null;
        }
        super.setOverScrollMode(mode);
    }

    private OnEcgClickListener mEcgClickListener;

    public void setOnEcgClickListener(OnEcgClickListener listener) {
        mEcgClickListener = listener;
    }
}