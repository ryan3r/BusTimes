package com.ryan3r.bustimes;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ryan3r.bustimes.nextbusclient.BusSchedule;
import com.ryan3r.bustimes.nextbusclient.FavoriteInfo;
import com.ryan3r.bustimes.nextbusclient.NextBusInfo;
import com.ryan3r.bustimes.nextbusclient.NextBusPredictions;
import com.ryan3r.bustimes.nextbusclient.StopInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class StopActivity extends BaseActivity implements NextBusPredictions.Handler, NextBusInfo.ErrorHandler,
        TabLayout.OnTabSelectedListener, AdapterView.OnItemSelectedListener {
    private NextBusPredictions nextbus;
    private NextBusInfo nextBusInfo;
    private FavoriteInfo favoriteInfo;
    private String routeId;

    private ProgressBar loader;
    private TextView errorText;
    private ListView stopTimes;
    private SwipeRefreshLayout refreshLayout;
    private ListView routeList;
//    private DrawerLayout drawerLayout;
//    private TabLayout tabLayout;
//    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop);

        // get ui components
        loader = findViewById(R.id.loader);
        errorText = findViewById(R.id.error_msg);
        stopTimes = findViewById(R.id.list);
        refreshLayout = findViewById(R.id.refresh_layout);
//        routeList = findViewById(R.id.routes);
//        drawerLayout = findViewById(R.id.drawer);
//        tabLayout = findViewById(R.id.tabs);
//        spinner = findViewById(R.id.spinner_nav);
        Toolbar toolbar = findViewById(R.id.toolbar);

        // set up the tabs
//        tabLayout.addTab(tabLayout.newTab().setText("Predictions"));
//        tabLayout.addTab(tabLayout.newTab().setText("Schedule"));

//        tabLayout.addOnTabSelectedListener(this);

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
//                if(tabLayout.getSelectedTabPosition() == 0) {
                    nextbus.refresh();
//                }
//                else {
//                    displaySchedule(true);
//                }
            }
        });

        // add the days for the schedule
//        String[] days = new String[] {
//                "Saturday",
//                "Monday",
//                "Tuesday",
//                "Wednesday",
//                "Thursday",
//                "Friday",
//                "Saturday"
//        };
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, days);
//        spinner.setAdapter(adapter);

        // select today in the drop down
