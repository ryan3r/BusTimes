package com.ryan3r.bustimes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ryan3r.bustimes.nextbusclient.StopConfig;

public class RoutePickerDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View listView = getActivity().getLayoutInflater().inflate(R.layout.dialog_route_picker, null);

        ListView routes = listView.findViewById(R.id.routes);
        routes.setAdapter(new RoutePickerAdapter(getContext(), new StopConfig()));

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
