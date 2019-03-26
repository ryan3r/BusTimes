package com.ryan3r.bustimes;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ryan3r.bustimes.nextbusclient.FavoriteInfo;
import com.ryan3r.bustimes.nextbusclient.NextBusInfo;
import com.ryan3r.bustimes.nextbusclient.NextBusPredictions;
import com.ryan3r.bustimes.nextbusclient.RouteChoice;
import com.ryan3r.bustimes.nextbusclient.StopInfo;

import java.util.ArrayList;
import java.util.Locale;

public class StopActivity extends BaseActivity implements NextBusPredictions.Handler, NextBusInfo.ErrorHandler, RoutePickerDialog.Callback {
    private NextBusPredictions nextbus;
    private NextBusInfo nextBusInfo;
    private FavoriteInfo favoriteInfo;
    private String routeId;

    private ProgressBar loader;
    private TextView errorText;
    private ListView stopTimes;
    private SwipeRefreshLayout refreshLayout;
    private TextView emptyMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop);

        // get ui components
        loader = findViewById(R.id.loader);
        errorText = findViewById(R.id.error_msg);
        stopTimes = findViewById(R.id.list);
        refreshLayout = findViewById(R.id.refresh_layout);
        emptyMsg = findViewById(R.id.empty_msg);

        stopTimes.setEmptyView(emptyMsg);

        Toolbar toolbar = findViewById(R.id.toolbar);
        // make our toolbar the toolbar
        setSupportActionBar(toolbar);

        enableBackButton();

        // load the route id
        if (savedInstanceState != null && savedInstanceState.containsKey("route")) {
            routeId = savedInstanceState.getString("route");
        }
        // get the passed route id (if there is one)
        else {
            routeId = getIntent().getStringExtra("route");
        }

        // initialize the predictions
        nextbus = new NextBusPredictions(this);

        // get the stop info
        nextBusInfo = new NextBusInfo(this);

        nextBusInfo.setErrorHandler(this);

        // listen for refresh to be pulled
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                nextbus.refresh();
            }
        });
    }

    // show the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stop_menu, menu);

        if(routeId == null) return true;

        final MenuItem favorite = menu.findItem(R.id.favorite);

        // disable the star until the info loads
        favorite.setEnabled(false);

        // load the favorite info for this stop
        nextBusInfo.getFavorite(getIntent().getStringExtra("stop"), new NextBusInfo.ResponseHandler<FavoriteInfo>() {
            @Override
            public void onResponse(FavoriteInfo response) {
                favoriteInfo = response;

                favorite.setEnabled(true);

                // update the icon
                if(response != null) {
                    favorite.setIcon(R.drawable.ic_star);
                }
            }
        });

        return true;
    }

    // handle a menu item being clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            // handle adding and removing a favorite
            case R.id.favorite:
                // update the icon
                item.setIcon(favoriteInfo == null ? R.drawable.ic_star : R.drawable.ic_star_border);

                // toggle the favorite state
                if(favoriteInfo == null) {
                    favoriteInfo = new FavoriteInfo(
                            getIntent().getStringExtra("stop")
                    );

                    nextBusInfo.saveFavorite(favoriteInfo, 1);
                }
                else {
                    nextBusInfo.saveFavorite(favoriteInfo, 2);

                    favoriteInfo = null;
                }

                return true;

            // Show the route picker dialog
            case R.id.routes:
                RoutePickerDialog picker = new RoutePickerDialog();
                picker.setStopId(getIntent().getStringExtra("stop"));
                picker.setCallback(this);
                picker.show(getFragmentManager(), "route-picker-" + getIntent().getStringExtra("stop"));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        // save the current route
        if(routeId != null) {
            state.putString("route", routeId);
        }

        super.onSaveInstanceState(state);
    }

    @Override
    protected void onStart() {
        super.onStart();

        updatePredictions();

        // start requesting predictions
        nextbus.startPredictions();
    }

    public void updatePredictions() {
        String stopId = getIntent().getStringExtra("stop");
        final StopActivity self = this;

        nextBusInfo.getStopInfo(stopId, new NextBusInfo.ResponseHandler<StopInfo>() {
            @Override
            public void onResponse(StopInfo stop) {
                // set the action bar title
                setTitle(stop.getTitle());

                final BaseAdapter predictionsAdapter = new PredictionAdapter(self, nextbus, self, nextBusInfo, stop.getTitle());
                stopTimes.setAdapter(predictionsAdapter);

                ArrayList<String> routes = new ArrayList<>();
                for(RouteChoice route : stop.getRouteChoices()) {
                    if(route.isSelected()) {
                        routeId = route.getRoute().getId();
                        routes.add(routeId + "|" + stop.getId());
                    }
                }

                nextbus.setRoutes(routes);
            }
        });
    }

    // show the refresh indicator
    public void onLoadStart() {
        // clear any previous errors
        errorText.setText("");

        // show the spinner if there are no times
        if(stopTimes.getAdapter() == null || stopTimes.getAdapter().isEmpty()) {
            loader.setVisibility(View.VISIBLE);
            emptyMsg.setVisibility(View.GONE);
        }
        // show the refresh indicator if there are times
        else {
            refreshLayout.setRefreshing(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        nextbus.stopPredictions();
    }

    public void onPrediction(ArrayList<NextBusPredictions.Prediction> savedPredictions) {
        // hide loading up and old error messages
        refreshLayout.setRefreshing(false);
        loader.setVisibility(View.GONE);
        errorText.setText("");
    }

    // Show an error message
    public void requestError(Throwable err) {
        String errMsg = String.format(Locale.getDefault(), "Failed to load stops: %s", err.getMessage());

        if(stopTimes.getAdapter() == null || stopTimes.getAdapter().isEmpty()) {
            errorText.setText(errMsg);
        }
        else {
            Toast.makeText(this, R.string.failed_to_load, Toast.LENGTH_SHORT).show();
        }

        // hide the loaders
        loader.setVisibility(View.GONE);
        refreshLayout.setRefreshing(false);
    }
}