package com.mua.chirkut.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Messenger;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mua.chirkut.MainActivity;
import com.mua.chirkut.R;
import com.mua.chirkut.handler.OutGoingHandler;
import com.mua.chirkut.listener.IncomingConnectionListener;
import com.mua.chirkut.listener.IncomingMessageListener;
import com.mua.chirkut.listener.OutgoingMessageListener;
import com.mua.chirkut.network.Client;
import com.mua.chirkut.network.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MessagingService
        extends Service
        implements IncomingMessageListener, IncomingConnectionListener, OutgoingMessageListener {

    private static final String ID = MessagingService.class.getName();
    private final Server server;
    private final Map<String, Client> clientMapping = new HashMap<>();
    private final OutGoingHandler mOutgoingHandler;
    private final Messenger messenger;

    public MessagingService() {
        this.server = Server.getServer(this,this);
        mOutgoingHandler = new OutGoingHandler(this);
        messenger = new Messenger(mOutgoingHandler);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        startInForeground();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
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


    public void sendMessage(String address,String message) throws IOException {
        Client client = clientMapping.get(address);
        if (client != null) {
            Toast
                    .makeText(
                            MessagingService.this.getApplicationContext(),
                            message,
                            Toast.LENGTH_LONG)
                    .show();
            client.send(message);
        }
    }

    @Override
    public void incomingSocket(Socket socket) {
        String address = socket.getInetAddress().getHostAddress();
        try {
            clientMapping.put(address, new Client(address));
        } catch (IOException ignored) { }
    }

    @Override
    public void outgoingMessage(String address, String message) {
        try {
            sendMessage(address, message);
        } catch (Exception ignored) { }
    }
}
