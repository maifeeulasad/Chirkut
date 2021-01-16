package com.mua.chirkut.network;

import android.util.Log;

import com.mua.chirkut.util.Default;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{

    public final ServerSocket serverSocket;
    private static Server server = null;

    public static Server Server(){
        if(server==null){
            try {
                server = new Server();
            } catch (Exception ignored) { }
        }
        new Thread(Server.server).start();
        return server;
    }

    private Server() throws IOException {
        serverSocket = new ServerSocket(Default.PORT);
    }

    public Socket acceptConnection(){
        try{
            //return null;
            return serverSocket.accept();
        }catch (Exception e){
            Log.d("d--mua--net-dd",e.getMessage());
            return null;
        }
    }


    @Override
    public void run() {
        Log.d("d--mua--net","start0000000000000000000000");
        while(true){
            Log.d("d--mua--net","start");
            try {
                Log.d("d--mua--net","start-1");
                //server.acceptConnection();
                Socket socket = acceptConnection();
                Log.d("d--mua--net","start-2");
                if(socket==null) {
                    Log.d("d--mua--net","socket--null");
                }
                else {
                    Log.d("d--mua--net","ache ache");
                }
            } catch (Exception e) {
                Log.d("d--mua--net","break");
                Log.d("d--mua--net-eer",e.getMessage());
                break;
            }
        }
    }

}
