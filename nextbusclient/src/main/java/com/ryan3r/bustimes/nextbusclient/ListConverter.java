package com.ryan3r.bustimes.nextbusclient;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class ListConverter {
    // convert a list to a json string
    @TypeConverter
    public static String toString(ArrayList<StopInfo.RouteInfo> routes) {
        JsonArray array = new JsonArray();

        for(StopInfo.RouteInfo info : routes) {
            array.add(info.toJson());
        }

        return array.toString();
    }

    // convert a json string to a list
    @TypeConverter
    public static ArrayList<StopInfo.RouteInfo> toRoute(String raw) {
        ArrayList<StopInfo.RouteInfo> routes = new ArrayList<>();
        JsonArray array = new JsonParser().parse(raw).getAsJsonArray();

        for(JsonElement route : array) {
            routes.add(StopInfo.RouteInfo.parse(route.getAsJsonObject()));
        }

        return routes;
    }
}
