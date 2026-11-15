package com.mua.chirkut.network;

import com.mua.chirkut.util.Default;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client{

    public Socket socket;
    public String hostAddress;

    public Client(String hostAddress) throws IOException {
        this.hostAddress = hostAddress;
        this.socket = new Socket();
        this.socket.connect(new InetSocketAddress(hostAddress, Default.PORT));

    }

    public void send(String message) throws IOException {
        socket.getOutputStream().write(message.getBytes());
    }

}
