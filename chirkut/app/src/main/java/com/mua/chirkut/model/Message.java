package com.mua.chirkut.model;

import java.util.Random;

public class Message {

    private String message = "";
    private boolean incoming = false;
    private String senderIp = "";

    public Message(String message, boolean isIncoming, String senderIp) {
        this.message = message;
        this.incoming = isIncoming;
        this.senderIp = senderIp;
    }

    public Message() {
        this("test"+ new Random().nextGaussian(),
                new Random().nextBoolean(),
                "ip"+new Random().nextDouble());
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }
}
