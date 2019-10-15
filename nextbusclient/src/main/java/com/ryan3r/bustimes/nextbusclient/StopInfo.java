package com.ryan3r.bustimes.nextbusclient;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// the info for a stop
@Entity
public class StopInfo {
    @PrimaryKey
    @NonNull
    private String stopId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "routes")
    private ArrayList<RouteInfo> routes;

    @Embedded
    private LatLon coordinates;

    @Ignore
    private ArrayList<RouteChoice> routeChoices;

    StopInfo() {
        stopId = "null";
        routes = new ArrayList<>();
        routeChoices = new ArrayList<>();
    }

    // parse a stop from the next bus api
    static StopInfo fromNextBus(JsonObject jStop) {
        StopInfo stop = new StopInfo();

        stop.title = jStop.get("Name").getAsString();
        stop.stopId = jStop.get("ID").getAsString();
        stop.coordinates = new LatLon(jStop.get("Latitude").getAsDouble(), jStop.get("Longitude").getAsDouble());

        return stop;
    }

    // add a route from next bus
    void addRoute(JsonObject route) {
        routes.add(RouteInfo.parse(route));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<RouteInfo> getRoutes() {
        return routes;
    }

    public void setRoutes(ArrayList<RouteInfo> routes) {
        this.routes = routes;
    }

    public String getId() {
        return stopId;
    }

    public LatLon getCoordinates() {
        return coordinates;
    }

    void setCoordinates(LatLon coordinates) {
        this.coordinates = coordinates;
    }

    @NonNull
    public String getStopId() {
        return stopId;
    }

    public void setStopId(@NonNull String stopId) {
        this.stopId = stopId;
    }

    @Override
    public int hashCode() {
        return stopId.hashCode();
    }

    public ArrayList<RouteChoice> getRouteChoices() {
        return routeChoices;
    }

    public void setRouteChoices(ArrayList<RouteChoice> routeChoices) {
        this.routeChoices = routeChoices;
    }

    // info for a route
    public static class RouteInfo {
        private String id;
        private String title;
        private String color;
        private String textColor;
        private String shortTitle;
        private String customerID;

        RouteInfo(String i, String t, String c, String o, String ci, String st) {
            id = i;
            title = t;

            if(c.charAt(0) != '#') c = "#" + c;
            if(o.charAt(0) != '#') o = "#" + o;

            color = c;
            textColor = o;
            customerID = ci;
            shortTitle = st;
        }

        // parse a json object
        static RouteInfo parse(JsonObject route) {
            String id = route.get("ID").getAsString();

            // TODO: Support more strings
            if(route.has("Patterns")) {
                id = route
                        .get("Patterns").getAsJsonArray()
                        .get(0).getAsJsonObject()
                        .get("ID").getAsString();
            }

            return new RouteInfo(
                    id,
                    route.get("Name").getAsString(),
                    route.get("Color").getAsString(),
                    route.get("TextColor").getAsString(),
                    route.get("CustomerID").getAsString(),
                    route.get("ShortName").getAsString()
            );
        }

        public String getTitle() {
            return title;
        }

        public String getId() {
            return id;
        }

        public String getColor() {
            return color;
        }

        public String getCustomerID() {
            return customerID;
        }

        // get a short version of the title
        public String getShortTitle() {
            return shortTitle;
        }

        // convert a route to json
        JsonObject toJson() {
            JsonObject route = new JsonObject();

            route.addProperty("ID", id);
            route.addProperty("Name", title);
            route.addProperty("Color", color);
            route.addProperty("TextColor", textColor);
            route.addProperty("CustomerID", textColor);
            route.addProperty("ShortName", shortTitle);

            return route;
        }
    }

    // Save the route choices
    public void saveRouteChoices() {
        new SaveRouteChoices().run(stopId, getRouteChoices());
    }

    // a latitude and longitude
    public static class LatLon {
        // coordinates
        private double lon;
        private double lat;

        LatLon() {}

        public LatLon(double la, double lo) {
            lat = la;
            lon = lo;
        }

        // get the distance from a specific point
        // copied from https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
        double distFrom(double lat, double lon) {
            final int R = 6371; // Radius of the earth

            double latDistance = Math.toRadians(this.lat - lat);
            double lonDistance = Math.toRadians(this.lon - lon);
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(this.lat))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return R * c * 0.621371; // convert to miles
        }

        public double distFrom(LatLon loc) {
            return distFrom(loc.lat, loc.lon);
        }

        double getLon() {
            return lon;
        }

        double getLat() {
            return lat;
        }

        void setLon(double lon) {
            this.lon = lon;
        }

        void setLat(double lat) {
            this.lat = lat;
        }
    }
}

class SaveRouteChoices extends AsyncTask<Object, Object, Object> {
    private String stopId;
    private ArrayList<RouteChoice> routeChoices;

    void run(String id, ArrayList<RouteChoice> choices) {
        stopId = id;
        routeChoices = choices;
        execute();
    }

    @Override
    final protected Object doInBackground(Object... objects) {
        RouteChoiceDao db = NextBusInfo.getUserDb().routeChoiceDao();

        db.removeByStop(stopId);

        for(RouteChoice choice : routeChoices) {
            if (!choice.isSelected()) {
                db.insert(choice);
            }
        }

        return null;
    }
}