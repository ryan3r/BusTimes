package com.ryan3r.bustimes.nextbusclient;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * The interface between the ui and next bus
 */
abstract class NextBus {
    // the base url for all cyride requests
    private static final String BASE_URL = "https://www.mycyride.com";
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
     * @param path The path for this api request (must start with /)
     * @param handler The handler to be called when a result is reached
     */
    void _apiCall(final String path, final JsonHandler handler) {
        String url = BASE_URL + path;

        Request req = new Request<String>(Request.Method.GET, url, new Response.ErrorListener() {
            // pass on the errors
            @Override
            public void onErrorResponse(VolleyError err) {
                requestError(err);
                handler.onError(err);
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "bus times");
                return headers;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String res = new String(response.data, "UTF-8");
                    return Response.success(res, null);
                } catch(UnsupportedEncodingException e) {
                    return Response.error(new VolleyError("Bad encoding"));
                }
            }

            @Override
            protected void deliverResponse(String res) {
                JsonArray result = new JsonParser().parse(res).getAsJsonArray();

                handler.onSuccess(result);
            }
        };

        _queue.add(req);
    }

    protected abstract void requestError(Throwable error);

    /**
     * The internal handler object used by _nextbus
     */
    protected static abstract class JsonHandler {
        public abstract void onSuccess(JsonArray json);

        public void onError(Throwable err) {}
    }
}
