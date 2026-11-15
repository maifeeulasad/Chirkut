package com.maifee.chirkut.call.audio;

import android.media.AudioFormat;

/** Immutable audio-pipeline parameters. 16 kHz mono PCM16, 20 ms frames. */
public final class AudioConfig {

    public static final AudioConfig DEFAULT = new AudioConfig(
            16_000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            20
    );

    private final int sampleRateHz;
    private final int inChannelMask;
    private final int outChannelMask;
    private final int encoding;
    private final int frameMillis;

    public AudioConfig(int sampleRateHz, int inChannelMask, int outChannelMask,
                       int encoding, int frameMillis) {
        this.sampleRateHz = sampleRateHz;
        this.inChannelMask = inChannelMask;
        this.outChannelMask = outChannelMask;
        this.encoding = encoding;
        this.frameMillis = frameMillis;
    }

    public int sampleRateHz() { return sampleRateHz; }
    public int inChannelMask() { return inChannelMask; }
    public int outChannelMask() { return outChannelMask; }
    public int encoding() { return encoding; }
    public int frameMillis() { return frameMillis; }

    /** Bytes per frame (one channel, 16-bit). */
    public int frameBytes() {
        int samples = sampleRateHz * frameMillis / 1000;
        return samples * 2;
    }
}
