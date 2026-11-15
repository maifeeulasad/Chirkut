package com.maifee.chirkut.call.signaling;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Accepts inbound signaling TCP connections and hands each one off as its own
 * {@link TcpSignalingChannel}. The channel is then opened with a listener by
 * whatever accepted it (typically the {@code CallManager}).
 * @author Maifee Ul Asad
 */
public final class CallSignalingServer {

    public interface InboundHandler {
        void onInbound(TcpSignalingChannel channel);
    }

    private static final String TAG = "CallSignalingServer";

    private final int port;
    private ServerSocket server;
    private Thread thread;
    private volatile boolean running;

    public CallSignalingServer(int port) {
        this.port = port;
    }

    public synchronized void start(InboundHandler handler) throws IOException {
        if (running) return;
        server = new ServerSocket(port);
        running = true;
        thread = new Thread(() -> acceptLoop(handler), "CallSignalingAccept");
        thread.start();
    }

    private void acceptLoop(InboundHandler handler) {
        while (running) {
            try {
                Socket s = server.accept();
                handler.onInbound(new TcpSignalingChannel(s));
            } catch (IOException e) {
                if (running) Log.w(TAG, "accept error", e);
                break;
            }
        }
    }

    public synchronized void stop() {
        running = false;
        try { if (server != null) server.close(); } catch (IOException ignored) { }
        server = null;
    }
}
