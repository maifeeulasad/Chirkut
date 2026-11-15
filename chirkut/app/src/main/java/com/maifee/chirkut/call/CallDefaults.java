package com.maifee.chirkut.call;

/**
 * Transport-layer constants for the call plane. Chat still owns port 8989.
 * @author Maifee Ul Asad
 */
public final class CallDefaults {
    public static final int SIGNALING_PORT = 8990;
    public static final int MEDIA_PORT = 8991;

    private CallDefaults() { }
}
