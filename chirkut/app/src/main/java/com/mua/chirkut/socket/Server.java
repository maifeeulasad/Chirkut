package com.mua.chirkut.socket;

import android.util.Log;

import com.mua.chirkut.util.Default;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    private final ServerSocket server;

    public Server() throws IOException{
        server = new ServerSocket(Default.PORT);
    }

    public void start() {
        new Thread(this).start();
    }


    @Override
    public void run() {
        Log.d("d--mua-mes","Waiting for the client request");
        try{
            while(true){
                Socket socket = server.accept();
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                String message = (String) ois.readObject();
                Log.d("d--mua-mes","Message Received: " + message);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject("Hi Client "+message);
                ois.close();
                oos.close();
                socket.close();
                if(message.equalsIgnoreCase("exit")) {
                    server.close();
                }
            }
        }catch (Exception ignored){

        }
    }
}
