package com.ryan3r.bustimes;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ryan3r.bustimes.nextbusclient.RouteChoice;

import java.util.ArrayList;

public class RoutePickerAdapter extends ArrayAdapter<RouteChoice> {
    private LayoutInflater inflater;

    RoutePickerAdapter(Context ctx, ArrayList<RouteChoice> routes) {
        super(ctx, R.layout.fragment_route_picker_item, routes);
        inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_route_picker_item, null);
        }

        final RouteChoice choice = getItem(position);

        if(choice == null) return convertView;

        CheckBox selected = convertView.findViewById(R.id.selected);
        selected.setChecked(choice.isSelected());

        selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice.setSelected(!choice.isSelected());
            }
        });

        TextView routeTitle = convertView.findViewById(R.id.route);
        routeTitle.setText(choice.getRoute().getTitle());

        return convertView;
    }
}
