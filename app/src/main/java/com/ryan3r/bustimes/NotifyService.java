package com.ryan3r.bustimes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.ryan3r.bustimes.nextbusclient.NextBusPredictions;

import java.util.ArrayList;

public class NotifyService extends Service {
    private static int MAX_SLIP = 5 * 60;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null) return START_NOT_STICKY;

        // Create a channel for the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("arrival", "Bus arrivals", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        final String id = intent.getStringExtra("id");
        final long lTime = intent.getLongExtra("time", 0);
        final int before = intent.getIntExtra("before", 0) * 1000;
        final String stopId = intent.getStringExtra("stop");
        final String stopTitle = intent.getStringExtra("stopTitle");

        final NextBusPredictions predictions = new NextBusPredictions(this);

        final Context self = this;

        // Count down until we are ready
        predictions.setHandler(new NextBusPredictions.Handler() {
            @Override
            public void requestError(Throwable err) {}
            @Override
            public void onLoadStart() {}

            @Override
            public void onPrediction(ArrayList<NextBusPredictions.Prediction> preds) {
                // No predictions
                if(preds.size() == 0) {
                    predictions.stopPredictions();
                    stopSelf();
                    return;
                }

                NextBusPredictions.Prediction pred = preds.get(0);
                NextBusPredictions.Time time = null;

                for(NextBusPredictions.Time possible : pred.getTimes()) {
                    if(possible.getTime() - lTime < MAX_SLIP) {
                        time = possible;
                        break;
                    }
                }

                // No time matching the one we were given
                if(time == null) {
                    predictions.stopPredictions();
                    stopSelf();
                    return;
                }

                // Time to notify the user
                if(time.getTime() - System.currentTimeMillis() <= before) {
                    Intent intent = new Intent(getApplicationContext(), StopActivity.class);
                    intent.putExtra("stop", stopId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(self, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(self, "arrival")
                            .setSmallIcon(R.drawable.icon)
                            .setContentTitle(stopTitle)
                            .setContentText(time.toString())
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .setOnlyAlertOnce(true)
                            .setContentIntent(pendingIntent);

                    NotificationManagerCompat mngr = NotificationManagerCompat.from(self);
                    mngr.notify(id.hashCode(), builder.build());

                    predictions.stopPredictions();
                    stopSelf();
                }
            }
        });

        predictions.startPredictions();
        predictions.setRoutes(id);

        return START_REDELIVER_INTENT;
    }
}
