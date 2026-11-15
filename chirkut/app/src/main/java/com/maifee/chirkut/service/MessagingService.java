package com.maifee.chirkut.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.maifee.chirkut.MainActivity;
import com.maifee.chirkut.R;
import com.maifee.chirkut.listener.IncomingConnectionListener;
import com.maifee.chirkut.listener.IncomingMessageListener;
import com.maifee.chirkut.listener.OutgoingMessageListener;
import com.maifee.chirkut.network.Client;
import com.maifee.chirkut.network.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Foreground service that owns one {@link Server} + a map of outbound
 * {@link Client}s to peers. Bound by {@code ChirkutMessaging} for
 * Compose-side send; incoming chat is broadcast as {@link #ACTION_INCOMING}.
 */
public class MessagingService
        extends Service
        implements IncomingMessageListener, IncomingConnectionListener, OutgoingMessageListener {

    public static final String ACTION_INCOMING = "com.maifee.chirkut.INCOMING_MESSAGE";
    public static final String EXTRA_PEER = "peer";
    public static final String EXTRA_BODY = "body";

    private static final String CHANNEL_ID = "chirkut.messaging";
    private static final int NOTIFICATION_ID = 42;
    private static final String TAG = "MessagingService";

    private final Map<String, Client> clientMapping = new HashMap<>();
    private final LocalBinder binder = new LocalBinder();
    private Server server;

    /** Binder surface for in-process callers (Compose bridge). */
    public final class LocalBinder extends Binder {
        public MessagingService service() { return MessagingService.this; }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        server = Server.getServer(this, this);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        startInForeground();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return binder; }

    @Override
    public void onDestroy() {
        for (Client c : clientMapping.values()) closeQuietly(c);
        clientMapping.clear();
        super.onDestroy();
    }

    private void startInForeground() {
        createChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, flags);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Chirkut is listening at " + localIp())
                .setSmallIcon(R.mipmap.chirkut_icon)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Messaging", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(ch);
        }
    }

    @SuppressWarnings("deprecation")
    private String localIp() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifi == null) return "unknown";
        return Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
    }

    // --- public API used by the Compose bridge ---

    /** Opens an outbound {@link Client} to [peerAddress] if we don't have one. */
    public synchronized void connect(String peerAddress) throws IOException {
        if (clientMapping.containsKey(peerAddress)) return;
        clientMapping.put(peerAddress, new Client(peerAddress));
    }

    /** Sends [body] to [peerAddress], opening a client on demand. */
    public synchronized void send(String peerAddress, String body) throws IOException {
        Client client = clientMapping.get(peerAddress);
        if (client == null) {
            client = new Client(peerAddress);
            clientMapping.put(peerAddress, client);
        }
        client.send(body + "\n");
    }

    // --- Listener callbacks (used by Server / handlers) ---

    @Override
    public void incomingMessage(String address, String message) {
        if (message == null || message.isEmpty()) return;
        Intent intent = new Intent(ACTION_INCOMING)
                .setPackage(getPackageName())
                .putExtra(EXTRA_PEER, address)
                .putExtra(EXTRA_BODY, message);
        sendBroadcast(intent);
    }

    @Override
    public void incomingSocket(Socket socket) {
        String address = socket.getInetAddress().getHostAddress();
        if (address == null) return;
        try {
            if (!clientMapping.containsKey(address)) {
                clientMapping.put(address, new Client(address));
            }
        } catch (IOException e) {
            Log.w(TAG, "return-client failed for " + address, e);
        }
    }

    @Override
    public void outgoingMessage(String address, String message) {
        try { send(address, message); } catch (IOException ignored) { }
    }

    private static void closeQuietly(Client c) {
        try { if (c != null && c.socket != null) c.socket.close(); } catch (IOException ignored) { }
    }
}
