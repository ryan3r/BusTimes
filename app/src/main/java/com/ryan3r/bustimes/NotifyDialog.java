package com.ryan3r.bustimes;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ryan3r.bustimes.nextbusclient.NextBusPredictions;

public class NotifyDialog extends DialogFragment {
    NextBusPredictions.Time mTime;
    String mStopTitle;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_notify, null);

        builder.setTitle("Notification")
                .setView(view)
                .setPositiveButton("Notify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TextView before = view.findViewById(R.id.before);
                        try {
                            int beforeTime = Integer.parseInt(before.getText().toString()) * 60;
                            Bundle info = new Bundle();
                            info.putInt("before", beforeTime);
                            info.putLong("time", mTime.getTime());
                            info.putString("id", mTime.getPrediction().getId());
                            info.putString("stop", mTime.getPrediction().getStopId() + "");
                            info.putString("stopTitle", mStopTitle);
                            info.putString("vehicle", mTime.getVehicle());

                            Intent wakeIntent = new Intent(getContext(), NotifyReceiver.class);
                            wakeIntent.putExtra("info", info);
                            //getContext().sendBroadcast(wakeIntent);
                            new NotifyReceiver().onReceive(getContext(), wakeIntent);
                        }
                        catch(NumberFormatException err) {
                            Toast.makeText(getContext(), "Time is not a number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return builder.create();
    }

    public void setTime(NextBusPredictions.Time t) {
        mTime = t;
    }
    public void setTitle(String title) { mStopTitle = title; }
}
