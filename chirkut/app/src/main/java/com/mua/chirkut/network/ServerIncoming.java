package com.mua.chirkut.network;


import android.util.Log;

import com.mua.chirkut.listener.IncomingSocketListener;

import java.io.IOException;
import java.net.Socket;

public class ServerIncoming implements Runnable{

    private final Server server;
    private final IncomingSocketListener incomingSocketListener;

    public ServerIncoming(Server server, IncomingSocketListener incomingSocketListener) {
        this.server = server;
        this.incomingSocketListener = incomingSocketListener;
    }

    @Override
    public void run() {
        while (true){
            Log.d("d--mua-zp","sehra");
            try {
                Log.d("d--mua-zp","age");
                Socket socket = server.getServerSocket().accept();
                incomingSocketListener.incomingSocket(socket);
                Log.d("d--mua-zp","pore");
            } catch (Exception e) {
                break;
            }
        }
    }
}
