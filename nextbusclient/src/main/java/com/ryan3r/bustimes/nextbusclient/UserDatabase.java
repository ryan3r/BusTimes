package com.ryan3r.bustimes.nextbusclient;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {FavoriteInfo.class, RouteChoice.class}, version = 2, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    public abstract FavoriteDao favoriteDao();
    public abstract RouteChoiceDao routeChoiceDao();
}
