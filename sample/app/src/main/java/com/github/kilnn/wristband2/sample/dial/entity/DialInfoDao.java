package com.github.kilnn.wristband2.sample.dial.entity;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public abstract class DialInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(DialInfo style);

    @Query("SELECT * FROM DialInfo WHERE projectNum=:projectNum AND lcd=:lcd AND toolVersion<=:toolVersion")
    public abstract Flowable<List<DialInfo>> query(String projectNum, int lcd, String toolVersion);

    @Delete
    public abstract void delete(DialInfo style);
}
