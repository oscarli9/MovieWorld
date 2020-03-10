package com.hfad.movieworld.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class TimerService extends Service {
    private final IBinder binder = new TimerBinder();
    private int seconds = 0;
    final Handler handler = new Handler(Looper.getMainLooper());

    public class TimerBinder extends Binder {
        public TimerService getTimer() {
            return TimerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (seconds % 900 == 0 && seconds > 0) {
                    autoToast();
                } else {
                    seconds++;
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void autoToast() {
        String text = "You have spent " + seconds / 60 + " minutes writing this post!";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    public void autoNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.sym_def_app_icon)
                        .setContentTitle("Time reminder")
                        .setContentText("You have spent " + seconds/ 60 + " minutes writing this post!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(notificationManager != null) {
            int NOTIFICATION_ID = 1029;
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
