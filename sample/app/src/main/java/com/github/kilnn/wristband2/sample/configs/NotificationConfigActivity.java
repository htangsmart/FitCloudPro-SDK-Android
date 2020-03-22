package com.github.kilnn.wristband2.sample.configs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.R;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.WristbandConfig;
import com.htsmart.wristband2.bean.config.NotificationConfig;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;


public class NotificationConfigActivity extends BaseActivity {

    private WristbandManager mWristbandManager = WristbandApplication.getWristbandManager();
    private WristbandConfig mWristbandConfig;

    private CheckBox cb_phone_call;
    private CheckBox cb_sms;
    private CheckBox cb_qq;
    private CheckBox cb_wechat;
    private CheckBox cb_facebook;
    private CheckBox cb_twitter;
    private CheckBox cb_linkedin;
    private CheckBox cb_instagram;
    private CheckBox cb_pinterest;
    private CheckBox cb_whatsapp;
    private CheckBox cb_line;
    private CheckBox cb_facebook_msg;
    private CheckBox cb_kakaotalk;
    private CheckBox cb_skype;
    private CheckBox cb_snapchat;
    private CheckBox cb_email;
    private CheckBox cb_telegram;
    private CheckBox cb_viber;
    private CheckBox cb_calendar;
    private CheckBox cb_others;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_config);
        initView();
        mWristbandConfig = mWristbandManager.getWristbandConfig();
        if (mWristbandConfig != null) {
            NotificationConfig config = mWristbandConfig.getNotificationConfig();
            cb_phone_call.setChecked(config.isFlagEnable(NotificationConfig.FLAG_TELEPHONE));
            cb_sms.setChecked(config.isFlagEnable(NotificationConfig.FLAG_SMS));
            cb_qq.setChecked(config.isFlagEnable(NotificationConfig.FLAG_QQ));
            cb_wechat.setChecked(config.isFlagEnable(NotificationConfig.FLAG_WECHAT));
            cb_facebook.setChecked(config.isFlagEnable(NotificationConfig.FLAG_FACEBOOK));
            cb_pinterest.setChecked(config.isFlagEnable(NotificationConfig.FLAG_PINTEREST));
            cb_whatsapp.setChecked(config.isFlagEnable(NotificationConfig.FLAG_WHATSAPP));
            cb_line.setChecked(config.isFlagEnable(NotificationConfig.FLAG_LINE));
            cb_kakaotalk.setChecked(config.isFlagEnable(NotificationConfig.FLAG_KAKAO));

            if (mWristbandConfig.getWristbandVersion().isExtAncsEmail()) {
                cb_email.setChecked(config.isFlagEnable(NotificationConfig.FLAG_EMAIL));
            } else {
                cb_email.setVisibility(View.GONE);
            }

            if (mWristbandConfig.getWristbandVersion().isExtAncsViberTelegram()) {
                cb_telegram.setChecked(config.isFlagEnable(NotificationConfig.FLAG_TELEGRAM));
                cb_viber.setChecked(config.isFlagEnable(NotificationConfig.FLAG_VIBER));
            } else {
                cb_telegram.setVisibility(View.GONE);
                cb_viber.setVisibility(View.GONE);
            }

            if (mWristbandConfig.getWristbandVersion().isExtAncsExtra1()) {
                cb_twitter.setChecked(config.isFlagEnable(NotificationConfig.FLAG_TWITTER));
                cb_linkedin.setChecked(config.isFlagEnable(NotificationConfig.FLAG_LINKEDIN));
                cb_instagram.setChecked(config.isFlagEnable(NotificationConfig.FLAG_INSTAGRAM));
                cb_facebook_msg.setChecked(config.isFlagEnable(NotificationConfig.FLAG_FACEBOOK_MESSENGER));
                cb_skype.setChecked(config.isFlagEnable(NotificationConfig.FLAG_SKYPE));
                cb_snapchat.setChecked(config.isFlagEnable(NotificationConfig.FLAG_SNAPCHAT));
            } else {
                cb_twitter.setVisibility(View.GONE);
                cb_linkedin.setVisibility(View.GONE);
                cb_instagram.setVisibility(View.GONE);
                cb_facebook_msg.setVisibility(View.GONE);
                cb_skype.setVisibility(View.GONE);
                cb_snapchat.setVisibility(View.GONE);
            }

            cb_calendar.setVisibility(View.GONE);

            cb_others.setChecked(config.isFlagEnable(NotificationConfig.FLAG_OTHERS_APP));
        }
    }

    private void initView() {
        cb_phone_call = findViewById(R.id.cb_phone_call);
        cb_sms = findViewById(R.id.cb_sms);
        cb_qq = findViewById(R.id.cb_qq);
        cb_wechat = findViewById(R.id.cb_wechat);
        cb_facebook = findViewById(R.id.cb_facebook);
        cb_twitter = findViewById(R.id.cb_twitter);
        cb_linkedin = findViewById(R.id.cb_linkedin);
        cb_instagram = findViewById(R.id.cb_instagram);
        cb_pinterest = findViewById(R.id.cb_pinterest);
        cb_whatsapp = findViewById(R.id.cb_whatsapp);
        cb_line = findViewById(R.id.cb_line);
        cb_facebook_msg = findViewById(R.id.cb_facebook_msg);
        cb_kakaotalk = findViewById(R.id.cb_kakaotalk);
        cb_skype = findViewById(R.id.cb_skype);
        cb_snapchat = findViewById(R.id.cb_snapchat);
        cb_email = findViewById(R.id.cb_email);
        cb_telegram = findViewById(R.id.cb_telegram);
        cb_viber = findViewById(R.id.cb_viber);
        cb_calendar = findViewById(R.id.cb_calendar);
        cb_others = findViewById(R.id.cb_others);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            if (mWristbandConfig != null) {
                final NotificationConfig config = mWristbandConfig.getNotificationConfig();
                config.setFlagEnable(NotificationConfig.FLAG_TELEPHONE, cb_phone_call.isChecked());
                config.setFlagEnable(NotificationConfig.FLAG_SMS, cb_sms.isChecked());
                config.setFlagEnable(NotificationConfig.FLAG_QQ, cb_qq.isChecked());
                config.setFlagEnable(NotificationConfig.FLAG_WECHAT, cb_wechat.isChecked());
                config.setFlagEnable(NotificationConfig.FLAG_FACEBOOK, cb_facebook.isChecked());
                config.setFlagEnable(NotificationConfig.FLAG_PINTEREST, cb_pinterest.isChecked());
                config.setFlagEnable(NotificationConfig.FLAG_WHATSAPP, cb_whatsapp.isChecked());
                config.setFlagEnable(NotificationConfig.FLAG_LINE, cb_line.isChecked());
                config.setFlagEnable(NotificationConfig.FLAG_KAKAO, cb_kakaotalk.isChecked());

                if (mWristbandConfig.getWristbandVersion().isExtAncsEmail()) {
                    config.setFlagEnable(NotificationConfig.FLAG_EMAIL, cb_email.isChecked());
                }

                if (mWristbandConfig.getWristbandVersion().isExtAncsViberTelegram()) {
                    config.setFlagEnable(NotificationConfig.FLAG_TELEGRAM, cb_telegram.isChecked());
                    config.setFlagEnable(NotificationConfig.FLAG_VIBER, cb_viber.isChecked());
                }

                if (mWristbandConfig.getWristbandVersion().isExtAncsExtra1()) {
                    config.setFlagEnable(NotificationConfig.FLAG_TWITTER, cb_twitter.isChecked());
                    config.setFlagEnable(NotificationConfig.FLAG_LINKEDIN, cb_linkedin.isChecked());
                    config.setFlagEnable(NotificationConfig.FLAG_INSTAGRAM, cb_instagram.isChecked());
                    config.setFlagEnable(NotificationConfig.FLAG_FACEBOOK_MESSENGER, cb_facebook_msg.isChecked());
                    config.setFlagEnable(NotificationConfig.FLAG_SKYPE, cb_skype.isChecked());
                    config.setFlagEnable(NotificationConfig.FLAG_SNAPCHAT, cb_snapchat.isChecked());
                }

//                config.setFlagEnable(NotificationConfig.FLAG_CALENDAR, cb_calendar.isChecked());
                config.setFlagEnable(NotificationConfig.FLAG_OTHERS_APP, cb_others.isChecked());

                mWristbandManager.setNotificationConfig(config)
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
                toast(R.string.operation_failed);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
