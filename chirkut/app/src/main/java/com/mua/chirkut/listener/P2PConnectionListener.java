package com.mua.chirkut.listener;

import android.net.wifi.p2p.WifiP2pDeviceList;

public interface P2PConnectionListener {
    void updateList(WifiP2pDeviceList peerList);
    void wifiP2PStatus(boolean status);
}
