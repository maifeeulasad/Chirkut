package com.maifee.chirkut.call;

import android.util.Log;

import com.maifee.chirkut.call.audio.AudioConfig;
import com.maifee.chirkut.call.signaling.CallSignalingServer;
import com.maifee.chirkut.call.signaling.TcpSignalingChannel;

import java.io.IOException;

/**
 * Process-wide coordinator for call sessions. One active {@link CallSession}
 * at a time; concurrent incoming invites are answered with BUSY.
 *
 * Deliberately not a static singleton — {@link com.maifee.chirkut.call.CallService}
 * holds the instance and binds its lifecycle to the foreground service.
 * @author Maifee Ul Asad
 */
public final class CallManager {

    private static final String TAG = "CallManager";

    private final AudioConfig audioConfig;
    private final int signalingPort;
    private final int mediaPort;
    private final CallListener listener;

    private final CallSignalingServer server;
    private CallSession active;

    public CallManager(AudioConfig audioConfig,
                       int signalingPort,
                       int mediaPort,
                       CallListener listener) {
        this.audioConfig = audioConfig;
        this.signalingPort = signalingPort;
        this.mediaPort = mediaPort;
        this.listener = listener;
        this.server = new CallSignalingServer(signalingPort);
    }

    public synchronized void start() throws IOException {
        server.start(this::onInbound);
    }

    public synchronized void stop() {
        if (active != null) {
            active.hangup();
            active = null;
        }
        server.stop();
    }

    public synchronized CallSession dial(String remoteHost) throws IOException {
        if (active != null && active.state() != CallState.ENDED) {
            throw new IllegalStateException("another call is active");
        }
        TcpSignalingChannel ch = new TcpSignalingChannel(remoteHost, signalingPort);
        active = new CallSession(CallRole.CALLER, ch, audioConfig, mediaPort, wrap(listener));
        active.startOutgoing();
        return active;
    }

    public synchronized CallSession current() { return active; }

    private void onInbound(TcpSignalingChannel channel) {
        synchronized (CallManager.this) {
            if (active != null && active.state() != CallState.ENDED) {
                // Busy — reject politely and close.
                try {
                    channel.open(new BusyRejector(channel));
                } catch (IOException e) {
                    Log.w(TAG, "busy response failed", e);
                }
                return;
            }
            CallSession session = new CallSession(
                    CallRole.CALLEE, channel, audioConfig, mediaPort, wrap(listener));
            active = session;
            try {
                session.startIncoming();
                listener.onIncomingCall(channel.remoteAddress());
            } catch (IOException e) {
                Log.w(TAG, "incoming setup failed", e);
                listener.onError(e);
                active = null;
            }
        }
    }

    private CallListener wrap(final CallListener delegate) {
        return new CallListener() {
            @Override public void onStateChanged(CallState state) { delegate.onStateChanged(state); }
            @Override public void onIncomingCall(String remote) { delegate.onIncomingCall(remote); }
            @Override public void onError(Throwable error) { delegate.onError(error); }
            @Override public void onCallEnded(CallEndReason reason) {
                synchronized (CallManager.this) { active = null; }
                delegate.onCallEnded(reason);
            }
        };
    }

    /** Sends BUSY and closes, without touching the real active session. */
    private static final class BusyRejector
            implements com.maifee.chirkut.call.signaling.SignalingListener {
        private final TcpSignalingChannel ch;
        BusyRejector(TcpSignalingChannel ch) { this.ch = ch; }
        @Override public void onSignal(com.maifee.chirkut.call.signaling.CallSignal s, String r) {
            try {
                ch.send(new com.maifee.chirkut.call.signaling.CallSignal(
                        com.maifee.chirkut.call.signaling.CallSignal.Type.BUSY, 0));
            } catch (IOException ignored) { }
            ch.close();
        }
        @Override public void onClosed() { }
    }
}
