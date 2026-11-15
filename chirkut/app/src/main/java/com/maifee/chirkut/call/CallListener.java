package com.maifee.chirkut.call;

/** UI-facing callbacks for a call session's lifecycle. */
public interface CallListener {
    void onStateChanged(CallState state);

    void onIncomingCall(String remoteAddress);

    void onCallEnded(CallEndReason reason);

    void onError(Throwable error);
}
