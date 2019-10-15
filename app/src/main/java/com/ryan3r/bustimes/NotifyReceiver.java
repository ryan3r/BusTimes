package com.ryan3r.bustimes;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ryan3r.bustimes.nextbusclient.NextBusPredictions;

import java.util.ArrayList;

public class NotifyReceiver extends BroadcastReceiver {
    final static int MAX_SLIP = 13 * 60 * 1000; // 13 minutes
    final static int MAX_INTERVAL = 5 * 60 * 1000; // 5 minutes
    final static double PERCENT_OF_TIME = 0.8;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // Create a channel for the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("arrival", "Bus arrivals", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            channel = new NotificationChannel("status", "Bus status", NotificationManager.IMPORTANCE_MIN);
            manager.createNotificationChannel(channel);
        }

        final NextBusPredictions predictions = new NextBusPredictions(context);

        predictions.setHandler(new NextBusPredictions.Handler() {
           @Override
           public void requestError(Throwable err) {}

           @Override
           public void onLoadStart() {}

           @Override
           public void onPrediction(ArrayList<NextBusPredictions.Prediction> preds) {
               Bundle info = intent.getBundleExtra("info");

               // No predictions
               if(preds.size() == 0) return;

               long lTime = info.getLong("time", 0);

               NextBusPredictions.Prediction pred = preds.get(0);
               NextBusPredictions.Time time = null;

               for(NextBusPredictions.Time possible : pred.getTimes()) {
                   if(Math.abs(possible.getTime() - lTime) < MAX_SLIP && possible.getVehicle().equals(info.getString("vehicle"))) {
                       time = possible;
                       break;
                   }
               }

               if(time != null) lTime = time.getTime();
               long before = info.getInt("before", 0) * 1000;

               long delayTime = lTime - before - System.currentTimeMillis();

               // Show the arrival notification
               if(delayTime <= 60 * 1000 /* 60 seconds */) {
                   Intent intent = new Intent(context.getApplicationContext(), StopActivity.class);
                   intent.putExtra("stop", info.getString("stop"));
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                   NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "arrival")
                           .setSmallIcon(R.drawable.icon)
                           .setContentTitle(info.getString("stopTitle"))
                           .setContentText(time != null ? time.toString() : "Tracker lost")
                           .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                           .setAutoCancel(true)
                           .setOnlyAlertOnce(true)
                           .setContentIntent(pendingIntent);

                   NotificationManagerCompat mngr = NotificationManagerCompat.from(context);
                   mngr.notify(info.getString("id").hashCode(), builder.build());
               }
               // Keep waiting
               else {
                    delayTime = (long) (delayTime * PERCENT_OF_TIME);
                    delayTime = Math.min(delayTime, MAX_INTERVAL);

                    info.putLong("time", lTime);
                    Intent wakeIntent = new Intent(context, NotifyReceiver.class);
                    wakeIntent.putExtra("info", info);
                    PendingIntent wakePending = PendingIntent.getBroadcast(context, 0, wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayTime, wakePending);
               }
           }
        });

        predictions.setRoutes(intent.getBundleExtra("info").getString("id"));
        predictions.predict();
    }
}
