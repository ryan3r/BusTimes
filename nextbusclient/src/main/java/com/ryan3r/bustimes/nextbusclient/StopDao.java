package com.ryan3r.bustimes.nextbusclient;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

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
