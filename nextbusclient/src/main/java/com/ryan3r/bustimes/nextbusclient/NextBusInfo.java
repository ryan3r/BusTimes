package com.ryan3r.bustimes.nextbusclient;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class NextBusInfo extends NextBus {
    private static StopDatabase db = null;
    private static UserDatabase userDb = null;

    public NextBusInfo(Context ctx) {
        super(ctx);

        if(db == null) {
            db = Room.databaseBuilder(ctx, StopDatabase.class, "stop-database")
                    .fallbackToDestructiveMigration()
                    .build();

            userDb = Room.databaseBuilder(ctx, UserDatabase.class, "user-database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
    }

    private ErrorHandler errorHandler;

    // a basic error handler
    public interface ErrorHandler {
        void requestError(Throwable err);
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    static UserDatabase getUserDb() { return userDb; }

    // pass errors on to the error handler
    @Override
    protected void requestError(Throwable error) {
        errorHandler.requestError(error);
    }

    // the generic handler for responses from the next bus api
    public interface ResponseHandler<T> {
        void onResponse(T response);
    }

    private static HashMap<String, StopInfo> stopCache = new HashMap<>();

    // get info for a stop
    public void getStopInfo(final String id, final ResponseHandler<StopInfo> handler) {
        downloadData(id, new Runnable() {
            @Override
            public void run() {
                handler.onResponse(stopCache.get(id));
            }
        });
    }

    // download and save the next bus meta data
    private void downloadData(String id, final Runnable onDone) {
        // we already loaded the data
        if(stopCache.containsKey(id)) {
            onDone.run();
            return;
        }

        // try to load the data from the cache
        new LoadStop(db, userDb, id, new StopHandler() {
            @Override
            public void onSuccess(StopInfo stop) {
                // invalid data, re-download
                if(stop == null) {
                    onError();
                    return;
                }

                // cache the stop info
                stopCache.put(stop.getId(), stop);

                onDone.run();
            }

            // if we don't have anything in the cache download the data
            @Override
            public void onError() {
                // load the info for the route
                _nextBus("routeConfig", "&terse", new JsonHandler() {
                    @Override
                    public void onSuccess(JsonElement json) {
                        JsonArray routes = json.getAsJsonObject().getAsJsonArray("route");
                        // convert the data to our preferred format
                        HashSet<StopInfo> toStore = new HashSet<>();

                        // process each route
                        for (JsonElement routeEl : routes) {
                            JsonObject route = routeEl.getAsJsonObject();
                            JsonArray stops = route.getAsJsonArray("stop");

                            // find the stop that was requested
                            for (JsonElement current : stops) {
                                // load or create the stop
                                StopInfo info = stopCache.get(current.getAsJsonObject().get("tag").getAsString());

                                if(info == null) {
                                    info = StopInfo.fromNextBus(current.getAsJsonObject());
                                }

                                // add the route to this stop
                                info.addRoute(route);

                                // cache the stop info
                                stopCache.put(info.getId(), info);

                                // get the raw version of the stop info
                                toStore.add(info);
                            }
                        }

                        onDone.run();

                        // cache the data for future use
                        new SaveStops(toStore).run(db, null);
                    }
                });
            }
        });
    }

    protected interface StopHandler {
        void onError();
        void onSuccess(StopInfo stops);
    }

    public void getAllStops(final ResponseHandler<List<StopInfo>> handler) {
        // try to load the stops from the database
        new LoadAllStops().run(db, new ResponseHandler<List<StopInfo>>() {
            @Override
            public void onResponse(List<StopInfo> response) {
                // we have the data
                if(response.size() > 0) {
                    handler.onResponse(response);
                    return;
                }

                // download the data
                downloadData("1462", new Runnable() {
                    @Override
                    public void run() {
                        handler.onResponse(new ArrayList<>(stopCache.values()));
                    }
                });
            }
        });
    }

    // get a favorite
    public void getFavorite(String stopId, NextBusInfo.ResponseHandler<FavoriteInfo> handler) {
        new LoadFavorite(stopId).run(userDb, handler);
    }

    // insert, update, or delete a favorite
    public void saveFavorite(FavoriteInfo favorite, int isNew) {
        new SaveFavorite(favorite, isNew).run(userDb, null);
    }

    // get all the favorites
    public void getAllFavorites(NextBusInfo.ResponseHandler<FavoriteInfo[]> handler) {
        new AllFavorites(userDb).run(db, handler);
    }

    // get the schedule for a route
    public void getSchedule(final String day, final String stopId, final String routeId,
                     boolean reload, final NextBusInfo.ResponseHandler<BusSchedule> handler) {
        // go straight to the network
        if(reload) {
            _getSchedule(day, stopId, routeId, handler);
        }
        else {
            // try to load the cached schedule
            new LoadSchedule(routeId + "|" + stopId + "|" + day).run(db, new ResponseHandler<BusSchedule>() {
                @Override
                public void onResponse(BusSchedule response) {
                    // use the cached version
                    if (response != null) {
                        handler.onResponse(response);
                    }
                    // load the schedule
                    else {
                        _getSchedule(day, stopId, routeId, handler);
                    }
                }
            });
        }
    }

    private void _getSchedule(final String day, final String stopId, final String routeId,
                              final NextBusInfo.ResponseHandler<BusSchedule> handler) {
        _nextBus("schedule", "&r=" + routeId, new JsonHandler() {
            @Override
            public void onSuccess(JsonElement json) {
                List<BusSchedule> schedules = BusSchedule.fromJson(json);

                BusSchedule schedule = null;

                // find the schedule we are looking for
                for (BusSchedule maybe : schedules) {
                    if (maybe.getId().equals(routeId + "|" + stopId + "|" + day)) {
                        schedule = maybe;
                        break;
                    }
                }

                handler.onResponse(schedule);

                // save the schedules
                new SaveSchedules(schedules).run(db, null);
            }
        });
    }
}

// the base for all async tasks
abstract class BaseTask<Db, Result> extends AsyncTask<Object, Object, Result> {
    protected Db db;
    private NextBusInfo.ResponseHandler<Result> handler;

    void run(Db d, NextBusInfo.ResponseHandler<Result> h) {
        db = d;
        handler = h;
        execute();
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);

        if(handler != null) {
            handler.onResponse(result);
        }
    }

    @Override
    final protected Result doInBackground(Object... objects) {
        return doWork();
    }

    abstract Result doWork();
}

// get all the favorites
class AllFavorites extends BaseTask<StopDatabase, FavoriteInfo[]> {
    private UserDatabase userDb;

    AllFavorites(UserDatabase d) {
        userDb = d;
    }

    @Override
    protected FavoriteInfo[] doWork() {
        FavoriteInfo[] favorites = userDb.favoriteDao().getAll();
        StopDao stops = db.stopDao();

        for(FavoriteInfo favorite : favorites) {
            StopInfo stop = stops.getStop(favorite.getStopId())[0];
            // set the title and color for the favorite
            favorite.setTitle(stop.getTitle());
        }

        return favorites;
    }
}

// insert, update, or delete a favorite
class SaveFavorite extends BaseTask<UserDatabase, Object> {
    private FavoriteInfo favorite;
    private int isNew;

    SaveFavorite(FavoriteInfo f, int n) {
        favorite = f;
        isNew = n;
    }

    @Override
    protected Object doWork() {
        FavoriteDao dao = db.favoriteDao();

        switch(isNew) {
            // delete this entry
            case 2:
                dao.remove(favorite);
                break;

            // insert an entry
            case 1:
                dao.insert(favorite);

                // update an entry
            case 0:
                dao.update(favorite);
        }

        return null;
    }
}

// load a favorite
class LoadFavorite extends BaseTask<UserDatabase, FavoriteInfo> {
    private String id;

    LoadFavorite(String i) {
        id = i;
    }

    @Override
    protected FavoriteInfo doWork() {
        FavoriteInfo[] info = db.favoriteDao().get(id);

        if(info.length == 0) return null;

        return info[0];
    }
}

// load all the stops from the data store
class LoadAllStops extends BaseTask<StopDatabase, List<StopInfo>> {
    @Override
    protected List<StopInfo> doWork() {
        return db.stopDao().getAll();
    }
}

// put all of the stops in the database
class SaveStops extends BaseTask<StopDatabase, Void> {
    private HashSet<StopInfo> stops;

    SaveStops(HashSet<StopInfo> s) {
        stops = s;
    }

    @Override
    protected Void doWork() {
        StopDao stopDao = db.stopDao();

        for (StopInfo stop : stops) {
            stopDao.addStop(stop);
        }

        return null;
    }
}

// pull all the stops out of the database
class LoadStop extends AsyncTask<String, Object, StopInfo[]> {
    private NextBusInfo.StopHandler handler;
    private StopDatabase db;
    private UserDatabase userDb;

    LoadStop(StopDatabase d, UserDatabase u, String stopId, NextBusInfo.StopHandler h) {
        super();

        handler = h;
        db = d;
        userDb = u;

        execute(stopId);
    }

    @Override
    protected StopInfo[] doInBackground(String... params) {
        StopInfo[] stops = db.stopDao().getStop(params[0]);

        if(stops.length > 0) {
            ArrayList<StopInfo.RouteInfo> routes = stops[0].getRoutes();
            List<RouteChoice> choices = userDb.routeChoiceDao().get(params[0]);
            ArrayList<RouteChoice> fullChoices = new ArrayList<>();
            ArrayList<StopInfo.RouteInfo> newRoutes = new ArrayList<>();

            outer: for(StopInfo.RouteInfo info : routes) {
                for(RouteChoice choice : choices) {
                    if(info.getId().equals(choice.getRouteId())) {
                        choice.setRoute(info);
                        fullChoices.add(choice);
                        continue outer;
                    }
                }

                newRoutes.add(info);
                fullChoices.add(new RouteChoice(true, params[0], info));
            }

            stops[0].setRoutes(newRoutes);
            stops[0].setRouteChoices(fullChoices);
        }

        return stops;
    }

    @Override
    protected void onPostExecute(StopInfo[] stops) {
        // pass the result on
        if(stops.length == 0) {
            handler.onError();
        }
        else {
            handler.onSuccess(stops[0]);
        }
    }
}

// get a schedule
class LoadSchedule extends BaseTask<StopDatabase, BusSchedule> {
    private String id;

    LoadSchedule(String i) {
        id = i;
    }

    @Override
    BusSchedule doWork() {
        return db.scheduleDao().get(id);
    }
}

// save the schedules
class SaveSchedules extends BaseTask<StopDatabase, Object> {
    private List<BusSchedule> schedules;

    SaveSchedules(List<BusSchedule> s) {
        schedules = s;
    }

    @Override
    Object doWork() {
        ScheduleDao scheduleDao = db.scheduleDao();

        for(BusSchedule schedule : schedules) {
            if(scheduleDao.get(schedule.getId()) == null) {
                scheduleDao.add(schedule);
            }
            else {
                scheduleDao.set(schedule);
            }
        }

        return null;
    }
}