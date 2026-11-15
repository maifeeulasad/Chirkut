package com.mua.chirkut.listener;

import java.net.Socket;

public interface IncomingConnectionListener {
    void incomingSocket(Socket socket);
}
