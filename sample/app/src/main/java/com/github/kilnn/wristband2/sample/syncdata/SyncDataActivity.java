package com.github.kilnn.wristband2.sample.syncdata;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.R;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.SyncDataRaw;
import com.htsmart.wristband2.bean.data.BloodPressureData;
import com.htsmart.wristband2.bean.data.EcgData;
import com.htsmart.wristband2.bean.data.HeartRateData;
import com.htsmart.wristband2.bean.data.OxygenData;
import com.htsmart.wristband2.bean.data.SleepData;
import com.htsmart.wristband2.bean.data.SportData;
import com.htsmart.wristband2.bean.data.StepData;
import com.htsmart.wristband2.bean.data.TodayTotalData;
import com.htsmart.wristband2.packet.SyncDataParser;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Sync Data
 */
public class SyncDataActivity extends BaseActivity {

    private WristbandManager mWristbandManager = WristbandApplication.getWristbandManager();
    private TextView mTvSyncState;
    private Button mBtnSync;

    private Disposable mSyncStateDisposable;
    private Disposable mSyncDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_data);

        mTvSyncState = findViewById(R.id.tv_sync_state);
        mBtnSync = findViewById(R.id.btn_sync);
        mBtnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync_data();
            }
        });

        mSyncStateDisposable = mWristbandManager.observerSyncDataState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer == null) return;
                        if (integer < 0) {//failed
                            if (integer == WristbandManager.SYNC_STATE_FAILED_DISCONNECTED) {
                                mTvSyncState.setText(R.string.sync_data_state_failed_disconnected);
                            } else if (integer == WristbandManager.SYNC_STATE_FAILED_CHECKING_ECG) {
                                mTvSyncState.setText(R.string.sync_data_state_failed_checking_ecg);
                            } else if (integer == WristbandManager.SYNC_STATE_FAILED_SAVING_ECG) {
                                mTvSyncState.setText(R.string.sync_data_state_failed_saving_ecg);
                            } else /*if(integer == WristbandManager.SYNC_STATE_FAILED_UNKNOWN)*/ {
                                mTvSyncState.setText(R.string.sync_data_state_failed_unknown);
                            }
                        } else if (integer == WristbandManager.SYNC_STATE_START) {
                            mTvSyncState.setText(R.string.sync_data_state_start);
                        } else if (integer == WristbandManager.SYNC_STATE_SUCCESS) {
                            mTvSyncState.setText(R.string.sync_data_state_success);
                        } else {
                            mTvSyncState.setText(getString(R.string.sync_data_state_progress, integer));
                        }
                    }
                });
    }

    private void sync_data() {
        if (mSyncDisposable != null && !mSyncDisposable.isDisposed()) {
            //Syncing
            return;
        }
        mSyncDisposable = mWristbandManager
                .syncData()
                .observeOn(Schedulers.io(), true)
                .flatMapCompletable(new Function<SyncDataRaw, CompletableSource>() {
                    @Override
                    public CompletableSource apply(SyncDataRaw syncDataRaw) throws Exception {
                        if (syncDataRaw.getDataType() == SyncDataParser.TYPE_HEART_RATE) {
                            List<HeartRateData> datas = SyncDataParser.parserHeartRateData(syncDataRaw.getDatas());
                            if (datas != null && datas.size() > 0) {
                                //TODO save data
                            }
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_BLOOD_PRESSURE) {
                            List<BloodPressureData> datas = SyncDataParser.parserBloodPressureData(syncDataRaw.getDatas());
                            if (datas != null && datas.size() > 0) {
                                //TODO save data
                            }
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_OXYGEN) {
                            List<OxygenData> datas = SyncDataParser.parserOxygenData(syncDataRaw.getDatas());
                            if (datas != null && datas.size() > 0) {
                                //TODO save data
                            }
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_SLEEP) {
                            List<SleepData> sleepDataList = SyncDataParser.parserSleepData(syncDataRaw.getDatas());
                            if (sleepDataList != null && sleepDataList.size() > 0) {
                                //TODO save data
                            }
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_SPORT) {
                            List<SportData> datas = SyncDataParser.parserSportData(syncDataRaw.getDatas(), syncDataRaw.getConfig());
                            if (datas != null && datas.size() > 0) {
                                //TODO save data
                            }
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_STEP) {
                            List<StepData> datas = SyncDataParser.parserStepData(syncDataRaw.getDatas());
                            if (datas != null && datas.size() > 0) {
                                //TODO save data
                            }
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_ECG) {
                            EcgData ecgData = SyncDataParser.parserEcgData(syncDataRaw.getDatas());
                            if (ecgData != null) {
                                //TODO save data
                            }
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_TOTAL_DATA) {
                            TodayTotalData data = SyncDataParser.parserTotalData(syncDataRaw.getDatas());
                            //TODO save data
                        }
                        return Completable.complete();
                    }
                })
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d("Sync", "Sync Data Success");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("Sync", "Sync Data Failed", throwable);
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSyncStateDisposable.dispose();
        if (mSyncDisposable != null && !mSyncDisposable.isDisposed()) {
            mSyncDisposable.dispose();
        }
    }

}
