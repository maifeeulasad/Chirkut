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
            //Log.d("d--mua--net-message",msg.obj.toString());
            //todo:fix hardcoded
            outgoingMessageListener.outgoingMessage("192.168.0.101",msg.obj.toString());
        }
    }
}
