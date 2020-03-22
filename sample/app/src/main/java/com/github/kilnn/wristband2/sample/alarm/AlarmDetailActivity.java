package com.github.kilnn.wristband2.sample.alarm;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.widget.SectionItem;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.WristbandAlarm;

import java.util.ArrayList;
import java.util.Date;

import cn.imengya.wheelview.WheelView;
import cn.imengya.wheelview.adapters.ArrayWheelAdapter;
import cn.imengya.wheelview.adapters.NumericWheelAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class AlarmDetailActivity extends BaseActivity
        implements EditLabelDialogFragment.EditLabelDialogFragmentHolder {

    public static final String EXTRA_ALARM_LIST = "alarm_list";
    public static final String EXTRA_ALARM_POSITION = "alarm_position";
    public static final String EXTRA_ALARM_REPEAT = "alarm_repeat";

    private WheelView mAmPmWheelView;
    private WheelView mHourWheelView;
    private WheelView mMinuteWheelView;
    private SectionItem mSectionItemAlarmRepeat;
    private SectionItem mSectionItemAlarmLabel;
    private RelativeLayout mRlAlarmDelete;

    private boolean is24HourFormat;
    private CharSequence[] mDayValuesSimple = null;

    private ArrayList<WristbandAlarm> mAlarmList;//Alarm列表
    private boolean mEdit;//是否是编辑模式，true编辑，false新增
    private WristbandAlarm mAlarm;//正在编辑的Alarm

    private WristbandManager mWristManager = WristbandApplication.getWristbandManager();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_detail);

        mAmPmWheelView = findViewById(R.id.wheel_view_alarm_am_pm);
        mHourWheelView = findViewById(R.id.wheel_view_alarm_hour);
        mMinuteWheelView = findViewById(R.id.wheel_view_alarm_minute);
        mSectionItemAlarmRepeat = findViewById(R.id.section_item_alarm_repeat);
        mSectionItemAlarmLabel = findViewById(R.id.section_item_alarm_label);
        mRlAlarmDelete = findViewById(R.id.rl_alarm_delete);

        is24HourFormat = DateFormat.is24HourFormat(this);
        mDayValuesSimple = new CharSequence[]{
                getString(R.string.ds_alarm_repeat_00_simple),
                getString(R.string.ds_alarm_repeat_01_simple),
                getString(R.string.ds_alarm_repeat_02_simple),
                getString(R.string.ds_alarm_repeat_03_simple),
                getString(R.string.ds_alarm_repeat_04_simple),
                getString(R.string.ds_alarm_repeat_05_simple),
                getString(R.string.ds_alarm_repeat_06_simple),
        };

        int alarmPosition = -1;
        if (getIntent() != null) {
            mAlarmList = getIntent().getParcelableArrayListExtra(EXTRA_ALARM_LIST);
            alarmPosition = getIntent().getIntExtra(EXTRA_ALARM_POSITION, -1);
        }
        if (mAlarmList == null) {
            mAlarmList = new ArrayList<>(1);
        }
        if (alarmPosition < 0 || alarmPosition >= mAlarmList.size()) {
            mEdit = false;//新增模式
            mAlarm = new WristbandAlarm();
            mAlarm.setAlarmId(WristbandAlarm.findNewAlarmId(mAlarmList));
            Date date = new Date();
            mAlarm.setHour(date.getHours());
            mAlarm.setMinute(date.getMinutes());
            mAlarmList.add(mAlarm);
        } else {
            mEdit = true;
            mAlarm = mAlarmList.get(alarmPosition);
        }

        //新增模式不显示删除闹钟
        mRlAlarmDelete.setVisibility(mEdit ? View.VISIBLE : View.GONE);

        NumericWheelAdapter hourAdapter;

        if (is24HourFormat) {
            mAmPmWheelView.setVisibility(View.GONE);
            hourAdapter = new NumericWheelAdapter(this, 0, 23, "%02d");
        } else {
            mAmPmWheelView.setVisibility(View.VISIBLE);
            String[] items = new String[]{getString(R.string.ds_alarm_am), getString(R.string.ds_alarm_pm)};
            ArrayWheelAdapter<String> ampmAdapter = new ArrayWheelAdapter<>(this, items);
            ampmAdapter.setItemResource(R.layout.item_wheel_view_text);
            ampmAdapter.setItemTextResource(R.id.tv_item_value);
            mAmPmWheelView.setVisibleItems(7);
            mAmPmWheelView.setWheelBackground(android.R.color.white);
            mAmPmWheelView.setViewAdapter(ampmAdapter);
            hourAdapter = new NumericWheelAdapter(this, 1, 12, "%02d");
        }
        hourAdapter.setItemResource(R.layout.item_wheel_view_text);
        hourAdapter.setItemTextResource(R.id.tv_item_value);

        NumericWheelAdapter minuteAdapter = new NumericWheelAdapter(this, 0, 59, "%02d");
        minuteAdapter.setItemResource(R.layout.item_wheel_view_text);
        minuteAdapter.setItemTextResource(R.id.tv_item_value);

        mHourWheelView.setVisibleItems(7);
        mHourWheelView.setWheelBackground(android.R.color.white);
        mHourWheelView.setViewAdapter(hourAdapter);
        mMinuteWheelView.setVisibleItems(7);
        mMinuteWheelView.setWheelBackground(android.R.color.white);
        mMinuteWheelView.setViewAdapter(minuteAdapter);

        updateUI();

        mSectionItemAlarmRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmDetailActivity.this, AlarmRepeatActivity.class);
                intent.putExtra(EXTRA_ALARM_REPEAT, mAlarm.getRepeat());
                startActivityForResult(intent, 1);
            }
        });
        mSectionItemAlarmLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EditLabelDialogFragment().show(getSupportFragmentManager(), null);
            }
        });
        mRlAlarmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });
    }

    private void updateUI() {
        if (mEdit) {
            getSupportActionBar().setTitle(R.string.ds_alarm_edit);
        } else {
            getSupportActionBar().setTitle(R.string.ds_alarm_add);
        }

        int hour = mAlarm.getHour();
        int minute = mAlarm.getMinute();

        if (hour == 24 && minute == 0) {
            if (is24HourFormat) {
                mHourWheelView.setCurrentItem(23);
                mMinuteWheelView.setCurrentItem(59);
            } else {
                mAmPmWheelView.setCurrentItem(0);
                mHourWheelView.setCurrentItem(12);
                mMinuteWheelView.setCurrentItem(0);
            }
        } else {
            if (is24HourFormat) {
                mHourWheelView.setCurrentItem(hour);
                mMinuteWheelView.setCurrentItem(minute);
            } else {
                if (hour < 12) {//上午
                    mAmPmWheelView.setCurrentItem(0);
                    if (hour == 0) {
                        hour = 12;
                    }
                } else {
                    mAmPmWheelView.setCurrentItem(1);
                    if (hour > 12) {
                        hour -= 12;
                    }
                }
                hour -= 1;//12小时制是从01开始的,要-1，因为item是从0开始
                mHourWheelView.setCurrentItem(hour);
                mMinuteWheelView.setCurrentItem(minute);
            }
        }

        mSectionItemAlarmLabel.getTextView().setText(mAlarm.getLabel());
        mSectionItemAlarmRepeat.getTextView().setText(repeatToSimpleStr(mAlarm.getRepeat()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            completed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void delete() {
        if (mAlarmList.indexOf(mAlarm) != -1) {
            mAlarmList.remove(mAlarm);
        }
        setAlarmList();
    }

    private void completed() {
        int hour = mHourWheelView.getCurrentItem();
        if (!is24HourFormat) {//12小时制
            hour += 1;//12小时制是从01开始的
            if (mAmPmWheelView.getCurrentItem() == 0) {//上午
                if (hour == 12) {
                    hour = 0;
                }
            } else {
                if (hour < 12) {
                    hour += 12;
                }
            }
        }

        mAlarm.setHour(hour);
        mAlarm.setMinute(mMinuteWheelView.getCurrentItem());
        mAlarm.setEnable(true);
//        mAlarm.adjustAlarm();//内部会自动adjust
        //如果没添加，就添加
        if (mAlarmList.indexOf(mAlarm) == -1) {
            mAlarmList.add(mAlarm);
        }
        setAlarmList();
    }

    private Disposable mDisposable;

    private void setAlarmList() {
        mDisposable = mWristManager
                .setAlarmList(mAlarmList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        toast(throwable.getMessage());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            mAlarm.setRepeat(data.getIntExtra(EXTRA_ALARM_REPEAT, 0));
            mSectionItemAlarmRepeat.getTextView().setText(repeatToSimpleStr(mAlarm.getRepeat()));
        }
    }

    //和AlarmListActivity里的方法一致
    private String repeatToSimpleStr(int repeat) {
        String text = null;
        int sumDays = 0;
        String resultString = "";
        for (int i = 0; i < 7; i++) {
            if (WristbandAlarm.isRepeatEnableIndex(repeat, i)) {
                sumDays++;
                resultString += (mDayValuesSimple[i] + " ");
            }
        }
        if (sumDays == 7) {
            text = getString(R.string.ds_alarm_repeat_every_day);
        } else if (sumDays == 0) {
            text = getString(R.string.ds_alarm_repeat_never);
        } else if (sumDays == 5) {
            boolean sat = !WristbandAlarm.isRepeatEnableIndex(repeat, 5);
            boolean sun = !WristbandAlarm.isRepeatEnableIndex(repeat, 6);
            if (sat && sun) {
                text = getString(R.string.ds_alarm_repeat_workdays);
            }
        } else if (sumDays == 2) {
            boolean sat = WristbandAlarm.isRepeatEnableIndex(repeat, 5);
            boolean sun = WristbandAlarm.isRepeatEnableIndex(repeat, 6);
            if (sat && sun) {
                text = getString(R.string.ds_alarm_repeat_weekends);
            }
        }
        if (text == null) {
            text = resultString;
        }
        return text;
    }


    @Override
    public String getAlarmLabel() {
        return mAlarm.getLabel();
    }

    @Override
    public void setAlarmLabel(String label) {
        mAlarm.setLabel(label);
        mSectionItemAlarmLabel.getTextView().setText(mAlarm.getLabel());
    }

}
