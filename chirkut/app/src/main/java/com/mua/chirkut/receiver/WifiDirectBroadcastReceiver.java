package com.mua.chirkut.receiver;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import com.mua.chirkut.listener.P2PConnectionListener;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private final WifiP2pManager mManager;
    private final WifiP2pManager.Channel mChannel;
    private final P2PConnectionListener mP2PConnectionListener;
    private final WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            mP2PConnectionListener.updateList(peerList);
        }
    };
    private boolean mConnected = false;
    private boolean mInitiated = false;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager,
                                       WifiP2pManager.Channel channel,
                                       P2PConnectionListener p2PConnectionListener) {
        super();
        mManager = manager;
        mChannel = channel;
        mP2PConnectionListener = p2PConnectionListener;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mP2PConnectionListener.wifiP2PStatus(true);
            } else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                mP2PConnectionListener.wifiP2PStatus(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (!mConnected) {
                mManager.requestPeers(mChannel, peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                mConnected = true;
                mManager.requestConnectionInfo(mChannel, wifiP2pInfo -> {
                    //todo: someone asking to be connected
                });
            } else {
                mConnected = false;
                if (mInitiated) {
                    //todo: disconnected
                } else {
                    mInitiated = true;
                    //todo: viewmodel/view
                }
            }
        }
    }
}
