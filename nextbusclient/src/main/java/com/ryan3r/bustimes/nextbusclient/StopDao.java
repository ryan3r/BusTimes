package com.ryan3r.bustimes.nextbusclient;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface StopDao {
    @Insert
    void addStop(StopInfo info);

    @Query("SELECT * FROM StopInfo WHERE StopInfo.stopId = :id")
    StopInfo[] getStop(String id);

    @Query("SELECT * FROM StopInfo")
    List<StopInfo> getAll();
}
