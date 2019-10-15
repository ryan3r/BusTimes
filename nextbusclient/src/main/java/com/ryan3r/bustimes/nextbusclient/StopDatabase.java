package com.ryan3r.bustimes.nextbusclient;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {StopInfo.class, BusSchedule.class}, version = 6, exportSchema = false)
@TypeConverters({ListConverter.class, ArrayConverter.class})
public abstract class StopDatabase extends RoomDatabase {
    public abstract StopDao stopDao();
    public abstract ScheduleDao scheduleDao();
}
