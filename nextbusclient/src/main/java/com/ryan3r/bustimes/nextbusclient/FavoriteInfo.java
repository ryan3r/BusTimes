package com.ryan3r.bustimes.nextbusclient;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity()
public class FavoriteInfo {
    @PrimaryKey(autoGenerate = true)
    private int _id;

    @NonNull
    private String stopId;
    private int accessCount;

    @Ignore
    private String title;

    FavoriteInfo() {
        stopId = "";
    }

    public FavoriteInfo(@NonNull String id) {
        stopId = id;
        accessCount = 1;
    }

    // Getters/setters
    public @NonNull String getStopId() {
        return stopId;
    }

    int getAccessCount() {
        return accessCount;
    }

    public void setStopId(@NonNull String stopId) {
        this.stopId = stopId;
    }

    void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    int get_id() {
        return _id;
    }

    void set_id(int _id) {
        this._id = _id;
    }
}
