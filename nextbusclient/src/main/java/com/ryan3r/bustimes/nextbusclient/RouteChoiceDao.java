package com.ryan3r.bustimes.nextbusclient;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface RouteChoiceDao {
    @Insert
    void insert(RouteChoice info);

    @Query("DELETE FROM `RouteChoice` WHERE stopId = :id")
    void removeByStop(String id);

    @Query("SELECT * FROM RouteChoice r WHERE r.stopId = :id")
    List<RouteChoice> get(String id);
}
