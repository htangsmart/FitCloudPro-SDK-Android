package com.topstep.fitcloud.sample2.ui.realtime;

import androidx.annotation.NonNull;

import com.github.kilnn.tool.util.RoundUtilKt;

import java.util.Date;

public class HealthRealtimeValueHolder {

    private float maxOne = 0;
    private float minOne = 0;
    private float sumOne;

    private float maxTwo = 0;
    private float minTwo = 0;
    private float sumTwo;

    private float lastOne;
    private float lastTwo;

    private int count = 0;

    public void reset() {
        maxOne = minOne = sumOne = maxTwo = minTwo = sumTwo = lastOne = lastTwo = count = 0;
    }

    public void addValue(float one) {
        if (count == 0) {//第一个数据，还原UI，显示数值
            maxOne = one;
            minOne = one;
        } else {
            if (maxOne < one) maxOne = one;
            if (minOne > one) minOne = one;
        }
        sumOne += one;
        count++;
        lastOne = one;
    }

    public void addValue(float one, float two) {
        if (count == 0) {//First data
            maxOne = one;
            minOne = one;
            maxTwo = two;
            minTwo = two;
        } else {
            if (maxOne < one) maxOne = one;
            if (minOne > one) minOne = one;
            if (maxTwo < two) maxTwo = two;
            if (minTwo > two) minTwo = two;
        }
        sumOne += one;
        sumTwo += two;
        count++;
        lastOne = one;
        lastTwo = two;
    }

    public int intMaxOne() {
        return (int) maxOne;
    }

    public int intMinOne() {
        return (int) minOne;
    }

    public int intAvgOne() {
        return ((int) sumOne) / count;
    }

    public int intMaxTwo() {
        return (int) maxTwo;
    }

    public int intMinTwo() {
        return (int) minTwo;
    }

    public int intAvgTwo() {
        return ((int) sumTwo) / count;
    }

    public float floatAvgOne() {
        return sumOne / count;
    }

    private float floatAvgTwo() {
        return sumTwo / count;
    }

    public int count() {
        return count;
    }

    @NonNull
    public HeartRateRealtime toHeartRate() {
        return new HeartRateRealtime(
                new Date(),
                intAvgOne()
        );
    }

    @NonNull
    public OxygenRealtime toOxygen() {
        return new OxygenRealtime(
                new Date(),
                intAvgOne()
        );
    }

    @NonNull
    public BloodPressureRealtime toBloodPressure(boolean isAirPumpBloodPressure) {
        if (isAirPumpBloodPressure) {
            return new BloodPressureRealtime(new Date(), (int) lastOne, (int) lastTwo);
        } else {
            return new BloodPressureRealtime(new Date(), intAvgOne(), intAvgTwo());
        }
    }

    @NonNull
    public TemperatureRealtime toTemperature() {
        return new TemperatureRealtime(
                new Date(),
                maxOne, //Only the last one has a value for body temperature, so the maximum value is used instead of the average value,
                RoundUtilKt.roundDown2(floatAvgTwo())
        );
    }

    @NonNull
    public PressureRealtime toPressure() {
        return new PressureRealtime(
                new Date(),
                intAvgOne()
        );
    }

}
