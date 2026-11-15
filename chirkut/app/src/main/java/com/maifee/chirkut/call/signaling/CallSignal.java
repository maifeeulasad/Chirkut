package com.maifee.chirkut.call.signaling;

/**
 * Control-plane signal exchanged between peers. Line-oriented wire format:
 * {@code TYPE|arg1|arg2\n}.
 */
public final class CallSignal {

    public enum Type { INVITE, ACCEPT, REJECT, BYE, BUSY, PING }

    private final Type type;
    private final int mediaPort;

    public CallSignal(Type type, int mediaPort) {
        this.type = type;
        this.mediaPort = mediaPort;
    }

    public Type type() { return type; }
    public int mediaPort() { return mediaPort; }

    public String encode() {
        return type.name() + "|" + mediaPort;
    }

    public static CallSignal decode(String line) {
        if (line == null || line.isEmpty()) return null;
        String[] parts = line.split("\\|");
        try {
            Type t = Type.valueOf(parts[0]);
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return new CallSignal(t, port);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
