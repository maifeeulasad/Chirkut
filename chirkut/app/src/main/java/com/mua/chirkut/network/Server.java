package com.mua.chirkut.network;

import android.util.Log;

import com.mua.chirkut.listener.IncomingConnectionListener;
import com.mua.chirkut.util.Default;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server
        implements Runnable, IncomingConnectionListener {

    private static Server server = null;
    private final ServerSocket serverSocket;
    private final Map<String, Socket> socketMapping = new HashMap<>();
    private final ServerIncomingConnection serverIncomingConnection;

    private Server() throws IOException {
        serverSocket = new ServerSocket(Default.PORT);
        serverIncomingConnection = new ServerIncomingConnection(this, this);
    }

    public static Server getServer() {
        if (server == null) {
            try {
                server = new Server();
            } catch (Exception ignored) {
            }
        }
        new Thread(server.serverIncomingConnection).start();
        new Thread(Server.server).start();
        return server;
    }

    @Override
    public void run() {
        while (true) {
            readAllConnectionMessage();
        }
    }

    void readAllConnectionMessage(){
        Log.d("d--mua--zp","reading "+socketMapping.size());
        for(String key :socketMapping.keySet()){
            Socket socket = socketMapping.get(key);
            String message = readSocketMessage(socket);
            Log.d("d--mua--net-rec",key);
            Log.d("d--mua--net-rec",message);
            Log.d("d--mua--net-rec","----------------");
        }
    }

    String readSocketMessage(Socket socket){
        byte[] bytes = new byte[1024];
        boolean end = false;
        StringBuilder res = new StringBuilder();
        try
        {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            while(!end)
            {
                Log.d("d--mua--ll","mainkar chipa");
                int bytesRead = in.read(bytes);
                res.append(new String(bytes, 0, bytesRead));
                if (res.length() == 1024)
                {
                    end = true;
                }
            }
        }
        catch (Exception ignored) { }
        return res.toString();
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public Map<String, Socket> getSocketMapping() {
        return socketMapping;
    }

    @Override
    public void incomingSocket(Socket socket) {
        Log.d("d--mua-new","new");
        socketMapping.put(socket.getInetAddress().getHostAddress(), socket);
    }
}
