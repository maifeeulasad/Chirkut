package com.maifee.chirkut.listener;

import java.net.Socket;

public interface IncomingConnectionListener {
    void incomingSocket(Socket socket);
}
