package com.ryan3r.bustimes.nextbusclient;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {FavoriteInfo.class, RouteChoice.class}, version = 2, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    public abstract FavoriteDao favoriteDao();
    public abstract RouteChoiceDao routeChoiceDao();
}
