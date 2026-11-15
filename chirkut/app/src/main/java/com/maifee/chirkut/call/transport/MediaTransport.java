package com.maifee.chirkut.call.transport;

import com.maifee.chirkut.call.audio.AudioFrameSink;

import java.io.IOException;

/**
 * Bidirectional media transport for a call. Writes locally captured frames
 * to the peer with {@link #send(byte[], int, int)} and emits peer frames
 * through the sink supplied to {@link #start(AudioFrameSink)}.
 * @author Maifee Ul Asad
 */
public interface MediaTransport {
    void start(AudioFrameSink remoteSink) throws IOException;

    void send(byte[] pcm, int offset, int length);

    void stop();
}
