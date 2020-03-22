package com.github.kilnn.wristband2.sample.syncdata;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.MyApplication;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.mock.AppFakeDataProvider;
import com.github.kilnn.wristband2.sample.mock.User;
import com.github.kilnn.wristband2.sample.mock.UserMock;
import com.github.kilnn.wristband2.sample.syncdata.db.SyncDataDao;
import com.github.kilnn.wristband2.sample.util.Utils;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.SyncDataRaw;
import com.htsmart.wristband2.bean.data.BloodPressureData;
import com.htsmart.wristband2.bean.data.EcgData;
import com.htsmart.wristband2.bean.data.HeartRateData;
import com.htsmart.wristband2.bean.data.OxygenData;
import com.htsmart.wristband2.bean.data.RespiratoryRateData;
import com.htsmart.wristband2.bean.data.SleepData;
import com.htsmart.wristband2.bean.data.SportData;
import com.htsmart.wristband2.bean.data.StepData;
import com.htsmart.wristband2.bean.data.TodayTotalData;
import com.htsmart.wristband2.packet.SyncDataParser;

import java.util.Calendar;
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

    private Disposable mSyncStateDisposable;
    private Disposable mSyncDisposable;

    //Get dao to access database
    private SyncDataDao mSyncDataDao = MyApplication.getSyncDataDb().dao();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_data);

        mTvSyncState = findViewById(R.id.tv_sync_state);

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

    /**
     * Users may quit sleep between 4 and 12:00
     */
    public static boolean isInExitSleepMonitorTime() {
        Calendar cd = Calendar.getInstance();
        int h = cd.get(Calendar.HOUR_OF_DAY);
        int m = cd.get(Calendar.MINUTE);
        int currentTime = h * 60 + m;
        return currentTime >= 4 * 60 && currentTime <= 12 * 60;
    }

    public void clickSync(View view) {
        if (mSyncDisposable != null && !mSyncDisposable.isDisposed()) {
            //Syncing
            return;
        }
        boolean syncManual = true;//Sync Manual
        if (syncManual && isInExitSleepMonitorTime()) {
            //Exit sleep monitor
            mWristbandManager.exitSleepMonitor().onErrorComplete().subscribe();
        }
        mSyncDisposable = mWristbandManager
                .syncData()
                .observeOn(Schedulers.io(), true)
                .flatMapCompletable(new Function<SyncDataRaw, CompletableSource>() {
                    @Override
                    public CompletableSource apply(SyncDataRaw syncDataRaw) throws Exception {
                        //parser sync data and save to database
                        if (syncDataRaw.getDataType() == SyncDataParser.TYPE_HEART_RATE) {
                            List<HeartRateData> datas = SyncDataParser.parserHeartRateData(syncDataRaw.getDatas());
                            mSyncDataDao.saveHeartRate(datas);
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_BLOOD_PRESSURE) {
                            List<BloodPressureData> datas = SyncDataParser.parserBloodPressureData(syncDataRaw.getDatas());
                            mSyncDataDao.saveBloodPressure(datas);
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_OXYGEN) {
                            List<OxygenData> datas = SyncDataParser.parserOxygenData(syncDataRaw.getDatas());
                            mSyncDataDao.saveOxygen(datas);
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_RESPIRATORY_RATE) {
                            List<RespiratoryRateData> datas = SyncDataParser.parserRespiratoryRateData(syncDataRaw.getDatas());
                            mSyncDataDao.saveRespiratoryRate(datas);
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_SLEEP) {
                            List<SleepData> datas = SyncDataParser.parserSleepData(syncDataRaw.getDatas(), syncDataRaw.getConfig());
                            mSyncDataDao.saveSleep(datas);
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_SPORT) {
                            List<SportData> datas = SyncDataParser.parserSportData(syncDataRaw.getDatas(), syncDataRaw.getConfig());
                            mSyncDataDao.saveSport(datas);
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_STEP) {
                            List<StepData> datas = SyncDataParser.parserStepData(syncDataRaw.getDatas(), syncDataRaw.getConfig());
                            if (datas != null && datas.size() > 0) {
                                if (syncDataRaw.getConfig().getWristbandVersion().isExtStepExtra()) {
                                    //The wristband supports automatic calculation of distance and calorie data
                                } else {
                                    //you need to calculate distance and calorie yourself.
                                    User user = UserMock.getLoginUser();
                                    float stepLength = Utils.getStepLength(user.getHeight(), user.isSex());
                                    for (StepData data : datas) {
                                        data.setDistance(Utils.step2Km(data.getStep(), stepLength));
                                        data.setCalories(Utils.km2Calories(data.getDistance(), user.getWeight()));
                                    }
                                }
                                //Only the step data is saved here. If you need distance and calorie data, you can choose according to the actual situation.
                                mSyncDataDao.saveStep(datas);
                            }
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_ECG) {
                            EcgData ecgData = SyncDataParser.parserEcgData(syncDataRaw.getDatas());
                            mSyncDataDao.saveEcg(ecgData);
                        } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_TOTAL_DATA) {
                            TodayTotalData data = SyncDataParser.parserTotalData(syncDataRaw.getDatas());
                            mSyncDataDao.saveTodayTotalData(data);
                        }
                        return Completable.complete();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (AppFakeDataProvider.ENABLED) {
                            mSyncDataDao.saveHeartRate(AppFakeDataProvider.fakeHeartRate(SyncDataActivity.this));
                            mSyncDataDao.saveSleep(AppFakeDataProvider.fakeSleepRecord());
                            mSyncDataDao.saveSport(AppFakeDataProvider.fakeSportRecord());
                            mSyncDataDao.saveEcg(AppFakeDataProvider.fakeEcgRecord());
                        }
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

    public void clickViewHeartRate(View view) {
        startActivity(new Intent(this, HeartRateActivity.class));
    }

    public void clickViewOxygen(View view) {
        toast(R.string.view_healthy_data_tips);
    }

    public void clickViewBloodPressure(View view) {
        toast(R.string.view_healthy_data_tips);
    }

    public void clickViewRespiratoryRate(View view) {
        toast(R.string.view_healthy_data_tips);
    }

    public void clickViewSleep(View view) {
        startActivity(new Intent(this, SleepActivity.class));
    }

    public void clickViewSport(View view) {
        startActivity(new Intent(this, SportActivity.class));
    }

    public void clickViewEcg(View view) {
        startActivity(new Intent(this, EcgActivity.class));
    }

    public void clickViewStep(View view) {
        startActivity(new Intent(this, StepActivity.class));
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
