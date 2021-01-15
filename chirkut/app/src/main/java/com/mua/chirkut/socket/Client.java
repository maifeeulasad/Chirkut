package com.mua.chirkut.socket;

import android.util.Log;

import com.mua.chirkut.util.Default;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client implements Runnable {

    private final Socket client;

    public Client(String hostAddress) throws IOException {
        client = new Socket(hostAddress, Default.PORT);
    }

    public void start(){
        new Thread(this).start();
    }


    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
            for(int i=0; i<1000;i++) {
                Log.d("d--mua-mes","Sending request to Socket Server");
                if (i == 999)
                    oos.writeObject("exit");
                else
                    oos.writeObject("" + i);
                String message = (String) ois.readObject();
                Log.d("d--mua-mes","Message: " + message);
            }
            ois.close();
            oos.close();
        }catch (Exception ignored){

        }
    }
}
