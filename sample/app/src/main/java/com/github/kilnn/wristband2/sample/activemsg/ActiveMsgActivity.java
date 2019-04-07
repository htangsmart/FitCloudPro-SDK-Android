package com.github.kilnn.wristband2.sample.activemsg;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.utils.AndPermissionHelper;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;

import java.lang.reflect.Method;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class ActiveMsgActivity extends BaseActivity {

    private WristbandManager mWristbandManager = WristbandApplication.getWristbandManager();

    private SoundPool mSoundPool;
    private int mFindPhoneSoundId;

    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_msg);
        initSound();
        mDisposable = mWristbandManager.observerWristbandMessage()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer == WristbandManager.MSG_WEATHER) {
                            //SDK no longer supports this feature
                        } else if (integer == WristbandManager.MSG_FIND_PHONE) {
                            mSoundPool.play(mFindPhoneSoundId, 1, 1, 0, 2, 1);
                        } else if (integer == WristbandManager.MSG_HUNG_UP_PHONE) {
                            endCall(ActiveMsgActivity.this);
                        }
                    }
                });
    }

    private void initSound() {
        //创建一个SoundPool对象，该对象可以容纳2个音频流
        mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        mFindPhoneSoundId = mSoundPool.load(this, R.raw.findphone, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
        mSoundPool.release();
    }

    public void camera_control(View view) {
        AndPermissionHelper.cameraRequest(this, new AndPermissionHelper.AndPermissionHelperListener1() {
            @Override
            public void onSuccess() {
                startActivity(new Intent(ActiveMsgActivity.this, CameraControlActivity.class));
            }
        });
    }

    /**
     * This is just an example of hanging up the phone, the actual function also needs to apply for permissions,
     * and not necessarily applicable to all versions of Android system.
     */
    @SuppressWarnings("unchecked")
    public static boolean endCall(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            TelecomManager manager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (manager != null && manager.endCall()) {
                return true;
            }
        }
        try {
            // Get the boring old TelephonyManager
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) {
                return false;
            }
            // Get the getITelephony() method
            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");

            // Ignore that the method is supposed to be private
            methodGetITelephony.setAccessible(true);

            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);

            // Get the endCall method from ITelephony
            Class telephonyInterfaceClass =
                    Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");

            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface);

        } catch (Exception ex) { // Many things can go wrong with reflection calls
            Log.e("EndCall", "EndCall Error", ex);
            return false;
        }
        return true;
    }

}
