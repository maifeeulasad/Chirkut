package com.mua.chirkut.handler;


import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mua.chirkut.listener.OutgoingMessageListener;

public class OutGoingHandler extends Handler {

    private final OutgoingMessageListener outgoingMessageListener;

    public OutGoingHandler(OutgoingMessageListener outgoingMessageListener) {
        this.outgoingMessageListener = outgoingMessageListener;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if(msg.what!=0){
            super.handleMessage(msg);
        }else{
            String data = msg.obj.toString();
            String address = data.substring(0,data.indexOf("-"));
            String message = data.substring(data.indexOf("-"));
            outgoingMessageListener.outgoingMessage(address,message);
        }
    }
}
