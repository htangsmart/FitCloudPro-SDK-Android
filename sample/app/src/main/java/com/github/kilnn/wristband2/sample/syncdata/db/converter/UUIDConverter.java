package com.github.kilnn.wristband2.sample.syncdata.db.converter;

import android.arch.persistence.room.TypeConverter;

import java.util.UUID;

public class UUIDConverter {

    @TypeConverter
    public static String fromUUID(UUID uuid) {
        return uuid.toString();
    }

    @TypeConverter
    public static UUID fromStr(String str) {
        return UUID.fromString(str);
    }
}
