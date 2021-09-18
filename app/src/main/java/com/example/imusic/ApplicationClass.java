package com.example.imusic;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ApplicationClass extends Application {

    public static final String CHANNEL_ID_1 = "CHANNEL_1";
    public static final String CHANNEL_ID_2 = "CHANNEL_2";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREV = "PREVIOUS";
    public static final String ACTION_PLAY = "PLAY";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
//            NotificationChannel notificationChannel1 = new NotificationChannel(CHANNEL_ID_1,
//                    "Channel(1)", NotificationManager.IMPORTANCE_LOW);
//            notificationChannel1.setDescription("channel 1 description");

            NotificationChannel notificationChannel2 = new NotificationChannel(CHANNEL_ID_2,
                    "Current Track", NotificationManager.IMPORTANCE_LOW);
            notificationChannel2.setDescription("channel 2 description");
            notificationChannel2.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel2.setShowBadge(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(notificationChannel1);
            notificationManager.createNotificationChannel(notificationChannel2);
        }
    }
}
