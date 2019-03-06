package com.ryan3r.bustimes.nextbusclient;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * The interface between the ui and next bus
 */
public class NextBusPredictions extends NextBus {
    // the stops we want predictions for
    private List<String> stops;
    // the handler used by the refresh timer
    private android.os.Handler timer = null;
    // the currently running timer callback
    private Runnable timerCallback = null;
    // the last time we requested predictions
    private static long lastRefresh = 0;
    // the predictions for these stops
    private ArrayList<NextBusPredictions.Prediction> stopTimes;
    // predictions started before routes were given
    private boolean requestsStarted = false;

    // the time to wait after a failed attempt
    private static final int FAIL_DELAY = 15000;
    // the time between refreshes
    private static final int REFRESH_INTERVAL = 60000;
    // the time between ticks
    private static final int TICK_INTERVAL = 1000;
    // the maximum number of times we can fail to refresh
    private static final int MAX_RETRIES = 3;

    // cache previous predictions
    private static HashMap<String, Prediction> predictionCache;

    static {
        predictionCache = new HashMap<>();
    }

    // event listeners
    private NextBusPredictions.Handler nbHandler;

    public NextBusPredictions(Context ctx) {
        super(ctx);
        timer = new android.os.Handler(ctx.getMainLooper());
    }

    /**
     * Set the handler for predictions
     * @param nbHandler The handler
     */
    public void setHandler(NextBusPredictions.Handler nbHandler) {
        this.nbHandler = nbHandler;
    }

    List<String> getRoutes() {
        return stops;
    }

    /**
     * Set the predictions to get
     * @param stopList The stops to track
     */
    public void setRoutes(List<String> stopList) {
        stops = stopList;

        // only refetch the predictions if we don't have a callback timer running or we have
        // new stops to predict
        if(requestsStarted) {
            clearTimer();
            predict(0, true, true);
        }
    }

    /**
     * Set the predicitons to get
     * @param stop The stop in the form route|stop
     */
    public void setRoutes(String stop) {
        stops = new ArrayList<>();
        stops.add(stop);

        if(requestsStarted) {
            clearTimer();
            predict(0, true, true);
        }
    }

    /**
     * Start the predictions
     */
    public void startPredictions() {
        requestsStarted = true;

        if(stops != null && !stops.isEmpty()) {
            predict(0, true, true);
        }
    }

    /**
     * Pass errors to the handler
     * @param error The handler for the request
     */
    @Override
    protected void requestError(Throwable error) {
        nbHandler.requestError(error);
    }

    /**
     * set up the refresh timer
     *
     * @param delay    The amount of time to wait for the next request
     * @param retries  The number of retries we have made so far (should usually be 0)
     * @param repeated Whether this timer should repeat
     */
    private void tick(final long delay, final int retries, final boolean repeated) {
        // we already have a callback running
        if (timerCallback != null) return;

        // create the callback
        timerCallback = new Runnable() {
            @Override
            public void run() {
                // clear the old callback
                timerCallback = null;

                if(System.currentTimeMillis() - lastRefresh >= delay) {
                    predict(retries, repeated, true);
                }
                else {
                    nbHandler.onPrediction(stopTimes);

                    if(!areTimesEmpty()) {
                        tick(delay, retries, repeated);
                    }
                }
            }
        };

        timer.postDelayed(timerCallback, TICK_INTERVAL);
    }

