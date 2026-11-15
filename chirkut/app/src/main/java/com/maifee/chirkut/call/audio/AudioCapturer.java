package com.maifee.chirkut.call.audio;

import android.annotation.SuppressLint;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Captures PCM frames from the microphone on its own thread and delivers them
 * to an {@link AudioFrameSink}. Owns its {@link AudioRecord}; safe to start/stop
 * repeatedly across call sessions.
 */
public final class AudioCapturer {

    private static final String TAG = "AudioCapturer";

    private final AudioConfig config;
    private final AudioFrameSink sink;

    private AudioRecord record;
    private Thread worker;
    private volatile boolean running;

    public AudioCapturer(AudioConfig config, AudioFrameSink sink) {
        this.config = config;
        this.sink = sink;
    }

    @SuppressLint("MissingPermission")
    public synchronized void start() {
        if (running) return;
        int minBuf = AudioRecord.getMinBufferSize(
                config.sampleRateHz(), config.inChannelMask(), config.encoding());
        int bufSize = Math.max(minBuf, config.frameBytes() * 4);
        record = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                config.sampleRateHz(),
                config.inChannelMask(),
                config.encoding(),
                bufSize);
        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord failed to initialize");
            record.release();
            record = null;
            return;
        }
        record.startRecording();
        running = true;
        worker = new Thread(this::loop, "AudioCapturer");
        worker.start();
    }

    public synchronized void stop() {
        running = false;
        if (worker != null) {
            try { worker.join(500); } catch (InterruptedException ignored) { }
            worker = null;
        }
        if (record != null) {
            try { record.stop(); } catch (IllegalStateException ignored) { }
            record.release();
            record = null;
        }
    }

    private void loop() {
        byte[] frame = new byte[config.frameBytes()];
        while (running && record != null) {
            int read = record.read(frame, 0, frame.length);
            if (read > 0) {
                sink.onFrame(frame, 0, read);
            }
        }
    }
}
