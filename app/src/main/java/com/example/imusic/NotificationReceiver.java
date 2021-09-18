package com.example.imusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.example.imusic.ApplicationClass.ACTION_NEXT;
import static com.example.imusic.ApplicationClass.ACTION_PLAY;
import static com.example.imusic.ApplicationClass.ACTION_PREV;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context , MusicService.class);
        if (actionName != null) {
            switch (actionName) {
                case ACTION_PLAY:
                    serviceIntent.putExtra("ActionName", "playPause");
                    context.startService(serviceIntent);
                    break;
                case ACTION_NEXT:
                    serviceIntent.putExtra("ActionName", "next");
                    context.startService(serviceIntent);
                    break;
                case ACTION_PREV:
                    serviceIntent.putExtra("ActionName", "previous");
                    context.startService(serviceIntent);
                    break;
            }
        }
        int notifId = (intent.getExtras() == null )? 0 : intent.getExtras().getInt("com.example.imusic.notification_id");
        if(notifId == 1) {
            context.stopService(intent);
            Log.e("notif", "stopped");
        }
    }
}
