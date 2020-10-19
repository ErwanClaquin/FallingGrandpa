package com.example.fallinggrandpa;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;

public class NotificationHelper extends ContextWrapper {
    public final NotificationManager notificationmanager;
    private static final String CHANNEL_HIGH_ID = "HIGH_CHANNEL";
    private static final String CHANNEL_HIGH_NAME = "High Channel";
    private static final String CHANNEL_DEFAULT_ID = "DEFAULT_CHANNEL";
    private static final String CHANNEL_DEFAUL_NAME = "Default Channel";
    private static final String TAGNOTIF = "NotifHelper";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context base) {
        super(base);
        notificationmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel notifChanHigh = new NotificationChannel(CHANNEL_HIGH_ID, CHANNEL_HIGH_NAME, NotificationManager.IMPORTANCE_HIGH);
        notifChanHigh.setShowBadge(true);
        notifChanHigh.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationmanager.createNotificationChannel(notifChanHigh);

        NotificationChannel notifChanDefault = new NotificationChannel(CHANNEL_DEFAULT_ID, CHANNEL_DEFAUL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notifChanDefault.setShowBadge(false);
        notificationmanager.createNotificationChannel(notifChanDefault);


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notify(int id, boolean prioritary, String title, String message) {
        String channelId = prioritary ? CHANNEL_HIGH_ID : CHANNEL_DEFAULT_ID;
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), channelId)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_baseline_announcement)
                .setAutoCancel(true);

        Notification notification = builder.build();
        notificationmanager.notify(id, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void canceled(MainActivity ac) throws IOException {
        int len = notificationmanager.getActiveNotifications().length;
        if (len>0) {
            Log.v(TAGNOTIF, "notification still here");
            ac.sendtoContacts();
        } else {
            Log.v(TAGNOTIF, "notification Canceled");
        }
        notificationmanager.cancelAll(); // Dismiss every other notification since we only want one


    }

}
