package com.mua.chirkut.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mua.chirkut.MainActivity;
import com.mua.chirkut.R;
import com.mua.chirkut.listener.IncomingMessageListener;
import com.mua.chirkut.network.Server;

public class MessagingService
        extends Service
        implements IncomingMessageListener {

    private static final String ID = MessagingService.class.getName();
    private final Server server;

    public MessagingService() {
        this.server = Server.getServer(this);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        startInForeground();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startInForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    ID,
                    "Server Running",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Server Running in Foreground")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    @Override
    public void incomingMessage(String address, String message) {
        if (message.equals(""))
            return;

        Intent intent = new Intent("mua.message");
        intent.putExtra("address", address);
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }
}
