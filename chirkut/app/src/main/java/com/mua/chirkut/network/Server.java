package com.mua.chirkut.network;

import android.util.Log;

import com.mua.chirkut.util.Default;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server implements Runnable {

    private static Server server = null;
    public final ServerSocket serverSocket;
    private final Map<String, Socket> socketMapping = new HashMap<>();


    private Server() throws IOException {
        serverSocket = new ServerSocket(Default.PORT);
    }

    public static Server Server() {
        if (server == null) {
            try {
                server = new Server();
            } catch (Exception ignored) {
            }
        }
        new Thread(Server.server).start();
        return server;
    }

    public Socket acceptConnection() {
        try {
            return serverSocket.accept();
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public void run() {
        while (true) {
            processNewConnection();
            readAllConnectionMessage();
        }
    }

    void processNewConnection(){
        new Thread(){
            @Override
            public void run() {
                Socket socket = acceptConnection();
                if (socket == null) {
                    //todo: show error
                } else {
                    socketMapping.put(socket.getInetAddress().getHostAddress(), socket);
                }
            }
        }.start();
    }
    void readAllConnectionMessage(){
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


}
