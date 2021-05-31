package com.github.kilnn.wristband2.sample.syncdata;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.MyApplication;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.syncdata.db.EcgRecord;
import com.github.kilnn.wristband2.sample.syncdata.db.SyncDataDao;
import com.github.kilnn.wristband2.sample.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EcgActivity extends BaseActivity {

    private EcgView mEcgView;
    private ListView mListView;
    private List<EcgRecord> mRecords;

    //Get dao to access database
    private SyncDataDao mSyncDataDao = MyApplication.getSyncDataDb().dao();
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg);
        mEcgView = findViewById(R.id.ecg_view);
        mListView = findViewById(R.id.list_view);
        mRecords = mSyncDataDao.queryEcgRecord();
        InnerAdapter adapter = new InnerAdapter();
        mListView.setAdapter(adapter);
        if (mRecords != null && mRecords.size() > 0) {
            clickEcgRecord(mRecords.get(0));
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickEcgRecord(mRecords.get(position));
            }
        });
    }

    private void clickEcgRecord(EcgRecord record) {
        mEcgView.clearData();
        mEcgView.setMode(EcgView.MODE_NORMAL);
        mEcgView.setSampleRate(record.getSample());
        mEcgView.addDataAndScrollToLast(record.getIntArrays());
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
                convertView = getLayoutInflater().inflate(R.layout.layout_ecg_item, parent, false);
            }
            TextView tv_ecg_time = Utils.get(convertView, R.id.tv_ecg_time);
            TextView tv_ecg_sample = Utils.get(convertView, R.id.tv_ecg_sample);
            EcgRecord record = mRecords.get(position);
            tv_ecg_time.setText(mFormat.format(record.getTime()));
            tv_ecg_sample.setText(getString(R.string.view_ecg_data_sample, record.getSample()));
            return convertView;
        }
    }

}
