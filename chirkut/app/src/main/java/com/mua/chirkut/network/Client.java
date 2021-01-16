package com.mua.chirkut.network;

import com.mua.chirkut.util.Default;

import java.net.InetSocketAddress;
import java.net.Socket;

public class Client extends Thread {

    public Socket socket;
    public String hostAddress;

    public Client(String hostAddress) {
        this.hostAddress = hostAddress;
        this.socket = new Socket();
    }

    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(hostAddress, Default.PORT));
        } catch (Exception ignored) { }
    }

}
