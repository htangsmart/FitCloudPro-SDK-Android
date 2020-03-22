package com.github.kilnn.wristband2.sample.syncdata;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.MyApplication;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.syncdata.db.HeartRateItem;
import com.github.kilnn.wristband2.sample.syncdata.db.SyncDataDao;
import com.github.kilnn.wristband2.sample.util.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HeartRateActivity extends BaseActivity {

    private Button mBtnDate;
    private LineChart mChart;

    //Get dao to access database
    private SyncDataDao mSyncDataDao = MyApplication.getSyncDataDb().dao();
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);

        mBtnDate = findViewById(R.id.btn_date);
        mChart = findViewById(R.id.line_chart);
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
        List<HeartRateItem> items = mSyncDataDao.queryHeartRate(date);
        //clear Chart
        mChart.clear();
        if (items == null || items.size() <= 0) {
            return;
        }

        //adjust data
        final List<String> xValues = new ArrayList<>();
        final List<Entry> yValues = new ArrayList<>();

        int startTimeMinute = (int) (items.get(0).getTime().getTime() / 1000 / 60);
        int endTimeMinute = (int) (items.get(items.size() - 1).getTime().getTime() / 1000 / 60);

        Date tempDate = new Date();
        int dataIndex = 0;
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        for (int i = startTimeMinute; i <= endTimeMinute; i++) {
            tempDate.setTime(i * 60 * 1000L);
            xValues.add(hourFormat.format(tempDate));
            if (dataIndex < items.size()) {
                HeartRateItem item = items.get(dataIndex);
                if ((int) (item.getTime().getTime() / 1000 / 60) == i) {
                    yValues.add(new Entry(i - startTimeMinute, item.getHeartRate()));
                    dataIndex++;
                }
            }
        }

        if (xValues.size() <= 0 || yValues.size() <= 0) {
            return;
        }

        //update chart
        mChart.getXAxis().setLabelCount(2, xValues.size() >= 2);

        mChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int index = (int) value;
                if (index < 0 || index >= xValues.size()) return "";
                return xValues.get(index);
            }
        });

        LineData data = new LineData();
        data.setDrawValues(false);

        LineDataSet set = new LineDataSet(yValues, "DataSet1");
        set.setDrawCircleHole(false);
        set.setDrawCircles(true);
        set.setDrawValues(false);
        set.setCircleColor(getResources().getColor(R.color.colorPrimary));
        set.setCircleRadius(2);
        set.setLineWidth(1.5f);
        set.setHighLightColor(Color.WHITE);
        set.setColor(getResources().getColor(R.color.colorPrimary));
        set.setDrawHorizontalHighlightIndicator(false);
        data.addDataSet(set);

        mChart.setData(data);
        mChart.animateXY(0, 2000);
        mChart.invalidate();
    }

    private void initChart() {
        mChart.setNoDataText("No Data");
        mChart.setNoDataTextColor(Color.GRAY);
        mChart.getDescription().setEnabled(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.getLegend().setEnabled(false);
        mChart.setScaleEnabled(false);

        int textColorPrimary = Utils.getColor(this, android.R.attr.textColorPrimary);
        int textColorSecondary = Utils.getColor(this, android.R.attr.textColorSecondary);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(textColorSecondary);
        xAxis.setTextColor(textColorSecondary);
        xAxis.setTextSize(12);
        xAxis.setLabelCount(2, true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(30);
        leftAxis.setAxisMaximum(200);
        leftAxis.setTextColor(textColorPrimary);
        leftAxis.setTextSize(12);
        leftAxis.setLabelCount(5, true);
        leftAxis.setSpaceTop(10f);
    }

}
