package com.maifee.chirkut.call;

import android.util.Log;

import com.maifee.chirkut.call.audio.AudioCapturer;
import com.maifee.chirkut.call.audio.AudioConfig;
import com.maifee.chirkut.call.audio.AudioPlayer;
import com.maifee.chirkut.call.signaling.CallSignal;
import com.maifee.chirkut.call.signaling.SignalingChannel;
import com.maifee.chirkut.call.signaling.SignalingListener;
import com.maifee.chirkut.call.transport.MediaTransport;
import com.maifee.chirkut.call.transport.UdpMediaTransport;

import java.io.IOException;
import java.net.InetAddress;

/**
 * One peer-to-peer call. Owns signaling + media + audio pipelines and drives
 * the {@link CallState} machine. Collaborators are injected so the class is
 * transport-agnostic and easy to test.
 * @author Maifee Ul Asad
 */
public final class CallSession implements SignalingListener {

    private static final String TAG = "CallSession";

    private final CallRole role;
    private final SignalingChannel signaling;
    private final AudioConfig audioConfig;
    private final CallListener listener;
    private final int localMediaPort;

    private CallState state = CallState.IDLE;
    private MediaTransport media;
    private AudioCapturer capturer;
    private AudioPlayer player;

    public CallSession(CallRole role,
                       SignalingChannel signaling,
                       AudioConfig audioConfig,
                       int localMediaPort,
                       CallListener listener) {
        this.role = role;
        this.signaling = signaling;
        this.audioConfig = audioConfig;
        this.localMediaPort = localMediaPort;
        this.listener = listener;
    }

    public CallState state() { return state; }
    public CallRole role() { return role; }
    public String remoteAddress() { return signaling.remoteAddress(); }

    /** Caller: open signaling and send INVITE. */
    public synchronized void startOutgoing() throws IOException {
        signaling.open(this);
        signaling.send(new CallSignal(CallSignal.Type.INVITE, localMediaPort));
        transition(CallState.OUTGOING_RINGING);
    }

    /** Callee: signaling already open; wait for user to accept/reject. */
    public synchronized void startIncoming() throws IOException {
        signaling.open(this);
        transition(CallState.INCOMING_RINGING);
    }

    public synchronized void accept() throws IOException {
        if (state != CallState.INCOMING_RINGING) return;
        signaling.send(new CallSignal(CallSignal.Type.ACCEPT, localMediaPort));
        // Media port of remote peer was learned when INVITE arrived, but to keep
        // the session self-contained the caller will also carry the port in
        // ACCEPT handling below.
    }

    public synchronized void reject() {
        try {
            signaling.send(new CallSignal(CallSignal.Type.REJECT, 0));
        } catch (IOException ignored) { }
        teardown(CallEndReason.LOCAL_HANGUP);
    }

    public synchronized void hangup() {
        try {
            signaling.send(new CallSignal(CallSignal.Type.BYE, 0));
        } catch (IOException ignored) { }
        teardown(CallEndReason.LOCAL_HANGUP);
    }

    @Override
    public synchronized void onSignal(CallSignal signal, String remoteAddress) {
        try {
            switch (signal.type()) {
                case INVITE:
                    // Callee side: remember the caller's media port so we can
                    // reply to the right UDP endpoint once accepted.
                    beginMedia(remoteAddress, signal.mediaPort());
                    // State was INCOMING_RINGING; nothing else to do here.
                    break;
                case ACCEPT:
                    // Caller side: peer accepted; wire up media.
                    beginMedia(remoteAddress, signal.mediaPort());
                    transition(CallState.ACTIVE);
                    break;
                case REJECT:
                    teardown(CallEndReason.REJECTED);
                    break;
                case BUSY:
                    teardown(CallEndReason.BUSY);
                    break;
                case BYE:
                    teardown(CallEndReason.REMOTE_HANGUP);
                    break;
                case PING:
                    // keep-alive; ignored for now
                    break;
            }
        } catch (IOException e) {
            Log.w(TAG, "signal handling failed", e);
            listener.onError(e);
            teardown(CallEndReason.NETWORK_ERROR);
        }
    }

    @Override
    public synchronized void onClosed() {
        if (state == CallState.ENDED) return;
        teardown(CallEndReason.NETWORK_ERROR);
    }

    private void beginMedia(String remoteAddress, int remotePort) throws IOException {
        if (media != null) return;
        transition(CallState.CONNECTING);
        InetAddress addr = InetAddress.getByName(remoteAddress);
        media = new UdpMediaTransport(addr, remotePort, localMediaPort);
        player = new AudioPlayer(audioConfig);
        player.start();
        media.start((pcm, offset, length) -> player.play(pcm, offset, length));
        capturer = new AudioCapturer(audioConfig,
                (pcm, offset, length) -> media.send(pcm, offset, length));
        capturer.start();
        if (role == CallRole.CALLEE) {
            transition(CallState.ACTIVE);
        }
    }

    private void teardown(CallEndReason reason) {
        if (state == CallState.ENDED) return;
        if (capturer != null) { capturer.stop(); capturer = null; }
        if (player != null) { player.stop(); player = null; }
        if (media != null) { media.stop(); media = null; }
        signaling.close();
        transition(CallState.ENDED);
        listener.onCallEnded(reason);
    }

    private void transition(CallState next) {
        this.state = next;
        listener.onStateChanged(next);
    }
}
