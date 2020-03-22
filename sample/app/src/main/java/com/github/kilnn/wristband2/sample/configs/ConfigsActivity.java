package com.github.kilnn.wristband2.sample.configs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.R;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.WristbandConfig;
import com.htsmart.wristband2.bean.WristbandVersion;
import com.htsmart.wristband2.bean.config.BloodPressureConfig;
import com.htsmart.wristband2.bean.config.DrinkWaterConfig;
import com.htsmart.wristband2.bean.config.FunctionConfig;
import com.htsmart.wristband2.bean.config.HealthyConfig;
import com.htsmart.wristband2.bean.config.NotDisturbConfig;
import com.htsmart.wristband2.bean.config.SedentaryConfig;
import com.htsmart.wristband2.bean.config.TurnWristLightingConfig;
import com.htsmart.wristband2.bean.config.WarnBloodPressureConfig;
import com.htsmart.wristband2.bean.config.WarnHeartRateConfig;

import androidx.annotation.Nullable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

@SuppressWarnings("ResultOfMethodCallIgnored")
@SuppressLint("CheckResult")
public class ConfigsActivity extends BaseActivity {

    private WristbandManager mWristbandManager = WristbandApplication.getWristbandManager();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configs);
    }

    /**
     * 1.Wristband version info
     */
    public void version_info(View view) {
        if (mWristbandManager.isConnected()) {
            WristbandConfig wristbandConfig = mWristbandManager.getWristbandConfig();
            HardwareInfoDialogFragment.newInstance(wristbandConfig.getWristbandVersion())
                    .show(getSupportFragmentManager(), null);
        } else {
            toast(R.string.toast_device_disconnected);
        }
    }

    /**
     * 2.Notification Config
     */
    public void notification_config(View view) {
        startActivity(new Intent(this, NotificationConfigActivity.class));
    }

    /**
     * 3.Blood Pressure Config
     */
    public void blood_pressure_config(View view) {
        if (mWristbandManager.isConnected()) {
//            If blood pressure function is not support, although this config can be set up normally, it is meaningless.
//            boolean bloodPressureEnabled = mWristbandManager.getWristbandConfig().getWristbandVersion().isBloodPressureEnabled();
            BloodPressureConfig config = mWristbandManager.getWristbandConfig().getBloodPressureConfig();
            config.setSystolicPressure(120);
            config.setDiastolicPressure(70);
            config.setPrivateModel(false);

            mWristbandManager.setBloodPressureConfig(config)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            toast(R.string.operation_success);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e("sample", "", throwable);
                            toast(R.string.operation_failed);
                        }
                    });
        } else {
            toast(R.string.toast_device_disconnected);
        }
    }


    /**
     * 4.Drink Water Config
     */
    public void drink_water_config(View view) {
        if (mWristbandManager.isConnected()) {
            DrinkWaterConfig config = mWristbandManager.getWristbandConfig().getDrinkWaterConfig();
            config.setStart(9 * 60);//Start Time:9:00
            config.setEnd(22 * 60);//End Time:22:00
            config.setInterval(30);//Interval 30min
            config.setEnable(true);

            mWristbandManager.setDrinkWaterConfig(config)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            toast(R.string.operation_success);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e("sample", "", throwable);
                            toast(R.string.operation_failed);
                        }
                    });
        } else {
            toast(R.string.toast_device_disconnected);
        }
    }

    /**
     * 5.Function Config
     */
    public void function_config(View view) {
        if (mWristbandManager.isConnected()) {
            FunctionConfig config = mWristbandManager.getWristbandConfig().getFunctionConfig();
            config.setFlagEnable(FunctionConfig.FLAG_WEAR_WAY, true);//Wear right hand
            config.setFlagEnable(FunctionConfig.FLAG_STRENGTHEN_TEST, true);//Enhanced measurement
            config.setFlagEnable(FunctionConfig.FLAG_HOUR_STYLE, false);//24-hour system
            config.setFlagEnable(FunctionConfig.FLAG_LENGTH_UNIT, false);//Metric unit
            config.setFlagEnable(FunctionConfig.FLAG_TEMPERATURE_UNIT, false);//Centigrade

            mWristbandManager.setFunctionConfig(config)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            toast(R.string.operation_success);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e("sample", "", throwable);
                            toast(R.string.operation_failed);
                        }
                    });
        } else {
            toast(R.string.toast_device_disconnected);
        }
    }

    /**
     * 6.Healthy Monitor Config
     */
    public void healthy_config(View view) {
        if (mWristbandManager.isConnected()) {
            HealthyConfig config = mWristbandManager.getWristbandConfig().getHealthyConfig();
            config.setStart(9 * 60);//Start Time:9:00
            config.setEnd(22 * 60);//End Time:22:00
            config.setEnable(true);

            mWristbandManager.setHealthyConfig(config)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            toast(R.string.operation_success);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e("sample", "", throwable);
                            toast(R.string.operation_failed);
                        }
                    });
        } else {
            toast(R.string.toast_device_disconnected);
        }
    }

    /**
     * 7.Sedentary Config
     */
    public void sedentary_config(View view) {
        if (mWristbandManager.isConnected()) {
            SedentaryConfig config = mWristbandManager.getWristbandConfig().getSedentaryConfig();
            config.setStart(9 * 60);//Start Time:9:00
            config.setEnd(23 * 59);//End Time:23:59
            config.setNotDisturbEnable(false);
            config.setEnable(true);

            mWristbandManager.setSedentaryConfig(config)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            toast(R.string.operation_success);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e("sample", "", throwable);
                            toast(R.string.operation_failed);
                        }
                    });
        } else {
            toast(R.string.toast_device_disconnected);
        }
    }

    /**
     * 8.Page Config
     */
    public void page_config(View view) {
        startActivity(new Intent(this, PageConfigActivity.class));
    }

    /**
     * 9.Turn Wrist Lighting Config
     */
    public void turn_wrist_lighting_config(View view) {
        if (mWristbandManager.isConnected()) {
            TurnWristLightingConfig config = mWristbandManager.getWristbandConfig().getTurnWristLightingConfig();
            config.setStart(9 * 60);//Start Time:9:00
            config.setEnd(23 * 59);//End Time:23:59
            config.setEnable(false);

            mWristbandManager.setTurnWristLightingConfig(config)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            toast(R.string.operation_success);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e("sample", "", throwable);
                            toast(R.string.operation_failed);
                        }
                    });
        } else {
            toast(R.string.toast_device_disconnected);
        }
    }


    /**
     * 10.Warn Heart Rate Config
     */
    public void warn_heart_rate_config(View view) {
        if (mWristbandManager.isConnected()) {
            WristbandVersion version = mWristbandManager.getWristbandConfig().getWristbandVersion();
            if (version.isExtWarnHeartRate()) {
                WarnHeartRateConfig config = mWristbandManager.getWristbandConfig().getWarnHeartRateConfig();
                config.setDynamicEnable(true);
                config.setStaticEnable(true);
                config.setDynamicValue(140);
                config.setStaticValue(90);
                mWristbandManager.setWarnHeartRateConfig(config)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                toast(R.string.operation_success);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e("sample", "", throwable);
                                toast(R.string.operation_failed);
                            }
                        });
            } else {
                toast(R.string.toast_device_not_support);
            }
        } else {
            toast(R.string.toast_device_disconnected);
        }
    }

    /**
     * 11.Warn BloodPressure Config
     */
    public void warn_blood_pressure_config(View view) {
        if (mWristbandManager.isConnected()) {
            WristbandVersion version = mWristbandManager.getWristbandConfig().getWristbandVersion();
            if (version.isExtWarnBloodPressure()) {
                WarnBloodPressureConfig config = mWristbandManager.getWristbandConfig().getWarnBloodPressureConfig();
                config.setEnable(true);
                config.setSbpLowerLimit(60);
                config.setSbpUpperLimit(100);
                config.setDbpLowerLimit(90);
                config.setDbpUpperLimit(140);
                mWristbandManager.setWarnBloodPressureConfig(config)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                toast(R.string.operation_success);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e("sample", "", throwable);
                                toast(R.string.operation_failed);
                            }
                        });
            } else {
                toast(R.string.toast_device_not_support);
            }
        } else {
            toast(R.string.toast_device_disconnected);
        }
    }

    /**
     * 12. DND
     */
    public void dnd_config(View view) {
        if (mWristbandManager.isConnected()) {
            WristbandVersion version = mWristbandManager.getWristbandConfig().getWristbandVersion();
            if (version.isExtNotDisturb()) {
                NotDisturbConfig config = mWristbandManager.getWristbandConfig().getNotDisturbConfig();
                config.setEnableAllDay(true);
                mWristbandManager.setNotDisturbConfig(config)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                toast(R.string.operation_success);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e("sample", "", throwable);
                                toast(R.string.operation_failed);
                            }
                        });
            } else {
                toast(R.string.toast_device_not_support);
            }
        } else {
            toast(R.string.toast_device_disconnected);
        }
    }

}
