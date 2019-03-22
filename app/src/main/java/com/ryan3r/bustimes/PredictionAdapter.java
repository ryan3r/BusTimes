package com.ryan3r.bustimes;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ryan3r.bustimes.nextbusclient.NextBusInfo;
import com.ryan3r.bustimes.nextbusclient.NextBusPredictions;
import com.ryan3r.bustimes.nextbusclient.StopInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PredictionAdapter extends BaseAdapter implements NextBusPredictions.Handler {
    private Context mContext;
    private ArrayList<NextBusPredictions.Time> mPrediction;
    private NextBusPredictions.Handler mHandler;
    private NextBusInfo mInfo;

    PredictionAdapter(Context context, NextBusPredictions predictor, NextBusPredictions.Handler handle, NextBusInfo info) {
        mContext = context;
        mInfo = info;
        mPrediction = new ArrayList<>();
        predictor.setHandler(this);
        mHandler = handle;
    }

    public void onPrediction(ArrayList<NextBusPredictions.Prediction> predictions) {
        mPrediction.clear();

        for(NextBusPredictions.Prediction prediction : predictions) {
            for(NextBusPredictions.Time time : prediction.getTimes()) {
                mPrediction.add(time);
            }
        }

        Collections.sort(mPrediction);

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

        final NextBusPredictions.Time time = mPrediction.get(position);

        // update the route title
        TextView titleView = currentView.findViewById(R.id.title);
        titleView.setText(time.toString());

        // update the button text
        final TextView textView = currentView.findViewById(R.id.text);
        final FloatingActionButton fab = currentView.findViewById(R.id.button);

        mInfo.getStopInfo(time.getPrediction().getStopId() + "", new NextBusInfo.ResponseHandler<StopInfo>() {
            @Override
            public void onResponse(StopInfo response) {
                StopInfo.RouteInfo info = null;

                for(StopInfo.RouteInfo i : response.getRoutes()) {
                    if(i.getId().equals("" + time.getPrediction().getRouteId())) {
                        info = i;
                        break;
                    }
                }

                textView.setText(info != null ? info.getShortTitle() : "--");

                // update the color
                if(info != null) {
                    fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(info.getColor())));
                }
            }
        });

        return currentView;
    }
}
