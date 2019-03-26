package com.ryan3r.bustimes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
                            Intent notify = new Intent(getContext(), NotifyService.class);
                            notify.putExtra("before", beforeTime);
                            notify.putExtra("time", mTime.getTime());
                            notify.putExtra("id", mTime.getPrediction().getId());
                            notify.putExtra("stop", mTime.getPrediction().getStopId() + "");
                            notify.putExtra("stopTitle", mStopTitle);
                            getActivity().startService(notify);
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
