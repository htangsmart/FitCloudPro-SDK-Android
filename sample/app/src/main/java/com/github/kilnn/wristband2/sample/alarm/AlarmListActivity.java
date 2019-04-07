package com.github.kilnn.wristband2.sample.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.widget.DataLceView;
import com.github.kilnn.wristband2.sample.widget.SwipeItemLayout;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.WristbandAlarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class AlarmListActivity extends BaseActivity {

    private DataLceView mDataLceView;
    private RecyclerView mRecyclerView;

    private ArrayList<WristbandAlarm> mAlarmList = new ArrayList<>(8);
    private AlarmListAdapter mAdapter;
    private boolean is24HourFormat;
    private CharSequence[] mDayValuesSimple;
    private boolean mAlarmUpdated;

    private WristbandManager mWristManager = WristbandApplication.getWristbandManager();
    private Disposable mRequestAlarmListDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);
        getSupportActionBar().setTitle(R.string.ds_alarm_list);

        mDataLceView = findViewById(R.id.lce_view);
        mRecyclerView = findViewById(R.id.recycler_view);

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

        mDataLceView.setLoadingListener(new DataLceView.LoadingListener() {
            @Override
            public void lceLoad() {
                refreshAlarmList();
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.addOnItemTouchListener(new SwipeItemLayout.OnSwipeItemTouchListener(this));
        mAdapter = new AlarmListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        refreshAlarmList();
    }

    private void refreshAlarmList() {
        if (mRequestAlarmListDisposable != null && !mRequestAlarmListDisposable.isDisposed()) {
            return;
        }
        mRequestAlarmListDisposable = mWristManager
                .requestAlarmList()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        mAlarmUpdated = false;
                        invalidateOptionsMenu();
                        mDataLceView.lceShowLoading();
                    }
                })
                .subscribe(new Consumer<List<WristbandAlarm>>() {
                    @Override
                    public void accept(List<WristbandAlarm> alarms) throws Exception {
                        mAlarmUpdated = true;
                        if (alarms.size() <= 0) {
                            mDataLceView.lceShowError(R.string.ds_alarm_no_data);
                        } else {
                            mDataLceView.lceShowContent();
                        }
                        mAlarmList.clear();
                        mAlarmList.addAll(alarms);
                        Collections.sort(mAlarmList, mAlarmComparator);
                        mAdapter.notifyDataSetChanged();
                        invalidateOptionsMenu();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        toast(throwable.getMessage());
                        mDataLceView.lceShowError(R.string.tip_load_error);
                    }
                });
    }


    private Disposable mSetAlarmListDisposable;

    private void setAlarmList(final List<WristbandAlarm> alarmList) {
        mSetAlarmListDisposable = mWristManager
                .setAlarmList(alarmList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        mAlarmList.clear();
                        mAlarmList.addAll(alarmList);
                        Collections.sort(mAlarmList, mAlarmComparator);
                        mAdapter.notifyDataSetChanged();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        toast(throwable.getMessage());
                    }
                });
    }

    private Comparator<WristbandAlarm> mAlarmComparator = new Comparator<WristbandAlarm>() {
        @Override
        public int compare(WristbandAlarm o1, WristbandAlarm o2) {
            int v1 = o1.getHour() * 60 + o1.getMinute();
            int v2 = o2.getHour() * 60 + o2.getMinute();
            if (v1 > v2) {
                return 1;
            } else if (v1 < v2) {
                return -1;
            } else {
                return o1.getAlarmId() - o2.getAlarmId();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRequestAlarmListDisposable != null && !mRequestAlarmListDisposable.isDisposed()) {
            mRequestAlarmListDisposable.dispose();
        }
        if (mSetAlarmListDisposable != null && !mSetAlarmListDisposable.isDisposed()) {
            mSetAlarmListDisposable.dispose();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mAlarmUpdated) {
            getMenuInflater().inflate(R.menu.menu_add, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_id_add) {
            if (mAlarmList.size() >= 5) {
                toast(R.string.ds_alarm_limit_count);
            } else {
                Intent intent = new Intent(AlarmListActivity.this, AlarmDetailActivity.class);
                intent.putParcelableArrayListExtra(AlarmDetailActivity.EXTRA_ALARM_LIST, mAlarmList);
                intent.putExtra(AlarmDetailActivity.EXTRA_ALARM_POSITION, -1);
                startActivityForResult(intent, 1);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            refreshAlarmList();
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

    public class AlarmViewHolder extends RecyclerView.ViewHolder {
        public View layout_alarm_content;
        public TextView tv_am_pm;
        public TextView tv_time;
        public TextView tv_label;
        public TextView tv_repeat;
        public SwitchCompat open_switch;
        public TextView tv_delete;

        private AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            layout_alarm_content = itemView.findViewById(R.id.layout_alarm_content);
            tv_am_pm = itemView.findViewById(R.id.tv_am_pm);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_label = itemView.findViewById(R.id.tv_label);
            tv_repeat = itemView.findViewById(R.id.tv_repeat);
            open_switch = itemView.findViewById(R.id.open_switch);
            tv_delete = itemView.findViewById(R.id.tv_delete);
        }
    }

    public class AlarmListAdapter extends RecyclerView.Adapter<AlarmViewHolder> {

        @NonNull
        @Override
        public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new AlarmViewHolder(getLayoutInflater().inflate(R.layout.item_alarm_list, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(final @NonNull AlarmViewHolder holder, int position) {
            final WristbandAlarm alarm = mAlarmList.get(position);

            if (is24HourFormat) {//24小时制
                holder.tv_am_pm.setVisibility(View.GONE);
                holder.tv_time.setText(alarm.getHour() + ":" + String.format("%02d", alarm.getMinute()));
            } else {
                holder.tv_am_pm.setVisibility(View.VISIBLE);
                int hour = alarm.getHour();
                if (hour < 12) {//上午
                    holder.tv_am_pm.setText(R.string.ds_alarm_am);
                    if (hour == 0) {
                        hour = 12;
                    }
                } else {
                    holder.tv_am_pm.setText(R.string.ds_alarm_pm);
                    if (hour > 12) {
                        hour -= 12;
                    }
                }
                holder.tv_time.setText(hour + ":" + String.format("%02d", alarm.getMinute()));
            }
            holder.tv_label.setText(alarm.getLabel());
            holder.tv_repeat.setText(repeatToSimpleStr(alarm.getRepeat()));
            holder.open_switch.setOnCheckedChangeListener(null);

            holder.open_switch.setChecked(alarm.isEnable());
            holder.open_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //复制数组，除开要删除的alarm
                    List<WristbandAlarm> alarmList = new ArrayList<>(mAlarmList.size());
                    alarmList.addAll(mAlarmList);
                    alarmList.remove(alarm);
                    try {
                        WristbandAlarm cloneAlarm = (WristbandAlarm) alarm.clone();
                        cloneAlarm.setEnable(isChecked);
                        alarmList.add(cloneAlarm);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    setAlarmList(alarmList);
                }
            });
            holder.tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //复制数组，除开要删除的alarm
                    List<WristbandAlarm> alarmList = new ArrayList<>(mAlarmList.size());
                    alarmList.addAll(mAlarmList);
                    alarmList.remove(alarm);
                    setAlarmList(alarmList);
                }
            });
            holder.layout_alarm_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AlarmListActivity.this, AlarmDetailActivity.class);
                    intent.putParcelableArrayListExtra(AlarmDetailActivity.EXTRA_ALARM_LIST, mAlarmList);
                    intent.putExtra(AlarmDetailActivity.EXTRA_ALARM_POSITION, holder.getAdapterPosition());
                    startActivityForResult(intent, 1);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAlarmList.size();
        }
    }

}
