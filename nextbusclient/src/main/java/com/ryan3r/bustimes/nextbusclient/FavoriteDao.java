package com.ryan3r.bustimes.nextbusclient;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

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
