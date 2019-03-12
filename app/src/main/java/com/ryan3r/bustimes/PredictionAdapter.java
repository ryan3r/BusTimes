package com.ryan3r.bustimes;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ryan3r.bustimes.nextbusclient.FavoriteInfo;
import com.ryan3r.bustimes.nextbusclient.NextBusInfo;
import com.ryan3r.bustimes.nextbusclient.NextBusPredictions;
import com.ryan3r.bustimes.nextbusclient.StopInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PredictionAdapter extends BaseAdapter implements NextBusPredictions.Handler {
    private Context mContext;
    private ArrayList<TimePair> mPrediction;
    private NextBusPredictions.Handler mHandler;
    private Map<String, StopInfo.RouteInfo> routes;
    private NextBusInfo mInfo;
    private HashSet<String> fetched;

    private class TimePair {
        private String title;
        private String routeId;

        TimePair(String _title, String predId) {
            title = _title;
            routeId = predId.split("\\|")[0];
            String stopId = predId.split("\\|")[1];

            if(!fetched.contains(stopId)) {
                fetched.add(stopId);
                mInfo.getStopInfo(stopId, new NextBusInfo.ResponseHandler<StopInfo>() {
                    @Override
                    public void onResponse(StopInfo stop) {
                        for(StopInfo.RouteInfo route : stop.getRoutes()) {
                            routes.put(route.getId(), route);
                        }
                    }
                });
            }
        }

        StopInfo.RouteInfo getRoute() {
            return routes.get(routeId);
        }
    }

    public PredictionAdapter(Context context, NextBusPredictions predictor, NextBusPredictions.Handler handle, NextBusInfo info) {
        mContext = context;
        mInfo = info;
        mPrediction = new ArrayList<>();
        predictor.setHandler(this);
        mHandler = handle;
        routes = new HashMap<>();
        fetched = new HashSet<>();
    }

    public void onPrediction(ArrayList<NextBusPredictions.Prediction> predictions) {
        mPrediction.clear();

        for(NextBusPredictions.Prediction prediction : predictions) {
            int limit = 3;

            for(NextBusPredictions.Time time : prediction.getTimes()) {
                if(limit-- == 0) break;

                mPrediction.add(new TimePair(time.getTimeUntil() + " (" + time.getArrivalTime() + ")", prediction.getId()));
            }

            if(prediction.getTimes().isEmpty()) {
                mPrediction.add(new TimePair("No predictions", prediction.getId()));
            }
        }

        mHandler.onPrediction(predictions);
        notifyDataSetChanged();
    }

    public void requestError(Throwable err) {
        mHandler.requestError(err);
    }

    public void onLoadStart() {
        mHandler.onLoadStart();
    }

    /**
     * Use indexes as id
     * @param index The index in the array
     * @return The id for the item
     */
    public long getItemId(int index) {
        return index;
    }

    /**
     * Get the number of items to display
     * @return The number of stops to display
     */
    public int getCount() {
        return mPrediction.size();
    }

    /**
     * Render an item
     * @param position The index to get
     * @return The value in our underlying array
     */
    public Object getItem(int position) {
        return mPrediction.get(position);
    }

    /**
     * Render an item
     * @param position The position of the item
     * @param currentView The view for this item
     * @param container The group for the views
     * @return The newly created view
     */
    public View getView(int position, View currentView, ViewGroup container) {
        if (currentView == null) {
            LayoutInflater inf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // if we have a null inflater bail
            if(inf == null) return null;

            currentView = inf.inflate(R.layout.fragment_favorite_item, container, false);
        }

        TimePair time = mPrediction.get(position);
        StopInfo.RouteInfo info = time.getRoute();

        // hide the loader
        ProgressBar loader = currentView.findViewById(R.id.loader);
        loader.setVisibility(View.GONE);

        // set the loader color
        if(info != null) {
            int colorInt = Color.parseColor(info.getColor());
            loader.getIndeterminateDrawable().setColorFilter(colorInt, PorterDuff.Mode.SRC_IN);
        }

        // update the route title
        TextView titleView = currentView.findViewById(R.id.title);
        titleView.setText(time.title);

        // update the button text
        TextView textView = currentView.findViewById(R.id.text);
        textView.setText(info != null ? info.getShortTitle() : "--");

        // update the color
        if(info != null) {
            FloatingActionButton fab = currentView.findViewById(R.id.button);
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(info.getColor())));
        }

        return currentView;
    }
}
