package com.mua.chirkut.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mua.chirkut.listener.IncomingMessageListener;

public class MessageReceiver extends BroadcastReceiver {

    private final IncomingMessageListener incomingMessageListener;

    public MessageReceiver(IncomingMessageListener incomingMessageListener) {
        this.incomingMessageListener = incomingMessageListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String address = extras.getString("address");
            String message = extras.getString("message");
            incomingMessageListener.incomingMessage(address,message);
        }
    }
}
