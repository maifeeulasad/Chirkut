package com.maifee.chirkut.call.signaling;

/**
 * @author Maifee Ul Asad
 */
public interface SignalingListener {
    void onSignal(CallSignal signal, String remoteAddress);

    void onClosed();
}
