package com.maifee.chirkut.network;

import android.util.Log;

import com.maifee.chirkut.listener.IncomingConnectionListener;
import com.maifee.chirkut.listener.IncomingMessageListener;
import com.maifee.chirkut.util.Default;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Accepts inbound TCP peers on {@link Default#PORT} and spawns one reader
 * thread per connection. Each reader uses a persistent {@link BufferedReader}
 * (the previous busy-loop re-created one per call and dropped buffered
 * bytes). A readLine returning null closes the peer cleanly.
 */
public class Server implements IncomingConnectionListener {

    private static final String TAG = "Server";

    private static Server server = null;

    private final ServerSocket serverSocket;
    private final Map<String, Socket> socketMapping = new ConcurrentHashMap<>();
    private final ServerIncomingConnection serverIncomingConnection;
    private final IncomingMessageListener incomingMessageListener;
    private final IncomingConnectionListener incomingConnectionListener;

    private Server(IncomingMessageListener incomingMessageListener,
                   IncomingConnectionListener incomingConnectionListener) throws IOException {
        this.serverSocket = new ServerSocket(Default.PORT);
        this.serverIncomingConnection = new ServerIncomingConnection(this, this);
        this.incomingMessageListener = incomingMessageListener;
        this.incomingConnectionListener = incomingConnectionListener;
    }

    public static synchronized Server getServer(
            IncomingMessageListener incomingMessageListener,
            IncomingConnectionListener incomingConnectionListener) {
        if (server == null) {
            try {
                server = new Server(incomingMessageListener, incomingConnectionListener);
                new Thread(server.serverIncomingConnection, "Server-Accept").start();
            } catch (IOException e) {
                Log.e(TAG, "server start failed", e);
            }
        }
        return server;
    }

    public ServerSocket getServerSocket() { return serverSocket; }

    public Map<String, Socket> getSocketMapping() { return socketMapping; }

    @Override
    public void incomingSocket(Socket socket) {
        String address = socket.getInetAddress().getHostAddress();
        if (address == null) address = "unknown";
        Log.d(TAG, "accepted socket from " + address);
        socketMapping.put(address, socket);
        incomingConnectionListener.incomingSocket(socket);
        new Thread(new SocketReader(socket, address), "Server-Reader-" + address).start();
    }

    private final class SocketReader implements Runnable {
        private final Socket socket;
        private final String address;

        SocketReader(Socket socket, String address) {
            this.socket = socket;
            this.address = address;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d(TAG, "<- " + address + ": " + line);
                    incomingMessageListener.incomingMessage(address, line);
                }
            } catch (IOException e) {
                Log.w(TAG, "reader closed for " + address, e);
            } finally {
                socketMapping.remove(address);
                try { socket.close(); } catch (IOException ignored) { }
            }
        }
    }
}
