package com.ryan3r.bustimes.nextbusclient;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface FavoriteDao {
    @Insert
    void insert(FavoriteInfo info);

    @Update
    void update(FavoriteInfo info);

    @Delete
    void remove(FavoriteInfo info);

    @Query("SELECT * FROM FavoriteInfo WHERE FavoriteInfo.stopId = :id")
    FavoriteInfo[] get(String id);

    @Query("SELECT * FROM FavoriteInfo")
    FavoriteInfo[] getAll();
}
