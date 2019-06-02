package com.github.kilnn.wristband2.sample.syncdata;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.MyApplication;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.syncdata.db.StepItem;
import com.github.kilnn.wristband2.sample.syncdata.db.SyncDataDao;
import com.github.kilnn.wristband2.sample.util.Utils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.htsmart.wristband2.bean.data.TodayTotalData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StepActivity extends BaseActivity {

    private Button mBtnDate;
    private TextView mTvTotalStep;
    private BarChart mChart;

    //Get dao to access database
    private SyncDataDao mSyncDataDao = MyApplication.getSyncDataDb().dao();
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        mBtnDate = findViewById(R.id.btn_date);
        mTvTotalStep = findViewById(R.id.tv_total_step);
        mChart = findViewById(R.id.bar_chart);
        initChart();
        mBtnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });
        selectDate(new Date());
    }

    private Date mSelectDate;
    private List<StepItem> mHourStepItems;

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
        List<StepItem> items = mSyncDataDao.queryStep(date);

        TodayTotalData todayTotalData = mSyncDataDao.queryTodayTotalData();

        int totalStep = 0;
        if (Utils.isToday(mSelectDate) && todayTotalData != null) {
            totalStep = todayTotalData.getStep();
        } else {
            if (items != null && items.size() > 0) {
                for (StepItem item : items) {
                    totalStep += item.getStep();
                }
            }
        }
        mTvTotalStep.setText(getString(R.string.view_step_data_total_step, totalStep));
        //clear Chart
        mChart.clear();
        if (items == null || items.size() <= 0) {
            mHourStepItems = null;
        } else {
            mHourStepItems = transformToHourItem(items);
        }
        mHourStepItems = fix24Hour();

        ArrayList<BarEntry> yValues = new ArrayList<>();
        for (int i = 0; i < mHourStepItems.size(); i++) {
            StepItem stepItem = mHourStepItems.get(i);
            yValues.add(new BarEntry(i, stepItem.getStep()));
        }

        BarDataSet set1 = new BarDataSet(yValues, "DataSet");
        set1.setDrawValues(false);//不绘制柱状如的值
        set1.setColor(0xffffff00);
        set1.setHighLightAlpha(255);
        set1.setHighLightColor(0x99ffff00);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        BarData data = new BarData(dataSets);
        data.setBarWidth(0.65f);

        mChart.setData(data);
    }

    /**
     * Merge data in the same hour
     */
    private List<StepItem> transformToHourItem(List<StepItem> items) {
        Calendar calendar = Calendar.getInstance();
        //24-hour data
        List<StepItem> results = new ArrayList<>(24);
        StepItem temp = null;
        long tempStart = 0;
        long tempEnd = 0;
        long tempTime;

        for (StepItem item : items) {
            tempTime = item.getTime().getTime();
            if (temp != null) {
                if (tempTime >= tempStart && tempTime <= tempEnd) {
                    temp.plus(item);
                } else {
                    temp = null;
                }
            }
            if (temp == null) {
                temp = new StepItem();
                results.add(temp);
                temp.setTime(Utils.getHourStartTime(calendar, item.getTime()));
                temp.plus(item);
                tempStart = temp.getTime().getTime();
                tempEnd = Utils.getHourEndTime(calendar, item.getTime()).getTime();
            }
        }
        return results;
    }

    /**
     * Complete 24-hour nodes
     */
    private List<StepItem> fix24Hour() {
        List<StepItem> items = new ArrayList<>(24);//24 hours

        Calendar calendar = Calendar.getInstance();
        Utils.getDayStartTime(calendar, mSelectDate);

        int selectIndex = 0;
        for (int i = 0; i < 24; i++) {
            long timeInMillis = calendar.getTimeInMillis();

            StepItem stepItem = null;
            if (mHourStepItems != null && selectIndex < mHourStepItems.size()) {
                if (mHourStepItems.get(selectIndex).getTime().getTime() == timeInMillis) {
                    stepItem = mHourStepItems.get(selectIndex);
                    selectIndex++;
                }
            }

            if (stepItem == null) {
                stepItem = new StepItem();
                stepItem.setTime(new Date(timeInMillis));
            }
            items.add(stepItem);

            //Move to next hour
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1);
        }
        return items;
    }

    private void initChart() {
        mChart.setNoDataText(null);
        mChart.getDescription().setEnabled(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.getLegend().setEnabled(false);
        mChart.setScaleEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private SimpleDateFormat formatHour = new SimpleDateFormat("H:mm", Locale.getDefault());

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int index = (int) value;
                if (mHourStepItems == null || index >= mHourStepItems.size())
                    return "";
                return formatHour.format(mHourStepItems.get(index).getTime());
            }
        });
        xAxis.setLabelCount(6);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value == 0) {
                    return "";
                } else {
                    return String.valueOf((int) value);//只显示整数的刻度
                }
            }
        });
        leftAxis.setLabelCount(5);
        leftAxis.setSpaceTop(10f);
    }

}
