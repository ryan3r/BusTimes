package com.ryan3r.bustimes.watch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.wear.widget.BoxInsetLayout;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ryan3r.bustimes.nextbusclient.NextBusInfo;
import com.ryan3r.bustimes.nextbusclient.NextBusPredictions;
import com.ryan3r.bustimes.nextbusclient.StopInfo;

import java.util.ArrayList;

public class MainActivity extends WearableActivity implements NextBusPredictions.Handler, NextBusInfo.ErrorHandler {

    private NextBusPredictions nextbus;
    private TextView mTitle;
    private ListView mPredictions;
    private BoxInsetLayout mBackground;
    private ProgressBar mLoader;
    private String[] strTimes;
    private ArrayAdapter<String> aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get the ui components
        mTitle = findViewById(R.id.title);
        mPredictions = findViewById(R.id.predictions);
        mBackground = findViewById(R.id.background);
        mLoader = findViewById(R.id.loader);

        // Enables Always-on
        setAmbientEnabled();

        nextbus = new NextBusPredictions(this);
        nextbus.setHandler(this);

        NextBusInfo info = new NextBusInfo(this);
        info.setErrorHandler(this);

        final String stopId = "2051";
        final MainActivity a = this;

        info.getStopInfo(stopId, new NextBusInfo.ResponseHandler<StopInfo>() {
            @Override
            public void onResponse(StopInfo response) {
                // update the page
                mTitle.setText(response.getTitle());

                ArrayList<String> routes = new ArrayList<>();
                for(StopInfo.RouteInfo route : response.getRoutes()) {
                    routes.add(route.getId() + "|" + stopId);
                }

                strTimes = new String[routes.size()];
                for(int i = 0; i < strTimes.length; ++i) strTimes[i] = "";
                aa = new ArrayAdapter<String>(a, android.R.layout.simple_list_item_1, strTimes);
                mPredictions.setAdapter(aa);

                nextbus.setRoutes(routes);
            }
        });
    }

    @Override
    public void onLoadStart() {
        mLoader.setVisibility(View.VISIBLE);
//        mPredictions.setText("");
    }

    @Override
    public void requestError(Throwable err) {
//        mPredictions.setText(err.getMessage());
    }

    @Override
    public void onPrediction(ArrayList<NextBusPredictions.Prediction> predictions) {
        // hide loading up and old error messages
        mLoader.setVisibility(View.GONE);

        // check if we have any predictions
        if(predictions != null && !predictions.isEmpty()) {
            int i = 0;
            for(NextBusPredictions.Prediction prediction : predictions) {
                ArrayList<NextBusPredictions.Time> times = prediction.getTimes();

                for (NextBusPredictions.Time time : times) {
                    strTimes[i] = "";
                    if ((time.getTime() - System.currentTimeMillis()) <= 0) {
                        strTimes[i++] = "Arriving";
                    } else {
                        strTimes[i++] = time.getTimeUntil() + " (" + time.getArrivalTime() + ")";
                    }
                    break;
                }

                // get the time they were fetched at
                //strTimes += "Loaded " + predictions.get(0).fetchedAtStr() + " ago.";
                aa.notifyDataSetChanged();
            }
        }
        else {
//            mPredictions.setText(R.string.no_predictions);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        nextbus.startPredictions();
    }

    @Override
    protected void onStop() {
        super.onStop();

        nextbus.stopPredictions();
    }
}
