package com.ryan3r.bustimes.nextbusclient;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RouteChoiceDao {
    @Insert
    void insert(RouteChoice choice);

    @Query("DELETE FROM `RouteChoice` WHERE stopId = :id")
    void removeByStop(String id);

    @Query("SELECT * FROM RouteChoice r WHERE r.stopId = :id")
    List<RouteChoice> get(String id);
}
