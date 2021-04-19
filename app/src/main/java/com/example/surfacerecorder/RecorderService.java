package com.example.surfacerecorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.example.surfacerecorder.MainActivity.LOG_TAG;

public class RecorderService extends Service {

    public static final String CHANNEL_ID = "RecorderChannel";
    public static final String CHANNEL_NAME = "SurfaceRecorder";
    public static final String APP_NAME = "Surface Recorder";

    private Handler handler;
    private Runnable runnable;

    private Notification updateNotification() {

        Context context = getApplicationContext();

        PendingIntent action = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT); /* Flag indicating that if the described PendingIntent already exists,
                                                      the current one should be cancelled before generating a new one.*/

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Surface Recorder notification channel");

            manager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        }
        else
        {
            builder = new NotificationCompat.Builder(context);
        }

        return builder.setContentIntent(action)
                .setContentTitle(APP_NAME)
                .setContentText("Recording...  " + "00:00")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOngoing(true)
                .setOngoing(true).build();

    }

    @Override
    public void onCreate() { super.onCreate(); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getStringExtra("ACTION");
        Log.v(LOG_TAG, "action: "+action);

        try {
            //if ("start" == action) {
                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        startForeground(101, updateNotification());
                        handler.postDelayed(this, 0);
                    }
                };

                handler.post(runnable);

              /*} else if ("stop" == action) {
                handler.removeCallbacks(runnable);
                stopForeground(true);
                stopSelf();
                Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Service intent invalid", Toast.LENGTH_SHORT).show();
            }*/
        }catch (Exception e){
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "Service Destroyed");
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
