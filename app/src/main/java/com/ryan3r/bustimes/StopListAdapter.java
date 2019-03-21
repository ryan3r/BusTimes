package com.ryan3r.bustimes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ryan3r.bustimes.nextbusclient.StopInfo;

import java.util.List;

public class StopListAdapter extends BaseAdapter {
    private List<StopInfo> stops;
    private LayoutInflater inflator;
    private StopInfo.LatLon loc;

    StopListAdapter(List<StopInfo> s, Context ctx, StopInfo.LatLon l) {
        stops = s;
        inflator = LayoutInflater.from(ctx);
        loc = l;
    }

    public void setValues(List<StopInfo> s, StopInfo.LatLon l) {
        stops = s;
        loc = l;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public int getCount() {
        return stops.size();
    }

    @Override
    public Object getItem(int i) {
        return stops.get(i);
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = inflator.inflate(R.layout.fragment_stop_item, viewGroup, false);
        }

        // build the distance in the form of x.xx
        String distance = (((int) (stops.get(i).getCoordinates().distFrom(loc) * 1000)) / 1000.0) + "mi";

        // set the stop title and distance
        ((TextView) view.findViewById(R.id.title)).setText(stops.get(i).getTitle());
        ((TextView) view.findViewById(R.id.distance)).setText(distance);

        // add the handler to open the stop
        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // launch the stop activity
                Context context = view.getContext();
                Intent intent = new Intent(context, StopActivity.class);
                intent.putExtra("stop", stops.get(i).getId());
                context.startActivity(intent);
            }
        });

        return view;
    }
}
