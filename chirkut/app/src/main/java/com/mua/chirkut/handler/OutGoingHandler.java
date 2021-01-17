package com.mua.chirkut.handler;


import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.mua.chirkut.listener.OutgoingMessageListener;

public class OutGoingHandler extends Handler {

    private final OutgoingMessageListener outgoingMessageListener;

    public OutGoingHandler(OutgoingMessageListener outgoingMessageListener) {
        this.outgoingMessageListener = outgoingMessageListener;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        //todo:fix hardcoded
        outgoingMessageListener.outgoingMessage("192.168.0.101",msg.obj.toString());
    }
}
