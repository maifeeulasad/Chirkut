package com.mua.chirkut.network;

import com.mua.chirkut.util.Default;

import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread{

    public Socket socket;
    public ServerSocket serverSocket;

    public Server(){

    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(Default.PORT);
            socket = serverSocket.accept();
        } catch (Exception ignored) { }
    }

}
