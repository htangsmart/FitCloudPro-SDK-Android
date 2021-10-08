package com.github.kilnn.wristband2.sample.mock;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.kilnn.wristband2.sample.utils.Utils;
import com.htsmart.wristband2.bean.data.EcgData;
import com.htsmart.wristband2.bean.data.HeartRateData;
import com.htsmart.wristband2.bean.data.SleepData;
import com.htsmart.wristband2.bean.data.SleepItemData;
import com.htsmart.wristband2.bean.data.SportData;
import com.htsmart.wristband2.bean.data.SportItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class AppFakeDataProvider {

    public static final boolean ENABLED = false;

    private static SharedPreferences openSp(Context context) {
        return context.getSharedPreferences("AppFakeDataProvider", Context.MODE_PRIVATE);
    }

    /**
     * Create Heart Rate fake data for test
     */
    public static List<HeartRateData> fakeHeartRate(Context context) {
        SharedPreferences sharedPreferences = openSp(context);

        //Previous time
        long previousFakeTime = sharedPreferences.getLong("heart_rate_fake_time", 0);

        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        long endTime = date.getTime();//Current time
        long startTime = Utils.getDayStartTime(calendar, new Date()).getTime();//Today start time

        if (previousFakeTime > startTime) {
            startTime = previousFakeTime;
        }

        List<HeartRateData> items = new ArrayList<>();
        Random random = new Random();

        int sum = 0;
        for (long time = startTime; time < endTime; time += 5 * 60 * 1000L) {//Generate a fake data every five minutes
            HeartRateData item = new HeartRateData();
            item.setTimeStamp(time);
            item.setHeartRate(random.nextInt(60) + 40);
            items.add(item);
            previousFakeTime = time;

            sum += item.getHeartRate();
            if (items.size() >= 10) {
                //最多生成10条数据
                break;
            }
        }
        if (items.size() > 0) {
            Log.e("AppFakeDataProvider", "create HeartRate fake data: size " + items.size() + ", avg " + sum / items.size());
        }
        sharedPreferences.edit().putLong("heart_rate_fake_time", previousFakeTime).apply();

        return items;
    }


    /**
     * Create Sleep fake data for test
     */
    public static List<SleepData> fakeSleepRecord() {
        List<SleepData> list = new ArrayList<>();
        SleepData sleepData = new SleepData();

        Calendar calendar = Calendar.getInstance();
        Date todayStartTime = Utils.getDayStartTime(calendar, new Date());//Today start time
        sleepData.setTimeStamp(todayStartTime.getTime());

        List<SleepItemData> items = new ArrayList<>();

        Random random = new Random();
        int segmentCount = random.nextInt(3) + 4;

        Date yesterdayStartTime = Utils.getExpireLimitTime(calendar, 1);
        yesterdayStartTime.setHours(random.nextInt(4) + 19);
        yesterdayStartTime.setMinutes(random.nextInt(60));

        long startTime = yesterdayStartTime.getTime();
        for (int i = 0; i < segmentCount; i++) {
            SleepItemData sleepItem = new SleepItemData();
            //第一段值不能是清醒。
            sleepItem.setStatus(i == 0 ? random.nextInt(2) + 1 : random.nextInt(3) + 1);
            sleepItem.setStartTime(startTime);
            long duration = random.nextInt(60) * 60 * 1000L;
            startTime += duration;
            sleepItem.setEndTime(startTime);

            SleepItemData previousItem = items.size() > 0 ? items.get(items.size() - 1) : null;
            if (previousItem != null && sleepItem.getStatus() == previousItem.getStatus()) {
                previousItem.setEndTime(sleepItem.getEndTime());
            } else {
                items.add(sleepItem);
            }
        }
        sleepData.setItems(items);
        Log.e("AppFakeDataProvider", "create Sleep fake data");

        list.add(sleepData);
        return list;
    }

    /**
     * Create Sport fake data for test
     */
    public static List<SportData> fakeSportRecord() {
        List<SportData> list = new ArrayList<>();
        SportData sportData = new SportData();
        Random random = new Random();

        sportData.setTimeStamp(System.currentTimeMillis());
        sportData.setSportType(random.nextInt(9) * 4 + 1);
        sportData.setDuration(random.nextInt(5000));
        sportData.setDistance(random.nextInt(100));
        sportData.setCalories(random.nextInt(100));
        sportData.setSteps(random.nextInt(500));
        if (random.nextInt(10) > 5) {
            //create SportHR
            List<SportItem> items = new ArrayList<>();
            int duration = 0;
            while (duration < sportData.getDuration()) {
                SportItem item = new SportItem();
                item.setDuration(duration);
                item.setHeartRate(random.nextInt(100));
                items.add(item);
                duration += 300;
            }
            sportData.setItems(items);
        }
        Log.e("AppFakeDataProvider", "create Sport fake data");

        list.add(sportData);
        return list;
    }

    /**
     * Create Ecg fake data for test
     */
    public static EcgData fakeEcgRecord() {
        EcgData ecgData = new EcgData();
        ecgData.setTimeStamp(System.currentTimeMillis());
        List<Integer> detail = new ArrayList<>(1000);
        ecgData.setItems(detail);
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            detail.add(random.nextInt(10000) + 10000);
        }
        ecgData.setSample(EcgData.DEFAULT_SAMPLE);
        Log.e("AppFakeDataProvider", "create Ecg fake data");
        return ecgData;
    }
}