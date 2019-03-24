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
import com.ryan3r.bustimes.nextbusclient.RouteChoice;
import com.ryan3r.bustimes.nextbusclient.StopInfo;

import java.util.ArrayList;

public class RoutePickerDialog extends DialogFragment {
    private String mStopId;
    private StopInfo mStop;

    public void setStopId(String stopId) {
        mStopId = stopId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View listView = getActivity().getLayoutInflater().inflate(R.layout.dialog_route_picker, null);

        final ListView routes = listView.findViewById(R.id.routes);
        final Context ctx = getContext();

        new NextBusInfo(ctx).getStopInfo(mStopId, new NextBusInfo.ResponseHandler<StopInfo>() {
            @Override
            public void onResponse(StopInfo response) {
                mStop = response;
                routes.setAdapter(new RoutePickerAdapter(ctx, response.getRouteChoices()));
            }
        });

        builder.setTitle("Route picker")
                .setView(listView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mStop.saveRouteChoices();
                    }
                });

        return builder.create();
    }
}
