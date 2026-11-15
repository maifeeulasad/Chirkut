package com.maifee.chirkut.call.transport;

import android.util.Log;

import com.maifee.chirkut.call.audio.AudioFrameSink;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * UDP-backed {@link MediaTransport}. Low-latency, best-effort: lost frames are
 * dropped, matching VoIP semantics. Uses a single socket for both directions.
 */
public final class UdpMediaTransport implements MediaTransport {

    private static final String TAG = "UdpMediaTransport";

    private final InetAddress remoteAddr;
    private final int remotePort;
    private final int localPort;

    private DatagramSocket socket;
    private Thread receiver;
    private volatile boolean running;

    public UdpMediaTransport(InetAddress remoteAddr, int remotePort, int localPort) {
        this.remoteAddr = remoteAddr;
        this.remotePort = remotePort;
        this.localPort = localPort;
    }

    @Override
    public synchronized void start(AudioFrameSink remoteSink) throws IOException {
        if (running) return;
        socket = new DatagramSocket(new InetSocketAddress(localPort));
        socket.setReceiveBufferSize(64 * 1024);
        running = true;
        receiver = new Thread(() -> receiveLoop(remoteSink), "UdpMediaRx");
        receiver.start();
    }

    private void receiveLoop(AudioFrameSink sink) {
        byte[] buf = new byte[2048];
        DatagramPacket pkt = new DatagramPacket(buf, buf.length);
        while (running) {
            try {
                socket.receive(pkt);
                sink.onFrame(pkt.getData(), pkt.getOffset(), pkt.getLength());
            } catch (SocketException se) {
                if (running) Log.w(TAG, "socket closed", se);
                break;
            } catch (IOException e) {
                Log.w(TAG, "recv error", e);
            }
        }
    }

    @Override
    public void send(byte[] pcm, int offset, int length) {
        DatagramSocket s = socket;
        if (s == null || !running) return;
        try {
            DatagramPacket pkt = new DatagramPacket(pcm, offset, length, remoteAddr, remotePort);
            s.send(pkt);
        } catch (IOException e) {
            Log.w(TAG, "send failed", e);
        }
    }

    @Override
    public synchronized void stop() {
        running = false;
        if (socket != null) {
            socket.close();
            socket = null;
        }
        if (receiver != null) {
            try { receiver.join(500); } catch (InterruptedException ignored) { }
            receiver = null;
        }
    }
}
