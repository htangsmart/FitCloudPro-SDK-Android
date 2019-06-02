package com.github.kilnn.wristband2.sample.syncdata;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.MyApplication;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.syncdata.db.SportHeartRate;
import com.github.kilnn.wristband2.sample.syncdata.db.SportRecord;
import com.github.kilnn.wristband2.sample.syncdata.db.SyncDataDao;
import com.github.kilnn.wristband2.sample.util.Utils;
import com.htsmart.wristband2.bean.data.SportData;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SportActivity extends BaseActivity {

    private ListView mListView;
    private List<SportRecord> mRecords;

    //Get dao to access database
    private SyncDataDao mSyncDataDao = MyApplication.getSyncDataDb().dao();
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport);
        mListView = findViewById(R.id.list_view);
        mRecords = mSyncDataDao.querySportRecord();
        InnerAdapter adapter = new InnerAdapter();
        mListView.setAdapter(adapter);
    }

    private class InnerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mRecords == null ? 0 : mRecords.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.layout_sport_item, parent, false);
            }
            TextView tv_sport_time = Utils.get(convertView, R.id.tv_sport_time);
            TextView tv_sport_type = Utils.get(convertView, R.id.tv_sport_type);
            TextView tv_sport_duration = Utils.get(convertView, R.id.tv_sport_duration);
            TextView tv_sport_calories = Utils.get(convertView, R.id.tv_sport_calories);
            TextView tv_sport_step = Utils.get(convertView, R.id.tv_sport_step);
            TextView tv_sport_distance = Utils.get(convertView, R.id.tv_sport_distance);
            TextView tv_sport_hr = Utils.get(convertView, R.id.tv_sport_hr);

            SportRecord record = mRecords.get(position);

            tv_sport_time.setText(mFormat.format(record.getTime()));

            switch (record.getSportType()) {
                case SportData.SPORT_RIDE:
                    tv_sport_type.setText(R.string.view_sport_type_ride);
                    break;

                case SportData.SPORT_OD:
                    tv_sport_type.setText(R.string.view_sport_type_od);
                    break;

                case SportData.SPORT_ID:
                    tv_sport_type.setText(R.string.view_sport_type_id);
                    break;

                case SportData.SPORT_WALK:
                    tv_sport_type.setText(R.string.view_sport_type_walk);
                    break;

                case SportData.SPORT_CLIMB:
                    tv_sport_type.setText(R.string.view_sport_type_climb);
                    break;

                case SportData.SPORT_BB:
                    tv_sport_type.setText(R.string.view_sport_type_bb);
                    break;

                case SportData.SPORT_SWIM:
                    tv_sport_type.setText(R.string.view_sport_type_swim);
                    break;

                case SportData.SPORT_BADMINTON:
                    tv_sport_type.setText(R.string.view_sport_type_badminton);
                    break;

                case SportData.SPORT_FOOTBALL:
                default:
                    tv_sport_type.setText(R.string.view_sport_type_football);
                    break;
            }

            int duration = record.getDuration();//秒
            String durationStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", duration / 3600, duration % 3600 / 60, duration % 3600 % 60);
            tv_sport_duration.setText(durationStr);

            tv_sport_calories.setText(getString(R.string.view_sport_data_calories, String.valueOf(Utils.roundDownFloat(record.getCalorie(), 1))));

            if (record.getSportType() == SportData.SPORT_RIDE
                    || record.getSportType() == SportData.SPORT_SWIM) {
                tv_sport_step.setVisibility(View.INVISIBLE);
            } else {
                tv_sport_step.setVisibility(View.VISIBLE);
                tv_sport_step.setText(getString(R.string.view_sport_data_step, record.getStep()));
            }

            if (record.getSportType() == SportData.SPORT_RIDE
                    || record.getSportType() == SportData.SPORT_SWIM
                    || record.getSportType() == SportData.SPORT_BB
                    || record.getSportType() == SportData.SPORT_BADMINTON
                    || record.getSportType() == SportData.SPORT_FOOTBALL) {
                tv_sport_distance.setVisibility(View.INVISIBLE);
            } else {
                tv_sport_distance.setVisibility(View.VISIBLE);
                tv_sport_distance.setText(getString(R.string.view_sport_data_distance, String.valueOf(Utils.roundDownFloat(record.getDistance(), 1))));
            }

            if (record.getHeartRates() == null || record.getHeartRates().size() <= 0) {
                tv_sport_hr.setVisibility(View.INVISIBLE);
            } else {
                tv_sport_hr.setVisibility(View.VISIBLE);
                int sum = 0;
                for (SportHeartRate h : record.getHeartRates()) {
                    sum += h.getValue();
                }
                tv_sport_hr.setText(getString(R.string.view_sport_data_hr, sum / record.getHeartRates().size()));
            }

            return convertView;
        }
    }

}
