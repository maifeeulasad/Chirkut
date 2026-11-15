package com.maifee.chirkut.call.signaling;

public interface SignalingListener {
    void onSignal(CallSignal signal, String remoteAddress);

    void onClosed();
}
