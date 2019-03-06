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
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ryan3r.bustimes.nextbusclient.FavoriteInfo;
import com.ryan3r.bustimes.nextbusclient.NextBusPredictions;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoriteAdapter extends BaseAdapter {
    private FavoriteInfo[] favorites;
    private Context context;
    private HashMap<String, NextBusPredictions.Prediction> predictions = null;
    private boolean errorOccured;

    FavoriteAdapter(FavoriteInfo[] f, Context ctx) {
        favorites = f;
        context = ctx;
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
        return favorites.length;
    }

    /**
     * Render an item
     * @param position The index to get
     * @return The value in our underlying array
     */
    public Object getItem(int position) {
        return favorites[position];
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
            LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // if we have a null inflater bail
            if(inf == null) return null;

            currentView = inf.inflate(R.layout.fragment_favorite_item, container, false);
        }

        final FavoriteInfo info = favorites[position];

        String predId = info.getRouteId() + "|" + info.getStopId();

        // hide the loader
        ProgressBar loader = currentView.findViewById(R.id.loader);
        loader.setVisibility(View.GONE);

        // set the loader color
        int colorInt = Color.parseColor(info.getColor());
        loader.getIndeterminateDrawable().setColorFilter(colorInt, PorterDuff.Mode.SRC_IN);

        // update the prediction
        if (predictions != null) {
            TextView predText = currentView.findViewById(R.id.prediction);

            // lookup the prediction
            NextBusPredictions.Prediction pred = predictions.containsKey(predId) ? predictions.get(predId) : null;
            ArrayList<NextBusPredictions.Time> times = pred != null ? pred.getTimes() : null;

            // show the times
            if (times != null && times.size() > 0) {
                // show that a bus is arriving
                if((times.get(0).getTime() - System.currentTimeMillis()) <= 0) {
                    predText.setText(R.string.arriving);
                }
                // show the time until a bus arrives
                else {
                    predText.setText(times.get(0).getTimeUntil());
                }
            }
            // show that there is nothing
            else {
                predText.setText(R.string.none);
            }
        }
        else if (errorOccured) {
            TextView predText = currentView.findViewById(R.id.prediction);

            predText.setText(R.string.error_short);
        }
        else {
            loader.setVisibility(View.VISIBLE);
        }

        // update the route title
        TextView titleView = currentView.findViewById(R.id.title);
        titleView.setText(info.getTitle());

        // update the button text
        TextView textView = currentView.findViewById(R.id.text);
        textView.setText(info.getShortTitle());

        // update the color
        FloatingActionButton fab = currentView.findViewById(R.id.button);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(info.getColor())));

        // make the item clickable
        currentView.setClickable(true);

        currentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open the stop
                Intent intent = new Intent(context, StopActivity.class);
                intent.putExtra("stop", info.getStopId());
                intent.putExtra("route", info.getRouteId());
                context.startActivity(intent);
            }
        });

        return currentView;
    }

    // update the times
    void setTimes(ArrayList<NextBusPredictions.Prediction> preds) {
        if(preds == null) {
            predictions = null;
            errorOccured = true;
        }
        else {
            predictions = new HashMap<>();

            // convert the arraylist to a hashmap
            for(NextBusPredictions.Prediction pred : preds) {
                predictions.put(pred.getId(), pred);
            }
        }

        notifyDataSetChanged();
    }
}
