package com.maifee.chirkut.util;

/**
 * Transport-layer defaults. {@link #PORT} is the preferred chat port; the
 * {@link com.maifee.chirkut.network.Server} tries this first and then falls
 * back to {@link #PORT_RANGE_START}..{@link #PORT_RANGE_END} one by one so a
 * busy port doesn't block the app from starting at all.
 * @author Maifee Ul Asad
 */
public class Default {
    public static final int PORT = 8989;
    public static final int PORT_RANGE_START = 48989;
    public static final int PORT_RANGE_END = 49989;
}
