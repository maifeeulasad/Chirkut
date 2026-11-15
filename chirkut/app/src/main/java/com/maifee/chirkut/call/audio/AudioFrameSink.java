package com.maifee.chirkut.call.audio;

/**
 * Consumer of captured PCM frames, e.g. a network transport.
 * @author Maifee Ul Asad
 */
public interface AudioFrameSink {
    void onFrame(byte[] pcm, int offset, int length);
}
