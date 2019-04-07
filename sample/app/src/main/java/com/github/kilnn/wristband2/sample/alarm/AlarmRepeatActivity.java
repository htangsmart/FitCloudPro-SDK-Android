package com.github.kilnn.wristband2.sample.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.utils.ViewHolderUtils;
import com.htsmart.wristband2.bean.WristbandAlarm;

public class AlarmRepeatActivity extends BaseActivity {

    private int mRepeat;
    private CharSequence[] mDayValues = null;
    private InnerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_repeat);
        getSupportActionBar().setTitle(R.string.ds_alarm_repeat);

        if (getIntent() != null) {
            mRepeat = getIntent().getIntExtra(AlarmDetailActivity.EXTRA_ALARM_REPEAT, 0);
        }

        mDayValues = new CharSequence[]{
                getString(R.string.ds_alarm_repeat_00),
                getString(R.string.ds_alarm_repeat_01),
                getString(R.string.ds_alarm_repeat_02),
                getString(R.string.ds_alarm_repeat_03),
                getString(R.string.ds_alarm_repeat_04),
                getString(R.string.ds_alarm_repeat_05),
                getString(R.string.ds_alarm_repeat_06),
        };

        ListView listView = findViewById(R.id.list_view);
        mAdapter = new InnerAdapter();
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isRepeatOn = WristbandAlarm.isRepeatEnableIndex(mRepeat, position);
                if (isRepeatOn) {
                    mRepeat = WristbandAlarm.setRepeatEnableIndex(mRepeat, position, false);
                } else {
                    mRepeat = WristbandAlarm.setRepeatEnableIndex(mRepeat, position, true);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private class InnerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 7;
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
                convertView = getLayoutInflater().inflate(R.layout.item_alarm_repeat, parent, false);
            }
            TextView text_tv = ViewHolderUtils.get(convertView, R.id.text_tv);
            ImageView select_img = ViewHolderUtils.get(convertView, R.id.select_img);
            text_tv.setText(mDayValues[position]);
            select_img.setVisibility(WristbandAlarm.isRepeatEnableIndex(mRepeat, position) ? View.VISIBLE : View.INVISIBLE);
            return convertView;
        }
    }

    @Override
    public void onBackPressed() {
        completed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            completed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void completed() {
        Intent intent = new Intent();
        intent.putExtra(AlarmDetailActivity.EXTRA_ALARM_REPEAT, mRepeat);
        setResult(RESULT_OK, intent);
        finish();
    }

}
