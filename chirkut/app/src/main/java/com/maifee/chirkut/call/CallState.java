package com.maifee.chirkut.call;

/**
 * Lifecycle states of a single call session.
 * @author Maifee Ul Asad
 */
public enum CallState {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTING,
    ACTIVE,
    ENDED
}
