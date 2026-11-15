package com.maifee.chirkut.call.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Plays PCM frames fed via {@link #play(byte[], int, int)} through an
 * {@link AudioTrack} in streaming mode. Call {@link #start()} before pushing
 * frames and {@link #stop()} on teardown.
 * @author Maifee Ul Asad
 */
public final class AudioPlayer {

    private final AudioConfig config;
    private AudioTrack track;

    public AudioPlayer(AudioConfig config) {
        this.config = config;
    }

    public synchronized void start() {
        if (track != null) return;
        int minBuf = AudioTrack.getMinBufferSize(
                config.sampleRateHz(), config.outChannelMask(), config.encoding());
        int bufSize = Math.max(minBuf, config.frameBytes() * 4);

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        AudioFormat format = new AudioFormat.Builder()
                .setSampleRate(config.sampleRateHz())
                .setChannelMask(config.outChannelMask())
                .setEncoding(config.encoding())
                .build();

        track = new AudioTrack(attrs, format, bufSize,
                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
        track.play();
    }

    public synchronized void stop() {
        if (track != null) {
            try { track.stop(); } catch (IllegalStateException ignored) { }
            track.release();
            track = null;
        }
    }

    public void play(byte[] pcm, int offset, int length) {
        AudioTrack t = track;
        if (t != null) {
            t.write(pcm, offset, length);
        }
    }
}