//        Calendar calendar = Calendar.getInstance();
//
//        spinner.setSelection(calendar.get(Calendar.DAY_OF_WEEK) - 1);
//
//        spinner.setOnItemSelectedListener(this);
//
//        // hide the spinner for predictions
//        spinner.setVisibility(View.GONE);
    }

    private void showFabs(final StopInfo stop, ArrayList<StopInfo.RouteInfo> routes) {
        // switch views
        RouteListAdapter.ClickListener listener = new RouteListAdapter.ClickListener() {
            @Override
            public void onItemClick(StopInfo.RouteInfo route) {
                routeId = route.getId();
                showRoute(stop, route);

                // close the drawer
//                drawerLayout.closeDrawer(Gravity.END);
            }
        };

        routeList.setDivider(null);
        routeList.setAdapter(new RouteListAdapter(routes, this, listener));

        StopInfo.RouteInfo route = routes.get(0);

        // find the route we are on
        if(routeId != null) {
            for(StopInfo.RouteInfo info : routes) {
                if(("" + info.getId()).equals(routeId)) {
                    route = info;
                }
            }
        }
        else {
            routeId = route.getId();
        }

        showRoute(stop, route);
    }

    // show the current route
    private void showRoute(StopInfo stop, StopInfo.RouteInfo route) {
        // change the spinner color
        int colorInt = Color.parseColor(route.getColor());

        // update the status bar and action bar
        setColor(colorInt);
        findViewById(R.id.appbar).setBackgroundColor(colorInt);

        // update the tab colors
//        tabLayout.setBackgroundColor(colorInt);
//        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);

        // set the action bar title
        String title = stop.getTitle() + " (" + route.getShortTitle()  + ")";
        setTitle(title);

        // set the multitasking color and title
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
        ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(title, icon, colorInt);
        setTaskDescription(description);

        loader.getIndeterminateDrawable().setColorFilter(colorInt, PorterDuff.Mode.SRC_IN);

        refreshLayout.setColorSchemeColors(colorInt);

        // clear the old times
//        stopTimes.setAdapter(null);

        // set the routes
        nextbus.setRoutes(route.getId() + "|" + stop.getId());

        // refresh the schedule in schedule mode
//        if(tabLayout.getSelectedTabPosition() == 1) {
//            displaySchedule(false);
//        }

        // refresh the menu
        invalidateOptionsMenu();
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
        nextBusInfo.getFavorite(getIntent().getStringExtra("stop"), routeId, new NextBusInfo.ResponseHandler<FavoriteInfo>() {
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
                // we need a route id
                if(routeId == null) return false;

                // update the icon
                item.setIcon(favoriteInfo == null ? R.drawable.ic_star : R.drawable.ic_star_border);

                // toggle the favorite state
                if(favoriteInfo == null) {
                    favoriteInfo = new FavoriteInfo(
                            getIntent().getStringExtra("stop"),
                            routeId
                    );

                    nextBusInfo.saveFavorite(favoriteInfo, 1);
                }
                else {
                    nextBusInfo.saveFavorite(favoriteInfo, 2);

                    favoriteInfo = null;
                }

                return true;

            // toggle the routes drawer
//            case R.id.routes:
//                // toggle the drawers
//                if(drawerLayout.isDrawerOpen(Gravity.END)) {
//                    drawerLayout.closeDrawer(Gravity.END);
//                }
//                else {
//                    drawerLayout.openDrawer(Gravity.END);
//                }
//
//                return true;

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

        String stopId = getIntent().getStringExtra("stop");
        final StopActivity self = this;

        nextBusInfo.getStopInfo(stopId, new NextBusInfo.ResponseHandler<StopInfo>() {
            @Override
            public void onResponse(StopInfo stop) {
                //showFabs(response, response.getRoutes());

                // set the action bar title
                setTitle(stop.getTitle());

                BaseAdapter predictionsAdapter = new PredictionAdapter(self, nextbus, self, nextBusInfo);
                stopTimes.setAdapter(predictionsAdapter);

                ArrayList<String> routes = new ArrayList<>();
                for(StopInfo.RouteInfo route : stop.getRoutes()) {
                    routeId = route.getId();
                    routes.add(route.getId() + "|" + stop.getId());
                }

                nextbus.setRoutes(routes);
            }
        });

        // start requesting predictions
        nextbus.startPredictions();
    }

    // show the refresh indicator
    public void onLoadStart() {
        // clear any previous errors
        errorText.setText("");

        // show the spinner if there are no times
        if(stopTimes.getAdapter() == null || stopTimes.getAdapter().isEmpty()) {
            loader.setVisibility(View.VISIBLE);
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

//        // check if we have any predictions
//        if(savedPredictions != null && !savedPredictions.isEmpty()) {
//            ArrayList<NextBusPredictions.Time> times = savedPredictions.get(0).getTimes();
//
//            // get the times (as strings)
//            String[] strTimes = new String[times.size() + 1];
//
//            int i = 0;
//            for (NextBusPredictions.Time time : times) {
//                if((time.getTime() - System.currentTimeMillis()) <= 0) {
//                    strTimes[i++] = "Arriving";
//                }
//                else {
//                    strTimes[i++] = time.getTimeUntil() + " (" + time.getArrivalTime() + ")";
//                }
//            }
//
//            // get the time they were fetched at
//            strTimes[i] = "Loaded " + savedPredictions.get(0).fetchedAtStr() + " ago.";
//
//            ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, strTimes);
//
//            stopTimes.setAdapter(aa);
//        }
//        else {
//            errorText.setText(R.string.no_predictions);
//        }
//    }

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

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    // switch between predictions and the schedule
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        // no tab yet
        if(tab.getText() == null) return;

        // clear the stop list
        //stopTimes.setAdapter(null);
        errorText.setText("");

        if(tab.getText().equals("Predictions")) {
            nextbus.startPredictions();
//            spinner.setVisibility(View.GONE);
        }
        else {
            nextbus.stopPredictions();
            displaySchedule(false);
        }
    }

    // switch days for the schedule
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        displaySchedule(false);
    }

    // show the schedule
    private void displaySchedule(boolean reload) {
        // show the loader
        if(stopTimes.getAdapter() == null) {
            loader.setVisibility(View.VISIBLE);
        }

        // nothing to show yet
        if(routeId == null) return;

        // load the schedule
//        String day = (String) spinner.getSelectedItem();
//        String stopId = getIntent().getStringExtra("stop");
//
//        nextBusInfo.getSchedule(day, stopId, routeId, reload, new NextBusInfo.ResponseHandler<BusSchedule>() {
//            @Override
//            public void onResponse(BusSchedule schedule) {
//                // hide the loader
//                loader.setVisibility(View.GONE);
//                refreshLayout.setRefreshing(false);
//
//                if(schedule == null) {
//                    errorText.setText(R.string.no_schedule);
//                   //stopTimes.setAdapter(null);
//                }
//                else {
//                    spinner.setVisibility(View.VISIBLE);
//                    errorText.setText("");
//
//                    // display the schedule
////                    stopTimes.setAdapter(
////                            new ArrayAdapter<>(StopActivity.this, android.R.layout.simple_list_item_1, schedule.getTimes()));
//                }
//            }
//        });
    }
}