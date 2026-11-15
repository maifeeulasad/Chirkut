package com.maifee.chirkut.call;

/** Lifecycle states of a single call session. */
public enum CallState {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTING,
    ACTIVE,
    ENDED
}
