package com.ryan3r.bustimes.nextbusclient;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity()
public class FavoriteInfo {
    @PrimaryKey(autoGenerate = true)
    private int _id;

    @NonNull
    private String stopId;
    private int accessCount;
    private String routeId;

    @Ignore
    private String title;

    @Ignore
    private String color;

    @Ignore
    private String shortTitle;

    FavoriteInfo() {
        stopId = "";
    }

    public FavoriteInfo(@NonNull String id, String rid) {
        stopId = id;
        routeId = rid;
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

    public String getRouteId() {
        return routeId;
    }

    void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    int get_id() {
        return _id;
    }

    void set_id(int _id) {
        this._id = _id;
    }
}
