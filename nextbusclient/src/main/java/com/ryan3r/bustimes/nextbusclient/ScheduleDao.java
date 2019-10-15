package com.ryan3r.bustimes.nextbusclient;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ScheduleDao {
    @Query("SELECT * FROM BusSchedule WHERE id = :id")
    BusSchedule get(String id);

    @Insert
    void add(BusSchedule schedule);

    @Update
    void set(BusSchedule schedule);
}
