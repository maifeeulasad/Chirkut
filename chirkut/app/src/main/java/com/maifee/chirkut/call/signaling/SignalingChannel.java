package com.maifee.chirkut.call.signaling;

import java.io.IOException;

/**
 * Control-plane channel for a single peer-to-peer connection.
 * @author Maifee Ul Asad
 */
public interface SignalingChannel {
    void open(SignalingListener listener) throws IOException;

    void send(CallSignal signal) throws IOException;

    String remoteAddress();

    void close();
}
