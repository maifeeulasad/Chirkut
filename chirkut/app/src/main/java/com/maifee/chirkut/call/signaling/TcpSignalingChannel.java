package com.maifee.chirkut.call.signaling;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * TCP implementation of {@link SignalingChannel}. Call setup/teardown is
 * reliable but kept off the media (UDP) plane.
 * @author Maifee Ul Asad
 */
public final class TcpSignalingChannel implements SignalingChannel {

    private static final String TAG = "TcpSignalingChannel";

    private final String remoteHost;
    private final int remotePort;
    private final int connectTimeoutMs;

    private Socket socket;
    private OutputStream out;
    private Thread reader;
    private volatile boolean running;

    public TcpSignalingChannel(String remoteHost, int remotePort) {
        this(remoteHost, remotePort, 5_000);
    }

    public TcpSignalingChannel(String remoteHost, int remotePort, int connectTimeoutMs) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.connectTimeoutMs = connectTimeoutMs;
    }

    /** Wrap an already-accepted socket (server side). */
    public TcpSignalingChannel(Socket accepted) {
        this.remoteHost = accepted.getInetAddress().getHostAddress();
        this.remotePort = accepted.getPort();
        this.connectTimeoutMs = 0;
        this.socket = accepted;
    }

    @Override
    public synchronized void open(SignalingListener listener) throws IOException {
        if (socket == null) {
            socket = new Socket();
            socket.connect(new InetSocketAddress(remoteHost, remotePort), connectTimeoutMs);
        }
        out = socket.getOutputStream();
        running = true;
        reader = new Thread(() -> readLoop(listener), "TcpSignalingRx");
        reader.start();
    }

    private void readLoop(SignalingListener listener) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while (running && (line = br.readLine()) != null) {
                CallSignal sig = CallSignal.decode(line);
                if (sig != null) listener.onSignal(sig, remoteAddress());
            }
        } catch (IOException e) {
            if (running) Log.w(TAG, "read closed", e);
        } finally {
            running = false;
            listener.onClosed();
        }
    }

    @Override
    public synchronized void send(CallSignal signal) throws IOException {
        if (out == null) throw new IOException("channel not open");
        out.write((signal.encode() + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    @Override
    public String remoteAddress() { return remoteHost; }

    @Override
    public synchronized void close() {
        running = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) { }
        socket = null;
        out = null;
    }
}
