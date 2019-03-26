package com.ryan3r.bustimes;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import java.util.concurrent.locks.ReentrantLock;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("arrival", "Bus arrivals", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        final String id = intent.getStringExtra("id");
        final long lTime = intent.getLongExtra("time", 0);
        final int before = intent.getIntExtra("before", 0) * 1000;

        final NextBusPredictions predictions = new NextBusPredictions(this);

        final Context self = this;

        predictions.setHandler(new NextBusPredictions.Handler() {
            @Override
            public void requestError(Throwable err) {}
            @Override
            public void onLoadStart() {}

            @Override
            public void onPrediction(ArrayList<NextBusPredictions.Prediction> preds) {
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

                if(time == null) {
                    predictions.stopPredictions();
                    stopSelf();
                    return;
                }

                if(time.getTime() - System.currentTimeMillis() <= before) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(self, "arrival")
                            .setSmallIcon(R.drawable.icon)
                            .setContentTitle("Bus arriving")
                            .setContentText(time.toString())
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    NotificationManagerCompat mngr = NotificationManagerCompat.from(self);
                    mngr.notify(1, builder.build());

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
