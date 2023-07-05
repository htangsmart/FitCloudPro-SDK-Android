package com.topstep.fitcloud.sample2.ui.widget;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.topstep.fitcloud.sample2.R;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class CountDownView extends FrameLayout {

    public interface OnCountDownFinishedListener {
        void onCountDownFinished();
    }

    private static final String TAG = "CountDownView";
    private static final int SET_TIMER_TEXT = 1;

    private final TextView mRemainingSecondsView;
    private final Animation mCountDownAnim;
    private final SoundPool mSoundPool;
    private final int mBeepTwice;
    private final int mBeepOnce;
    private final int mBeepShutter;
    private final Handler mHandler;

    private int mRemainingSecs = 0;
    private OnCountDownFinishedListener mListener;
    private boolean mPlaySound;

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCountDownAnim = AnimationUtils.loadAnimation(context, R.anim.count_down_exit);
        // Load the beeps
        mSoundPool = new SoundPool.Builder().setMaxStreams(3).setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
        ).build();
        mBeepOnce = mSoundPool.load(context, R.raw.beep_once, 1);
        mBeepTwice = mSoundPool.load(context, R.raw.beep_twice, 1);
        mBeepShutter = mSoundPool.load(context, R.raw.beep_shutter, 1);
        mHandler = new MainHandler(this);

        LayoutInflater.from(getContext()).inflate(R.layout.count_down_view, this);
        mRemainingSecondsView = findViewById(R.id.remaining_seconds);
    }

    public void playBeepShutter() {
        mSoundPool.play(mBeepShutter, 1.0f, 1.0f, 0, 0, 1.0f);
    }

    private void remainingSecondsChanged(int newVal) {
        mRemainingSecs = newVal;
        if (newVal == 0) {
            // Countdown has finished
            mRemainingSecondsView.setText(null);
            mListener.onCountDownFinished();
        } else {
            Locale locale = getResources().getConfiguration().locale;
            String localizedValue = String.format(locale, "%d", newVal);
            mRemainingSecondsView.setText(localizedValue);
            // Fade-out animation
            mCountDownAnim.reset();
            mRemainingSecondsView.clearAnimation();
            mRemainingSecondsView.startAnimation(mCountDownAnim);

            // Play sound effect for the last 3 seconds of the countdown
            if (mPlaySound) {
                if (newVal == 1) {
                    mSoundPool.play(mBeepTwice, 1.0f, 1.0f, 0, 0, 1.0f);
                } else if (newVal <= 3) {
                    mSoundPool.play(mBeepOnce, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }
            // Schedule the next remainingSecondsChanged() call in 1 second
            mHandler.sendEmptyMessageDelayed(SET_TIMER_TEXT, 1000);
        }
    }

    public void setCountDownFinishedListener(OnCountDownFinishedListener listener) {
        mListener = listener;
    }

    public boolean isCountingDown() {
        return mRemainingSecs > 0;
    }

    public void startCountDown(int sec, boolean playSound) {
        if (sec <= 0) {
            return;
        }
        mRemainingSecondsView.setText(null);
        mPlaySound = playSound;
        remainingSecondsChanged(sec);
    }

    public void cancelCountDown() {
        if (mRemainingSecs > 0) {
            mRemainingSecs = 0;
            mHandler.removeMessages(SET_TIMER_TEXT);
            mRemainingSecondsView.setText(null);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mSoundPool.release();
    }

    private static class MainHandler extends Handler {
        private final WeakReference<CountDownView> reference;

        private MainHandler(CountDownView countDownView) {
            super(Looper.getMainLooper());
            reference = new WeakReference<>(countDownView);
        }

        @Override
        public void handleMessage(Message message) {
            if (message.what == SET_TIMER_TEXT) {
                CountDownView countDownView = reference.get();
                if (countDownView != null) {
                    countDownView.remainingSecondsChanged(countDownView.mRemainingSecs - 1);
                }
            }
        }
    }
}