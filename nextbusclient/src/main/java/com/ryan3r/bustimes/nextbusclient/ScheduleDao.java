package com.ryan3r.bustimes.nextbusclient;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface ScheduleDao {
    @Query("SELECT * FROM BusSchedule WHERE id = :id")
    BusSchedule get(String id);

    @Insert
    void add(BusSchedule schedule);

    @Update
    void set(BusSchedule schedule);
}
