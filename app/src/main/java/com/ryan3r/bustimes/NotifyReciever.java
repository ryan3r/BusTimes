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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.ryan3r.bustimes.nextbusclient.NextBusPredictions;

import java.util.ArrayList;

public class NotifyReceiver extends BroadcastReceiver {
    final static int MAX_SLIP = 5;

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
               predictions.stopPredictions();

               Bundle info = intent.getBundleExtra("info");

               // No predictions
               if(preds.size() == 0) return;

               long lTime = intent.getLongExtra("time", 0);

               NextBusPredictions.Prediction pred = preds.get(0);
               NextBusPredictions.Time time = null;

               for(NextBusPredictions.Time possible : pred.getTimes()) {
                   if(Math.abs(possible.getTime() - lTime) < MAX_SLIP) {
                       time = possible;
                       break;
                   }
               }

               lTime = time.getTime();
               int before = info.getInt("before", 0);

               long delayTime = lTime - before - System.currentTimeMillis();

               // Show the arrival notification
               if(delayTime <= 60) {
                   Intent intent = new Intent(context.getApplicationContext(), StopActivity.class);
                   intent.putExtra("stop", info.getString("stop"));
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                   NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "arrival")
                           .setSmallIcon(R.drawable.icon)
                           .setContentTitle(info.getString("stopTitle"))
                           .setContentText(time.toString())
                           .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                           .setAutoCancel(true)
                           .setOnlyAlertOnce(true)
                           .setContentIntent(pendingIntent);

                   NotificationManagerCompat mngr = NotificationManagerCompat.from(context);
                   mngr.notify(1, builder.build());
               }
               // Keep waiting
               else {
                    delayTime = (long) (delayTime * 0.9);

                    Intent wakeIntent = new Intent(context, NotifyReceiver.class);
                    PendingIntent wakePending = PendingIntent.getBroadcast(context, 0, wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayTime, wakePending);
               }
           }
        });

        predictions.startPredictions();
        predictions.setRoutes(intent.getBundleExtra("info").getString("id"));
    }
}
