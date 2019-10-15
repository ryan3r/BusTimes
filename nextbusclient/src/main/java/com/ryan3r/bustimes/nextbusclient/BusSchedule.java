package com.ryan3r.bustimes.nextbusclient;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

@Entity
public class BusSchedule {
    @PrimaryKey
    private @NonNull String id;
    private String[] times;
    private long fetchedAt;

    BusSchedule() {
        id = "";
    }

    private BusSchedule(@NonNull String i, String[] t) {
        id = i;
        times = t;
        fetchedAt = System.currentTimeMillis();
    }

    // convert the nextbus output into pojos
    static ArrayList<BusSchedule> fromJson(JsonElement json) {
        ArrayList<BusSchedule> schedules = new ArrayList<>();

        JsonArray schedule = json.getAsJsonObject().getAsJsonArray("route");

        for(JsonElement dayEl : schedule) {
            JsonObject day = dayEl.getAsJsonObject();

            String dayName = day.get("serviceClass").getAsString();
            String routeId = day.get("tag").getAsString();

            // get the names for the stops
            JsonArray stops = day.getAsJsonObject("header").getAsJsonArray("stop");
            // the times for each stop
            JsonArray stopTimes = day.getAsJsonArray("tr");

            for(int i = 0; i < stops.size(); ++i) {
                String stopId = null;

                // convert the time longs to pojos
                String[] times = new String[stopTimes.size()];
                int j = 0;

                for(JsonElement timeGroup : stopTimes) {
                    JsonObject info = timeGroup.getAsJsonObject()
                            .getAsJsonArray("stop")
                            .get(i).getAsJsonObject();

                    // get the stop id
                    if(stopId == null) {
                        stopId = info.get("tag").getAsString();
                    }

                    // get the arrival time
                    String[] timeBits = info.get("content").getAsString().split(":");

                    // skip empty times
                    if(timeBits[0].equals("--")) continue;

                    int hours = Integer.parseInt(timeBits[0]);
                    boolean isPm = hours > 12;
                    if(isPm) hours -= 12;

                    times[j++] = hours + ":" + timeBits[1] + (isPm ? " pm" : " am");
                }

                schedules.add(new BusSchedule(routeId + "|" + stopId + "|" + dayName, times));
            }
        }

        return schedules;
    }

    public String getStopId() {
        return id.split("\\|")[1];
    }

    public String getRouteId() {
        return id.split("\\|")[0];
    }

    public @NonNull String getId() {
        return id;
    }

    public String[] getTimes() {
        return times;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setTimes(String[] times) {
        this.times = times;
    }

    public long getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(long fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}
