package com.ryan3r.bustimes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ryan3r.bustimes.nextbusclient.NextBusInfo;
import com.ryan3r.bustimes.nextbusclient.StopInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class NearByFragment extends Fragment implements NextBusInfo.ErrorHandler {
    // a switch for development to use a fake location
    final boolean USE_REAL_LOCATION = !BuildConfig.DEBUG;

    final double NEAR_BY_DIST = 1;

    private NextBusInfo nextBusInfo;
    private LocationManager manager;
    private LocationListener locationListener;

    // we need permission when the app loads
    private boolean needPermission = false;

    // components
    private TextView errorMsg;
    private ListView listView;
    private ProgressBar loader;

    // cached stops
    private StopInfo.LatLon cachedLoc;
    private List<StopInfo> cachedStops;

    private StopListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_near_by, container, false);

        // get a next bus info fetcher
        nextBusInfo = new NextBusInfo(getContext());
        nextBusInfo.setErrorHandler(this);

        // get the views
        errorMsg = rootView.findViewById(R.id.error_msg);
        listView = rootView.findViewById(R.id.list);
        loader = rootView.findViewById(R.id.loader);

        // set up the adapter
        adapter = new StopListAdapter(new ArrayList<StopInfo>(), getContext(), new StopInfo.LatLon(0, 0));
        listView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadStops();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        loadStops();
    }

    @Override
    public void setUserVisibleHint(boolean visibleHint) {
        super.setUserVisibleHint(visibleHint);

        // request the permission
        if(visibleHint && needPermission) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);

            needPermission = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(manager != null) {
            manager.removeUpdates(locationListener);
        }
    }

    // load the near by stops
    private void loadStops() {
        if(getContext() == null || errorMsg == null) return;

        // reset everything
        errorMsg.setText("");

        if(listView.getAdapter() == null) {
            loader.setVisibility(View.VISIBLE);
        }

        // check for permission
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            errorMsg.setText(R.string.location_permission);
            loader.setVisibility(View.GONE);

            // request the permission
            if(getUserVisibleHint()) {
                ActivityCompat.requestPermissions(
                        getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
            else {
                needPermission = true;
            }

            return;
        }

        // get the user's location
        locationListener = new LocationListener() {
            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {}

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onLocationChanged(Location location) {
                if(getContext() == null) return;

                // show an error message
                if (location == null && USE_REAL_LOCATION) {
                    errorMsg.setText(R.string.location_error);
                    loader.setVisibility(View.GONE);
                    return;
                }

                // use a fake location because the emulators don't have the correct info
                final StopInfo.LatLon loc = new StopInfo.LatLon(
                        USE_REAL_LOCATION ? location.getLatitude() : 42.02347,
                        USE_REAL_LOCATION ? location.getLongitude() : -93.6497999
                );

                // approximately less than 10 ft away from the cached location
                if(cachedLoc != null && loc.distFrom(cachedLoc) < 0.00189394) {
                    // display the stops
                    adapter.setValues(cachedStops, loc);

                    // no stops to display
                    if(cachedStops.size() == 0) {
                        errorMsg.setText(
                                String.format(Locale.getDefault(), "No stops within %f miles of your current location.", NEAR_BY_DIST));
                    }

                    loader.setVisibility(View.GONE);

                    return;
                }

                // load the stops
                nextBusInfo.getAllStops(new NextBusInfo.ResponseHandler<List<StopInfo>>() {
                    @Override
                    public void onResponse(List<StopInfo> response) {
                        // remove stops that are too far away
                        for (int i = response.size() - 1; i >= 0; --i) {
                            if (response.get(i).getCoordinates().distFrom(loc) > NEAR_BY_DIST) {
                                response.remove(i);
                            }
                        }

                        // sort the remaining stops by their distance from us
                        Collections.sort(response, new Comparator<StopInfo>() {
                            @Override
                            public int compare(StopInfo a, StopInfo b) {
                                return (int) ((a.getCoordinates().distFrom(loc) - b.getCoordinates().distFrom(loc)) * 10000);
                            }
                        });

                        // cache the stops
                        cachedLoc = loc;
                        cachedStops = response;

                        // display the stops
                        adapter.setValues(response, loc);
                        loader.setVisibility(View.GONE);

                        // no stops to display
                        if(response.size() == 0) {
                            errorMsg.setText(
                                    String.format(
                                            Locale.getDefault(),
                                            "No stops within %f miles of your current location.",
                                            NEAR_BY_DIST));
                        }
                    }
                });
            }
        };

        // use the user's current location
        if(USE_REAL_LOCATION) {
            manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

            // failed to get the location manager
            if(manager == null) {
                errorMsg.setText(R.string.location_error);
                loader.setVisibility(View.GONE);
                return;
            }

            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
        // use a fake location for dev
        else {
            locationListener.onLocationChanged(null);
        }
    }

    // an error occurred in a request
    @Override
    public void requestError(Throwable err) {
        // reset everything
        adapter.setValues(new ArrayList<StopInfo>(), new StopInfo.LatLon(0, 0));
        loader.setVisibility(View.GONE);

        errorMsg.setText(String.format(Locale.getDefault(), "Failed to load stops: %s", err.getMessage()));
    }
}
