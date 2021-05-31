package com.github.kilnn.wristband2.sample.syncdata.db;

import com.github.kilnn.wristband2.sample.MyApplication;
import com.github.kilnn.wristband2.sample.mock.DbMock;
import com.github.kilnn.wristband2.sample.syncdata.db.converter.DateConverter;
import com.github.kilnn.wristband2.sample.syncdata.db.converter.TimeConverter;
import com.github.kilnn.wristband2.sample.utils.Utils;
import com.htsmart.wristband2.bean.data.BloodPressureData;
import com.htsmart.wristband2.bean.data.EcgData;
import com.htsmart.wristband2.bean.data.HeartRateData;
import com.htsmart.wristband2.bean.data.OxygenData;
import com.htsmart.wristband2.bean.data.RespiratoryRateData;
import com.htsmart.wristband2.bean.data.SleepData;
import com.htsmart.wristband2.bean.data.SleepItemData;
import com.htsmart.wristband2.bean.data.SportData;
import com.htsmart.wristband2.bean.data.SportItem;
import com.htsmart.wristband2.bean.data.StepData;
import com.htsmart.wristband2.bean.data.TodayTotalData;
import com.htsmart.wristband2.packet.SleepCalculateHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

@Dao
public abstract class SyncDataDao {

    //////////////////////////////////////Healthy Data//////////////////////////////////////////////

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(HeartRateItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(OxygenItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(BloodPressureItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(RespiratoryRateItem item);

    public void saveHeartRate(List<HeartRateData> datas) {
        if (datas == null || datas.size() <= 0) return;
        for (HeartRateData d : datas) {
            HeartRateItem item = new HeartRateItem();
            item.setHeartRate(d.getHeartRate());
            item.setTime(new Date(d.getTimeStamp()));
            insert(item);
        }
    }

    public void saveBloodPressure(List<BloodPressureData> datas) {
        if (datas == null || datas.size() <= 0) return;
        for (BloodPressureData d : datas) {
            BloodPressureItem item = new BloodPressureItem();
            item.setSbp(d.getSbp());
            item.setDbp(d.getDbp());
            item.setTime(new Date(d.getTimeStamp()));
            insert(item);
        }
    }

    public void saveOxygen(List<OxygenData> datas) {
        if (datas == null || datas.size() <= 0) return;
        for (OxygenData d : datas) {
            OxygenItem item = new OxygenItem();
            item.setOxygen(d.getOxygen());
            item.setTime(new Date(d.getTimeStamp()));
            insert(item);
        }
    }

    public void saveRespiratoryRate(List<RespiratoryRateData> datas) {
        if (datas == null || datas.size() <= 0) return;
        for (RespiratoryRateData d : datas) {
            RespiratoryRateItem item = new RespiratoryRateItem();
            item.setRate(d.getRate());
            item.setTime(new Date(d.getTimeStamp()));
            insert(item);
        }
    }

    @Query("SELECT * FROM HeartRateItem WHERE time BETWEEN :start AND :end ORDER BY time ASC")
    protected abstract List<HeartRateItem> queryHeartRateBetween(@TypeConverters(TimeConverter.class) Date start, @TypeConverters(TimeConverter.class) Date end);

    /**
     * Query Heart Rate data for a day
     */
    public List<HeartRateItem> queryHeartRate(Date date) {
        Calendar calendar = Calendar.getInstance();
        Date start = Utils.getDayStartTime(calendar, date);
        Date end = Utils.getDayEndTime(calendar, date);
        return queryHeartRateBetween(start, end);
    }

    //////////////////////////////////////Sleep Data//////////////////////////////////////////////

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(SleepRecord record);

    public void saveSleep(List<SleepData> datas) {
        if (datas == null || datas.size() <= 0) return;

        for (SleepData sleepData : datas) {
            SleepRecord sleepRecord = new SleepRecord();
            sleepRecord.setDate(new Date(sleepData.getTimeStamp()));//The date of the Sleep data

            //Convert SleepItemData to SleepItem
            if (sleepData.getItems() != null) {
                List<SleepItem> items = new ArrayList<>(sleepData.getItems().size());
                for (SleepItemData itemData : sleepData.getItems()) {
                    SleepItem item = new SleepItem();
                    item.setStatus(itemData.getStatus());
                    item.setStartTime(new Date(itemData.getStartTime()));
                    item.setEndTime(new Date(itemData.getEndTime()));
                    items.add(item);
                }
                sleepRecord.setDetail(items);
            }

            //TODO Warn :Merge existing day's sleep data
            //TODO Warn :In actual processing, if the sleep data comes from multiple different bracelets, you can choose to clear or merge according to your needs.
            SleepRecord existRecord = querySleepRecord(sleepRecord.getDate());
            if (existRecord != null) {
                List<SleepItem> existItems = existRecord.getDetail();
                if (existItems != null) {
                    if (sleepRecord.getDetail() != null) {
                        existItems.addAll(sleepRecord.getDetail());
                    }
                    sleepRecord.setDetail(existItems);
                }
            }

            //Calculate sleep time
            if (sleepRecord.getDetail() != null && sleepRecord.getDetail().size() > 0) {
                Collections.sort(sleepRecord.getDetail(), new Comparator<SleepItem>() {
                    @Override
                    public int compare(SleepItem o1, SleepItem o2) {
                        return (int) (o1.getStartTime().getTime() - o2.getStartTime().getTime());
                    }
                });
                int[] durations = SleepCalculateHelper.calculateDuration(sleepRecord.getDetail());

                sleepRecord.setDeepSleep(durations[0]);
                sleepRecord.setLightSleep(durations[1]);
                sleepRecord.setSoberSleep(durations[2]);
            }

            //insert
            insert(sleepRecord);
        }
    }

    /**
     * Query Sleep data for a day
     */
    @Query("SELECT * FROM SleepRecord WHERE date =:date")
    public abstract SleepRecord querySleepRecord(@TypeConverters(DateConverter.class) Date date);

    //////////////////////////////////////Sport Data//////////////////////////////////////////////

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(SportRecord record);

    public void saveSport(List<SportData> datas) {
        if (datas == null || datas.size() <= 0) return;
        for (SportData d : datas) {
            SportRecord sportRecord = new SportRecord();
            sportRecord.setSportType(d.getSportType());
            sportRecord.setTime(new Date(d.getTimeStamp()));
            sportRecord.setDuration(d.getDuration());
            sportRecord.setDistance(d.getDistance());
            sportRecord.setCalorie(d.getCalories());
            sportRecord.setStep(d.getSteps());

            List<SportItem> items = d.getItems();
            if (items != null && items.size() > 0) {
                List<SportHeartRate> heartRates = new ArrayList<>(items.size());
                for (SportItem item : items) {
                    if (item.getHeartRate() > 0) {
                        SportHeartRate sportHeartRate = new SportHeartRate();
                        sportHeartRate.setDuration(item.getDuration());
                        sportHeartRate.setValue(item.getHeartRate());
                        heartRates.add(sportHeartRate);
                    }
                }
                sportRecord.setHeartRates(heartRates);
            }

            sportRecord.setSportId(UUID.randomUUID());
            insert(sportRecord);
        }
    }

    /**
     * Query all Sport data
     */
    @Query("SELECT * FROM SportRecord")
    public abstract List<SportRecord> querySportRecord();

    //////////////////////////////////////Ecg Data//////////////////////////////////////////////

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(EcgRecord record);

    public void saveEcg(EcgData data) {
        if (data == null) return;
        EcgRecord ecgRecord = new EcgRecord();
        ecgRecord.setTime(new Date(data.getTimeStamp()));
        ecgRecord.setDetail(data.getItems());
        ecgRecord.setSample(data.getSample());

        ecgRecord.setEcgId(UUID.randomUUID());
        insert(ecgRecord);
    }

    /**
     * Query all Ecg data
     */
    @Query("SELECT * FROM EcgRecord")
    public abstract List<EcgRecord> queryEcgRecord();

    //////////////////////////////////////Step Data//////////////////////////////////////////////

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(StepItem item);

    public void saveStep(List<StepData> datas) {
        if (datas == null || datas.size() <= 0) return;
        for (StepData d : datas) {
            StepItem item = new StepItem();
            item.setStep(d.getStep());
            item.setTime(new Date(d.getTimeStamp()));
            insert(item);
        }
    }

    @Query("SELECT * FROM StepItem WHERE time BETWEEN :start AND :end ORDER BY time ASC")
    protected abstract List<StepItem> queryStepBetween(@TypeConverters(TimeConverter.class) Date start, @TypeConverters(TimeConverter.class) Date end);

    /**
     * Query Step data for a day
     */
    public List<StepItem> queryStep(Date date) {
        Calendar calendar = Calendar.getInstance();
        Date start = Utils.getDayStartTime(calendar, date);
        Date end = Utils.getDayEndTime(calendar, date);
        return queryStepBetween(start, end);
    }

    public void saveTodayTotalData(TodayTotalData data) {
        DbMock.setTodayTotalData(MyApplication.getInstance(), data);
    }

    public TodayTotalData queryTodayTotalData() {
        TodayTotalData data = DbMock.getTodayTotalData(MyApplication.getInstance());
        if (data == null) return null;
        if (Utils.isToday(new Date(data.getTimeStamp()))) {
            //TodayTotalData's time is today
            return data;
        } else {
            return null;
        }
    }

    @Query("DELETE FROM StepItem WHERE time BETWEEN :start AND :end")
    protected abstract void deleteStepBetween(@TypeConverters(TimeConverter.class) Date start, @TypeConverters(TimeConverter.class) Date end);

    /**
     * Clear today step data
     */
    public void clearTodayStep() {
        //Clear StepItem
        Date date = new Date();//today
        Calendar calendar = Calendar.getInstance();
        Date start = Utils.getDayStartTime(calendar, date);
        Date end = Utils.getDayEndTime(calendar, date);
        deleteStepBetween(start, end);
        //Clear TodayTotalData
        saveTodayTotalData(null);
    }

}

