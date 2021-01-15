package com.mua.chirkut.listener;

import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;

public interface P2PConnectionListener {
    void updateList(WifiP2pDeviceList peerList);

    void wifiP2PStatus(boolean status);

    void incomingConnection(WifiP2pInfo wifiP2pInfo);
}
