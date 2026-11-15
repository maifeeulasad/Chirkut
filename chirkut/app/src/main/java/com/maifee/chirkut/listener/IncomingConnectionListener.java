package com.maifee.chirkut.listener;

import java.net.Socket;

/**
 * @author Maifee Ul Asad
 */
public interface IncomingConnectionListener {
    void incomingSocket(Socket socket);
}
