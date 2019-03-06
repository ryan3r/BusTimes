package com.ryan3r.bustimes.nextbusclient;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {StopInfo.class, BusSchedule.class}, version = 6, exportSchema = false)
@TypeConverters({ListConverter.class, ArrayConverter.class})
public abstract class StopDatabase extends RoomDatabase {
    public abstract StopDao stopDao();
    public abstract ScheduleDao scheduleDao();
}
