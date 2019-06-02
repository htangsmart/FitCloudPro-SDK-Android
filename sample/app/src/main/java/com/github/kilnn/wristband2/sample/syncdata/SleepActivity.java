package com.github.kilnn.wristband2.sample.syncdata;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.MyApplication;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.syncdata.db.SleepRecord;
import com.github.kilnn.wristband2.sample.syncdata.db.SyncDataDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SleepActivity extends BaseActivity {

    private Button mBtnDate;
    private TextView mTvDeepSleep;
    private TextView mTvLightSleep;
    private TextView mTvSoberSleep;
    private SleepDayView mSleepDayView;

    //Get dao to access database
    private SyncDataDao mSyncDataDao = MyApplication.getSyncDataDb().dao();
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        mBtnDate = findViewById(R.id.btn_date);
        mTvDeepSleep = findViewById(R.id.tv_deep_sleep);
        mTvLightSleep = findViewById(R.id.tv_light_sleep);
        mTvSoberSleep = findViewById(R.id.tv_sober_sleep);
        mSleepDayView = findViewById(R.id.sleep_day_view);

        mBtnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });
        selectDate(new Date());
    }

    private Date mSelectDate;

    private void datePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this, mListener,
                mSelectDate.getYear() + 1900, mSelectDate.getMonth(), mSelectDate.getDate());
        dialog.show();
    }

    private DatePickerDialog.OnDateSetListener mListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Date date = new Date();
            date.setYear(year - 1900);
            date.setMonth(month);
            date.setDate(dayOfMonth);
            selectDate(date);
        }
    };

    private void selectDate(Date date) {
        mSelectDate = date;
        //update date
        mBtnDate.setText(mFormat.format(mSelectDate));
        //query data
        SleepRecord sleepRecord = mSyncDataDao.querySleepRecord(date);
        if (sleepRecord == null) {
            mTvDeepSleep.setText(null);
            mTvLightSleep.setText(null);
            mTvSoberSleep.setText(null);
            mSleepDayView.setVisibility(View.GONE);
        } else {
            mTvDeepSleep.setText(getString(R.string.deep_sleep_hour_minute, sleepRecord.getDeepSleep() / 3600, (sleepRecord.getDeepSleep() % 3600) / 60));
            mTvLightSleep.setText(getString(R.string.light_sleep_hour_minute, sleepRecord.getLightSleep() / 3600, (sleepRecord.getLightSleep() % 3600) / 60));
            mTvSoberSleep.setText(getString(R.string.sober_sleep_hour_minute, sleepRecord.getSoberSleep() / 3600, (sleepRecord.getSoberSleep() % 3600) / 60));
            mSleepDayView.setVisibility(View.VISIBLE);
            mSleepDayView.setSleepDayDatas(sleepRecord);
        }
    }

}