    /**
     * Check if the current predictions are all empty
     * @return Whether all predictions are empty
     */
    private boolean areTimesEmpty() {
        // check if the actual times are empty
        if(stopTimes == null || stopTimes.isEmpty()) return true;

        // check if all the stop predictions are empty
        for(Prediction prediction : stopTimes) {
            // at least one prediction is not empty don't run the refresh timer
            if(!prediction.getTimes().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Stop and clear any currently running retry/refresh timer
     */
    private void clearTimer() {
        if (timerCallback != null) {
            // remove the callback for the timer if it is running
            timer.removeCallbacks(timerCallback);

            timerCallback = null;
        }
    }

    /**
     * Cancel the prediction timers
     */
    public void stopPredictions() {
        requestsStarted = false;
        clearTimer();
    }

    /**
     * Refresh the data
     */
    public void refresh() {
        predict(-1, false, false);
    }

    /**
     * Get predictions for routes
     */
    private void predict(final int retries, final boolean repeated, boolean useCache) {
        if(retries > MAX_RETRIES) return;

        // tell the ui we are starting a request
        nbHandler.onLoadStart();

        boolean allCached = true;
        StringBuilder queryBuilder = new StringBuilder();

        HashSet<String> toRemove = new HashSet<>();

        long now = System.currentTimeMillis();
        // build the request query
        for(String stop : stops) {
            // check if the stop is already in the cache
            if(!predictionCache.containsKey(stop) ||
                    (now - predictionCache.get(stop).getFetchedAt()) >= REFRESH_INTERVAL) {
                allCached = false;

                toRemove.add(stop);
            }

            queryBuilder.append("&stops=").append(stop);
        }

        // remove old keys
        for(String key : toRemove) predictionCache.remove(key);

        // all the predictions are already in the cache
        if(allCached && useCache) {
            stopTimes = new ArrayList<>();

            for(String stop : stops) {
                stopTimes.add(predictionCache.get(stop));
            }

            // send the result to the views
            nbHandler.onPrediction(stopTimes);

            // set up the next refresh
            if(repeated) {
                tick(REFRESH_INTERVAL, 0, true);
            }

            return;
        }

        String query = queryBuilder.toString();

        _nextBus("predictionsForMultiStops", query, new NextBus.JsonHandler() {
            public void onSuccess(JsonElement json) {
                stopTimes = new ArrayList<>();

                JsonElement preds = json.getAsJsonObject().get("predictions");

                // handle the single element and multi element case
                if(preds.isJsonArray()) {
                    JsonArray predArr = preds.getAsJsonArray();

                    for(int i = 0; i < predArr.size(); ++i) {
                        _handlePred(predArr.get(i).getAsJsonObject());
                    }
                }
                else {
                    _handlePred(preds.getAsJsonObject());
                }

                // cache the predictions
                for(NextBusPredictions.Prediction pred : stopTimes) {
                    predictionCache.put(pred.getId(), pred);
                }

                // send the result to the views
                nbHandler.onPrediction(stopTimes);

                lastRefresh = System.currentTimeMillis();

                // set up the next refresh
                if(repeated) {
                    tick(REFRESH_INTERVAL, 0, true);
                }
            }

            private void _handlePred(JsonObject pred) {
                if(pred == null || pred.get("direction") == null) return;

                JsonObject dir;

                // TODO: Proper separation of directions
                if (pred.get("direction").isJsonArray()) {
                    dir = pred.getAsJsonArray("direction").get(0).getAsJsonObject();
                }
                else {
                    dir = pred.getAsJsonObject("direction");
                }

                // no direction
                if (dir == null || pred.get("direction").isJsonNull()) return;

                JsonElement predEl = dir.get("prediction");

                // no prediction
                if(dir.get("prediction").isJsonNull()) return;

                Prediction prediction = new NextBusPredictions.Prediction(
                        pred.get("routeTag").getAsInt(),
                        pred.get("stopTag").getAsInt(),
                        System.currentTimeMillis()
                );

                stopTimes.add(prediction);

                // more array funkiness
                if(predEl.isJsonArray()) {
                    JsonArray predArr = predEl.getAsJsonArray();

                    for(int i = 0; i < predArr.size(); ++i) {
                        _handleTime(predArr.get(i).getAsJsonObject(), prediction);
                    }
                }
                else {
                    _handleTime(predEl.getAsJsonObject(), prediction);
                }
            }

            private void _handleTime(JsonObject time, NextBusPredictions.Prediction prediction) {
                prediction.times.add(new Time(time.get("epochTime").getAsLong()));
            }

            // retry sooner after errors
            public void onError(Throwable err) {
                lastRefresh = System.currentTimeMillis();

                if(retries == -1) return;

                tick(FAIL_DELAY, retries + 1, repeated);
            }
        });
    }

    /**
     * A prediction returned from next bus
     */
    public static class Prediction {
        private int routeId;
        private int stopId;
        private ArrayList<Time> times;
        private long fetchedAt;

        private Prediction(int rId, int sId, long fAt) {
            routeId = rId;
            stopId = sId;
            times = new ArrayList<>();
            fetchedAt = fAt;
        }

        public ArrayList<Time> getTimes() {
            return times;
        }

        long getFetchedAt() {
            return fetchedAt;
        }

        public String getId() {
            return routeId + "|" + stopId;
        }

        public String fetchedAtStr() {
            return makeTimeStr(fetchedAt, System.currentTimeMillis());
        }
    }

    /**
     * Generate a time string from a unix time stamp
     * @param time A unix time stamp
     * @return The string representation of the time
     */
    private static String makeTimeStr(long now, long time) {
        long diff = (time - now) / 1000;
        int seconds = (int) (diff % 60);

        return (diff / 60) + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    public static class Time {
        private long time;

        Time(long t) {
            time = t;
        }

        /**
         * Get the time until the bus arrives (from now)
         * @return The time string in minutes:seconds
         */
        public String getTimeUntil() {
            return makeTimeStr(System.currentTimeMillis(), time);
        }

        /**
         * Get the time when the bus arrives
         * @return The user friendly (12 hour) time
         */
        public String getArrivalTime() {
            Calendar arrival = GregorianCalendar.getInstance();
            arrival.setTime(new Date(time));

            int hours = arrival.get(Calendar.HOUR);
            int minutes = arrival.get(Calendar.MINUTE);

            if(hours == 0) hours = 12;

            return + hours + ":" + (minutes < 10 ? "0" + minutes : minutes);
        }

        /**
         * Get the raw time
         * @return The time
         */
        public long getTime() {
            return time;
        }
    }

    public interface Handler {
        void requestError(Throwable err);
        void onLoadStart();
        void onPrediction(ArrayList<NextBusPredictions.Prediction> preds);
    }
}
