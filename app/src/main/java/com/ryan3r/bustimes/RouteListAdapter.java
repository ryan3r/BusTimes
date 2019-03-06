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

import com.ryan3r.bustimes.nextbusclient.StopInfo;

import java.util.ArrayList;

public class RouteListAdapter extends BaseAdapter {
    private ArrayList<StopInfo.RouteInfo> routes;
    private Context context;
    private ClickListener listener;

    RouteListAdapter(ArrayList<StopInfo.RouteInfo> r, Context ctx, ClickListener l) {
        routes = r;
        context = ctx;
        listener = l;
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
        return routes.size();
    }

    /**
     * Render an item
     */
    public Object getItem(int position) {
        return routes.get(position);
    }

    /**
     * Render an item
     * @param position The position of the item
     * @param currentView The view for this item
     * @param container The group for the views
     */
    public View getView(int position, View currentView, ViewGroup container) {
        if(currentView == null) {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            assert(inf != null);

            currentView = inf.inflate(R.layout.fragment_route_fab, container, false);
        }

        final StopInfo.RouteInfo info = routes.get(position);

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
                // switch views
                listener.onItemClick(info);
            }
        });

        return currentView;
    }

    // A listener for clicks
    interface ClickListener {
        void onItemClick(StopInfo.RouteInfo route);
    }
}
