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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Foreground service that owns one {@link Server} + a map of outbound
 * {@link Client}s to peers.
 *
 * Peer identity comes from the {@code HELLO|<addr>} handshake the remote
 * {@link Client} sends on connect, not the raw inbound IP. That lets a
 * peer announce an address reachable from here even when the inbound
 * socket's source IP (a NAT tunnel, say) is not routable back.
 */
public class MessagingService
        extends Service
        implements IncomingMessageListener, IncomingConnectionListener, OutgoingMessageListener {

    public static final String ACTION_INCOMING = "com.maifee.chirkut.INCOMING_MESSAGE";
    public static final String EXTRA_PEER = "peer";
    public static final String EXTRA_BODY = "body";

    private static final String HELLO_PREFIX = "HELLO|";
    private static final String CHANNEL_ID = "chirkut.messaging";
    private static final int NOTIFICATION_ID = 42;
    private static final String TAG = "MessagingService";

    /** peerId (as announced via HELLO) -> outbound client */
    private final Map<String, Client> clientMapping = new ConcurrentHashMap<>();
    /** inbound socket's raw IP -> peerId learned from its HELLO */
    private final Map<String, String> peerIdByHost = new ConcurrentHashMap<>();
    private final LocalBinder binder = new LocalBinder();
    private Server server;

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
        peerIdByHost.clear();
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

    // --- public API for the Compose bridge ---

    /** Opens an outbound {@link Client} to [peerId] (host or host:port) if we don't have one. */
    public synchronized void connect(String peerId) throws IOException {
        ensureClient(peerId);
    }

    /** Sends [body] to [peerId], opening a client on demand. */
    public synchronized void send(String peerId, String body) throws IOException {
        ensureClient(peerId).send(body);
    }

    private Client ensureClient(String peerId) throws IOException {
        Client existing = clientMapping.get(peerId);
        if (existing != null) return existing;
        // Announce ourselves back as the same peerId: in a symmetric setup
        // (same hostname + port both ways) the peer should dial us the same
        // way we dial them. Callers that need an asymmetric reply address
        // can build a Client directly.
        Client fresh = new Client(peerId, peerId);
        clientMapping.put(peerId, fresh);
        return fresh;
    }

    // --- Listener callbacks (used by Server) ---

    @Override
    public void incomingMessage(String host, String message) {
        if (message == null || message.isEmpty()) return;

        if (message.startsWith(HELLO_PREFIX)) {
            String announcedId = message.substring(HELLO_PREFIX.length()).trim();
            if (announcedId.isEmpty()) return;
            Log.d(TAG, "HELLO from host=" + host + " announces peerId=" + announcedId);
            peerIdByHost.put(host, announcedId);
            // Eagerly open a reply client so UI can send to this peer without
            // the user re-entering the address.
            try { ensureClient(announcedId); } catch (IOException e) {
                Log.w(TAG, "reply-client open failed for " + announcedId, e);
            }
            return;
        }

        String peerId = peerIdByHost.getOrDefault(host, host);
        Log.d(TAG, "broadcast incoming peer=" + peerId + " body=" + message);
        Intent intent = new Intent(ACTION_INCOMING)
                .setPackage(getPackageName())
                .putExtra(EXTRA_PEER, peerId)
                .putExtra(EXTRA_BODY, message);
        sendBroadcast(intent);
    }

    @Override
    public void incomingSocket(Socket socket) {
        // Reply client is created lazily when we see the sender's HELLO.
    }

    @Override
    public void outgoingMessage(String peerId, String message) {
        try { send(peerId, message); } catch (IOException ignored) { }
    }

    private static void closeQuietly(Client c) {
        try { if (c != null && c.socket != null) c.socket.close(); } catch (IOException ignored) { }
    }
}
