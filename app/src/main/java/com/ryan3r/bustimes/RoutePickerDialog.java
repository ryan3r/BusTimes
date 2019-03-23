package com.ryan3r.bustimes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.ryan3r.bustimes.nextbusclient.NextBusInfo;
import com.ryan3r.bustimes.nextbusclient.StopConfig;

import java.util.ArrayList;

public class RoutePickerDialog extends DialogFragment {
    private String mStopId;

    public void setStopId(String stopId) {
        mStopId = stopId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View listView = getActivity().getLayoutInflater().inflate(R.layout.dialog_route_picker, null);

        final ListView routes = listView.findViewById(R.id.routes);
        final Context ctx = getContext();

        new StopConfig(new NextBusInfo(ctx), mStopId).getFullRoutes(new NextBusInfo.ResponseHandler<ArrayList<StopConfig.RouteChoice>>() {
            @Override
            public void onResponse(ArrayList<StopConfig.RouteChoice> response) {
                routes.setAdapter(new RoutePickerAdapter(ctx, response));
            }
        });

        builder.setTitle("Route picker")
                .setView(listView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        return builder.create();
    }
}
