package com.ryan3r.bustimes.nextbusclient;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

/**
 * The interface between the ui and next bus
 */
abstract class NextBus {
    // the base url for all cyride requests
    private static final String BASE_URL = "http://webservices.nextbus.com/service/publicJSONFeed?a=cyride";
    // the queue for network requests
    private RequestQueue _queue;
    // the context we are using
    private Context context;

    NextBus(Context ctx) {
        _queue = Volley.newRequestQueue(ctx);
        context = ctx;
    }

    public Context getContext() {
        return context;
    }

    /**
     * Send and parse a request to next bus (internally)
     *
     * @param command The next bus command to run
     * @param query   The query string for next bus
     * @param handler The handler to be called when a result is reached
     */
    void _nextBus(final String command, String query, final JsonHandler handler) {
        String url = BASE_URL + "&command=" + command + query;

        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                JsonElement result = new JsonParser().parse(res);

                // an error occurred
                if(result.isJsonObject() && result.getAsJsonObject().has("Error")) {
                    JsonObject error = result.getAsJsonObject();

                    // take the first error
                    if(error.get("Error").isJsonArray()) {
                        error = error.getAsJsonArray("Error").get(0).getAsJsonObject();
                    }
                    else {
                        error = error.getAsJsonObject("Error");
                    }

                    Error err = new Error(error.get("content").getAsString());

                    // send the error
                    requestError(err);
                    handler.onError(err);

                    return;
                }

                handler.onSuccess(result);
            }
        }, new Response.ErrorListener() {
            // pass on the errors
            @Override
            public void onErrorResponse(VolleyError err) {
                requestError(err);
                handler.onError(err);
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept-Encoding", "gzip, deflate");
                headers.put("User-Agent", "bus times");
                return headers;
            }
        };

        _queue.add(req);
    }

    protected abstract void requestError(Throwable error);

    /**
     * The internal handler object used by _nextbus
     */
    protected static abstract class JsonHandler {
        public abstract void onSuccess(JsonElement json);

        public void onError(Throwable err) {}
    }
}
