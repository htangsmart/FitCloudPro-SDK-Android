package com.github.kilnn.wristband2.sample.syncdata.db.converter;


import android.arch.persistence.room.TypeConverter;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.github.kilnn.wristband2.sample.syncdata.db.SportHeartRate;

import java.util.List;

public class SportHeartRateConverter {

    @TypeConverter
    public static String fromList(List<SportHeartRate> list) {
        return list == null ? null : JSON.toJSONString(list);
    }

    @TypeConverter
    public static List<SportHeartRate> fromStr(String str) {
        return TextUtils.isEmpty(str) ? null : JSON.parseArray(str, SportHeartRate.class);
    }

}
