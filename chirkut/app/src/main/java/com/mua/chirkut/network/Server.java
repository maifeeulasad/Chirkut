package com.mua.chirkut.network;

import android.util.Log;

import com.mua.chirkut.listener.IncomingConnectionListener;
import com.mua.chirkut.util.Default;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
            } catch (Exception ignored) { }
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
            //if(message!=null)
            Log.d("d--mua--net-recx",message);
            Log.d("d--mua--net-rec","----------------");
        }
    }

    String readSocketMessage(Socket socket){
        try {
            String res
                    = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(),"UTF-8"))
                    .readLine();
            return (res==null) ? "" : res;
        } catch (Exception e) {
            return "";
        }
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
