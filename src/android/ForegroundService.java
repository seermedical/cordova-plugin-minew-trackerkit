package com.minew;


import android.content.Intent;
import android.content.Context;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.IBinder;
import android.os.Bundle;
import android.annotation.TargetApi;

public class ForegroundService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("start")) {
            startPluginForegroundService(intent.getExtras());
        } else {
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    @TargetApi(26)
    private void startPluginForegroundService(Bundle extras) {
        Context context = getApplicationContext();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.deleteNotificationChannel("foreground.service.channel");

        Integer importance;

        try {
            importance = Integer.parseInt((String) extras.get("importance"));
        } catch (NumberFormatException e) {
            importance = 1;
        }

        switch(importance) {
            case 2:
                importance = NotificationManager.IMPORTANCE_DEFAULT;
                break;
            case 3:
                importance = NotificationManager.IMPORTANCE_HIGH;
                break;
            default:
                importance = NotificationManager.IMPORTANCE_LOW;
        }

        NotificationChannel channel = new NotificationChannel("foreground.service.channel", "Background Services", importance);
        channel.setDescription("Enables background processing.");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        int icon = getResources().getIdentifier((String) extras.get("icon"), "drawable", context.getPackageName());

        Notification notification = new Notification.Builder(context, "foreground.service.channel")
                .setContentTitle((CharSequence) extras.get("title"))
                .setContentText((CharSequence) extras.get("text"))
                .setOngoing(true)
                .setSmallIcon(icon == 0 ? 17301514 : icon)
                .build();

        Integer id;
        try {
            id = Integer.parseInt((String) extras.get("id"));
        } catch (NumberFormatException e) {
            id = 0;
        }

        startForeground(id != 0 ? id : 90, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}