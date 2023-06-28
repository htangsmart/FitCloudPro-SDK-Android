package com.topstep.fitcloud.sample2.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.github.kilnn.tool.ui.DisplayUtil;
import com.topstep.fitcloud.sample2.R;
import com.topstep.fitcloud.sample2.data.entity.EcgRecordEntity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;


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

    public static final int MODE_NORMAL = 1;//正常模式，展示数据，允许滑动
    public static final int MODE_REALTIME = 2;//展示实时数据，不允许滑动
    public static final int MODE_PLAYBACK = 3;//回放数据，不允许滑动

    @IntDef({MODE_NORMAL, MODE_REALTIME, MODE_PLAYBACK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    private static final int DEFAULT_LINE_COLOR = Color.RED;
    private static final int DEFAULT_LINE_WIDTH = 2;//2dp
    private static final int DEFAULT_VERTICAL_COUNT = 40;
    private static final int DEFAULT_SAMPLING_RATE = 125;//默认100hz
    private static final int DEFAULT_SPEED = 25;//默认走速25mm/s
    private static final int DEFAULT_AMPLITUDE = 10;//默认增益10mm/mv

    @Mode
    private volatile int mMode = MODE_NORMAL;
    private List<Integer> mEcgDatas;//数据点

    /*手势相关变量*/
    private OverScroller mScroller;
    private EdgeEffect mEdgeGlowLeft;
    private EdgeEffect mEdgeGlowRight;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mOverScrollDistance;
    private int mOverFlingDistance;

    /*attr初始化的变量*/
    private int mGridVerticalCount;
    private int mSamplingRate;//数据采样率，单位hz
    private int mSpeed;//走速，单位mm/s
    private int mAmplitude;//增益，单位mm/mv

    /*这两个值根据实际数据微调*/
    private float mEcgDataUnit = 0.07152582f;//数据分辨率。不同芯片的数据，这个值会不一样。
    private float mYBaseLine = 3;// y轴的基准线

    /*绘图相关变量*/
    private float mGridWidth;//每个小格子的宽度
    private float mGridHeight;//每个小格子的高度
    private float mXPointGap;//每个点在x轴上中间间隔多少个像素
    private Path mPath;
    private Paint mPaint;

    //refresh
    private float mWindowRefreshRate;
    private int mRefreshPointCount;//每次刷新几个点
    private int mRefreshInterval;//每次刷新间隔时间,毫秒
    private RefreshThread mRefreshThread;
    private final AtomicInteger mRefreshDrawCount = new AtomicInteger(0);//How many points should be drawn by the refresh thread

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mEcgDatas = new ArrayList<>(10000);

        //set default value
        int lineColor = DEFAULT_LINE_COLOR;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float lineWidth = DEFAULT_LINE_WIDTH * metrics.density;
        mGridVerticalCount = DEFAULT_VERTICAL_COUNT;

        mSamplingRate = DEFAULT_SAMPLING_RATE;
        mSpeed = DEFAULT_SPEED;
        mAmplitude = DEFAULT_AMPLITUDE;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EcgView, defStyleAttr, 0);

            lineColor = a.getColor(R.styleable.EcgView_ecg_line_color, lineColor);
            lineWidth = a.getDimension(R.styleable.EcgView_ecg_line_width, lineWidth);
            mGridVerticalCount = a.getInt(R.styleable.EcgView_ecg_grid_vertical_count, mGridVerticalCount);

            mSamplingRate = a.getInt(R.styleable.EcgView_ecg_sampling_rate, mSamplingRate);
            mSpeed = a.getInt(R.styleable.EcgView_ecg_speed, mSpeed);
            mAmplitude = a.getInt(R.styleable.EcgView_ecg_amplitude, mAmplitude);
            a.recycle();
        }

        mGridWidth = DisplayUtil.get_x_pix_per_mm(getContext());
        mGridHeight = DisplayUtil.get_y_pix_per_mm(getContext());
        calculateXPointGap();

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

        //Get screen Refresh rate
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        mWindowRefreshRate = display.getRefreshRate();
        calculateRefreshParam();

        //Disable Hardware acceleration to prevent Path from being too large to draw
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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

    public interface OnPlayBackListener {
        void onStartPlayBack();

        void onStopPlayBack();
    }

    private OnPlayBackListener mOnPlayBackListener;

    public void setOnPlayBackListener(OnPlayBackListener listener) {
        mOnPlayBackListener = listener;
    }

    public void clearData() {
        mEcgDatas.clear();
        //reset mScrollX
        mScrollX = 0;
        invalidate();
    }

    /**
     * Add one data
     */
    public void addData(int data) {
        mEcgDatas.add(data);
        if (mMode == MODE_NORMAL) {
            invalidate();
        }
    }

    /**
     * Add a set of data
     */
    public void addData(int[] datas) {
        if (datas == null) return;
        for (int d : datas) {
            mEcgDatas.add(d);
        }
        if (mMode == MODE_NORMAL) {
            invalidate();
        }
    }

    /**
     * Add a set of data and scroll to last
     */
    public void addDataAndScrollToLast(int[] datas) {
        if (datas == null) return;
        for (int d : datas) {
            mEcgDatas.add(d);
        }
        post(new Runnable() {
            @Override
            public void run() {
                if (getWidth() != 0 && getHeight() != 0) {
                    if (mMode == MODE_NORMAL) {
                        mScrollX = 0;
                        invalidate();
                    }
                } else {
                    post(this);
                }
            }
        });
    }

    /**
     * Manually scroll to the last data
     */
    public void scrollToLastData() {
        post(new Runnable() {
            @Override
            public void run() {
                if (mMode == MODE_NORMAL) {
                    mScrollX = 0;
                    invalidate();
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopRefreshThread();
    }

    private void startRefreshThread() {
        stopRefreshThread();
        if (mMode == MODE_NORMAL) return;
        mRefreshThread = new RefreshThread();
        mRefreshThread.start();
    }

    private void stopRefreshThread() {
        if (mRefreshThread != null && mRefreshThread.isAlive()) {
            mRefreshThread.interrupt();
        }
        mRefreshThread = null;
    }

    private class RefreshThread extends Thread {

        @Override
        public void run() {
            mRefreshDrawCount.set(0);
            while (mMode != MODE_NORMAL) {
                postInvalidate();
                try {
                    Thread.sleep(mRefreshInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int add = Math.min(mRefreshPointCount, mEcgDatas.size() - mRefreshDrawCount.get());
                if (add > 0) {
                    mRefreshDrawCount.addAndGet(add);
                }
            }
        }
    }

    public int getMode() {
        return mMode;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public int getAmplitude() {
        return mAmplitude;
    }

    public int getSamplingRate() {
        return mSamplingRate;
    }

    public void setMode(@Mode int mode) {
        if (mMode == mode) return;
        //If it was previously in playback mode, notify the end of playback mode
        if (mMode == MODE_PLAYBACK && mOnPlayBackListener != null) {
            mOnPlayBackListener.onStopPlayBack();
        }

        mMode = mode;
        stopRefreshThread();
        mScrollX = 0;        //reset mScrollX

        if (mMode == MODE_NORMAL) {
            invalidate();
        } else {
            startRefreshThread();
            if (mMode == MODE_PLAYBACK && mOnPlayBackListener != null) {
                mOnPlayBackListener.onStartPlayBack();
            }
        }
    }

    public void setSpeed(int speed) {
        if (speed != mSpeed) {
            this.mSpeed = speed;
            calculateXPointGap();
            if (mMode == MODE_NORMAL) {
                invalidate();
            }
        }
    }

    public void setAmplitude(int amplitude) {
        if (mAmplitude != amplitude) {
            this.mAmplitude = amplitude;
            if (mMode == MODE_NORMAL) {
                invalidate();
            }
        }
    }

    public void setSamplingRate(int rate) {
        if (rate <= 0) {
            rate = DEFAULT_SAMPLING_RATE;
        }
        if (mSamplingRate != rate) {
            this.mSamplingRate = rate;
            calculateXPointGap();
            calculateRefreshParam();
            if (mMode == MODE_NORMAL) {
                invalidate();
            }
        }
    }

    public void setDataType(@EcgRecordEntity.Type int type) {
        if (type == EcgRecordEntity.Type.TI) {
            mEcgDataUnit = 0.07152582f;
            mYBaseLine = 3;
            setSpeed(DEFAULT_SPEED);
            setAmplitude(DEFAULT_AMPLITUDE);
        } else {
            //TODO At present, it is the same and will be adjusted according to later testing
            mEcgDataUnit = 0.07152582f;
            mYBaseLine = 3;
            setSpeed(DEFAULT_SPEED);
            setAmplitude(DEFAULT_AMPLITUDE);
        }
    }

    /**
     * Calculate the distance of each ecg value on the x-axis
     */
    private void calculateXPointGap() {
        mXPointGap = mGridWidth / ((1000.0f / mSpeed) / (1000.0f / mSamplingRate));
    }

    private void calculateRefreshParam() {
        mRefreshPointCount = (int) Math.ceil(mSamplingRate / mWindowRefreshRate);
        mRefreshInterval = (int) (mRefreshPointCount * (1000.0f / mSamplingRate));
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
            case MODE_REALTIME:
                drawRefresh(canvas);
                break;
        }
    }

    private void drawNormal(Canvas canvas) {
        final int dataSize = mEcgDatas.size();
        if (dataSize <= 0) return;
        int startX = mScrollX;//draw start x
        int endX = mScrollX + getWidth();//draw end x
        // 最左边点的位置要<=startX ,最右边点的位置要>=endX，这样才能显示完整的可是区域，并且点的数量正好合适
        int leftIndex = (int) Math.floor(startX / mXPointGap);
        int rightIndex = (int) Math.ceil(endX / mXPointGap);

        //leftIndex和rightIndex是可以绘制的点序号范围。
        //要注意，leftIndex可能小于0(因为overscroll的原因, mScrollX 可能小于0)
        //rightIndex可能大于mEcgDatas的size(当数据较少，或者因为overscroll的原因)
        if (leftIndex < 0) leftIndex = 0;
        if (rightIndex > dataSize - 1) rightIndex = dataSize - 1;
        int count = rightIndex - leftIndex + 1;
        //因为视图从最左边的点开始绘制，所以最左边的点超出了屏幕多少
        float offset = leftIndex * mXPointGap - mScrollX;
        drawRangeEcgDatas(canvas, leftIndex, offset, count, mPath);
    }

    private void drawRefresh(Canvas canvas) {
        final int dataSize = mEcgDatas.size();
        final int drawIndex = mRefreshDrawCount.get() - 1;
        if (dataSize <= 0 || drawIndex < 0) return;

        int rowPointCount = (int) (getWidth() / mXPointGap) + 1;//每行能放多少个点
        int currentRow = drawIndex / rowPointCount;//当前是画第几行

        int startIndex = currentRow * rowPointCount;
        drawRangeEcgDatas(canvas, startIndex, 0, drawIndex - startIndex + 1, mPath);

        if (currentRow > 0) {
            int deltaIndex = (drawIndex % rowPointCount + mRefreshPointCount);
            if (deltaIndex < rowPointCount) {
                //画后面一段
                startIndex = deltaIndex + (currentRow - 1) * rowPointCount;
                int offset = (int) ((startIndex % rowPointCount) * mXPointGap);
                drawRangeEcgDatas(canvas, startIndex, offset, rowPointCount - startIndex % rowPointCount, mPath);
            }
        }

        if (mMode == MODE_PLAYBACK && drawIndex == dataSize - 1) {
            setMode(MODE_NORMAL);
        }
    }

    /**
     * @param canvas
     * @param startIndex Starting from which point to draw
     * @param offset     What is the offset to start drawing
     * @param count      Number of drawing points
     */
    private void drawRangeEcgDatas(Canvas canvas, int startIndex, float offset, int count, Path path) {
//        if (BuildConfig.DEBUG) {
//            Log.e(TAG, "startIndex:" + startIndex + "   count:" + count + "   offset:" + offset);
//        }
        path.reset();
        path.moveTo(offset, getPointY(mEcgDatas.get(startIndex)));
        if (count > 1) {
            for (int i = startIndex + 1; i < startIndex + count; i++) {
                path.lineTo(offset + mXPointGap * (i - startIndex), getPointY(mEcgDatas.get(i)));
            }
        }
        canvas.drawPath(path, mPaint);
    }

    private float getPointY(int value) {
        float valueHeight = value * mEcgDataUnit / 1000 * mAmplitude;//计算心电值的高度为多少毫米
        return getHeight() - (valueHeight - mYBaseLine * mAmplitude / 10.0f) * mGridHeight;//计算心电值的y轴坐标
    }

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
            scrollRange = (int) Math.max(0, (limit - 1) * mXPointGap - getWidth());
        }
        return scrollRange;
    }

    private int getScrollRange() {
        int scrollRange = 0;
        if (mEcgDatas.size() > 0) {
            scrollRange = (int) Math.max(0, (mEcgDatas.size() - 1) * mXPointGap - getWidth());
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
                if ((mIsBeingDragged == !mScroller.isFinished())) {
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
                    Timber.tag(TAG).e("Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
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
                        Timber.tag(TAG).e("Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
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
            int right = (int) ((mEcgDatas.size() - 1) * mXPointGap);
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

        int scrollRange = (int) ((count - 1) * mXPointGap);
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