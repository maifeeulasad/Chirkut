package com.maifee.chirkut.call;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.maifee.chirkut.R;
import com.maifee.chirkut.call.audio.AudioConfig;

import java.io.IOException;

/**
 * Foreground service hosting the {@link CallManager}. Needed because capturing
 * the microphone from the background is restricted on API 31+.
 */
public final class CallService extends Service {

    public static final String CHANNEL_ID = "chirkut.call";
    private static final int NOTIFICATION_ID = 1337;
    private static final String TAG = "CallService";

    private final IBinder binder = new LocalBinder();
    private CallManager manager;
    private CallListener externalListener;

    public final class LocalBinder extends Binder {
        public CallService service() { return CallService.this; }
    }

    public void setListener(CallListener listener) {
        this.externalListener = listener;
    }

    public CallManager manager() { return manager; }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        manager = new CallManager(
                AudioConfig.DEFAULT,
                CallDefaults.SIGNALING_PORT,
                CallDefaults.MEDIA_PORT,
                new ForwardingListener());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = buildNotification("Chirkut call service running");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                            | ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        try {
            manager.start();
        } catch (IOException e) {
            Log.e(TAG, "manager start failed", e);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return binder; }

    @Override
    public void onDestroy() {
        if (manager != null) manager.stop();
        super.onDestroy();
    }

    private void createChannel() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Calls", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification(String text) {
        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Chirkut")
                .setContentText(text)
                .setSmallIcon(R.mipmap.chirkut_icon)
                .setOngoing(true)
                .build();
    }

    private final class ForwardingListener implements CallListener {
        @Override public void onStateChanged(CallState state) {
            if (externalListener != null) externalListener.onStateChanged(state);
        }
        @Override public void onIncomingCall(String remoteAddress) {
            if (externalListener != null) externalListener.onIncomingCall(remoteAddress);
        }
        @Override public void onCallEnded(CallEndReason reason) {
            if (externalListener != null) externalListener.onCallEnded(reason);
        }
        @Override public void onError(Throwable error) {
            if (externalListener != null) externalListener.onError(error);
        }
    }
}
