package com.ryan3r.bustimes.nextbusclient;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity()
public class RouteChoice {
    @Ignore private boolean selected;
    @Ignore private StopInfo.RouteInfo route;

    @PrimaryKey(autoGenerate = true) private int _id;

    @NonNull private String stopId = "";
    @NonNull private String routeId = "";

    RouteChoice() {
        selected = false;
    }

    RouteChoice(boolean sel, String sId, StopInfo.RouteInfo r) {
        stopId = sId;
        selected = sel;
        route = r;
        routeId = r.getId();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public StopInfo.RouteInfo getRoute() {
        return route;
    }

    public int get_id() {
        return _id;
    }

    @NonNull
    public String getStopId() {
        return stopId;
    }

    @NonNull
    public String getRouteId() {
        return routeId;
    }

    public void setRoute(StopInfo.RouteInfo route) {
        this.route = route;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setStopId(@NonNull String stopId) {
        this.stopId = stopId;
    }

    public void setRouteId(@NonNull String routeId) {
        this.routeId = routeId;
    }
}
