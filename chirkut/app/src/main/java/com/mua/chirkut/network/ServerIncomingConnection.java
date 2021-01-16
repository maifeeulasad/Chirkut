package com.mua.chirkut.network;


import android.util.Log;

import com.mua.chirkut.listener.IncomingConnectionListener;

import java.net.Socket;

public class ServerIncomingConnection implements Runnable{

    private final Server server;
    private final IncomingConnectionListener incomingConnectionListener;

    public ServerIncomingConnection(Server server, IncomingConnectionListener incomingConnectionListener) {
        this.server = server;
        this.incomingConnectionListener = incomingConnectionListener;
    }

    @Override
    public void run() {
        while (true){
            try {
                Socket socket = server.getServerSocket().accept();
                incomingConnectionListener.incomingSocket(socket);
            } catch (Exception e) {
                break;
            }
        }
    }
}
