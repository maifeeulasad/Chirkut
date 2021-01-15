package com.mua.chirkut;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mua.chirkut.listener.P2PConnectionListener;


public class MainActivity extends AppCompatActivity implements P2PConnectionListener {

    private final int MAX_CONNECTION_TRY = 5;
    private int connectionRetryCounter = 0;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    void init() {
        mIntentFilter = new IntentFilter();

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        closeAll();
        initReceiver();
        startDiscovery();
    }

    private void initReceiver() {
        mBroadcastReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    private void closeAll() {

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Force closing may resolve this issue.", Toast.LENGTH_LONG).show();
            }
        });
    }


    @SuppressLint("MissingPermission")
    public void startDiscovery() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "P2P success", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        if (connectionRetryCounter < MAX_CONNECTION_TRY) {
                            Toast
                                    .makeText(MainActivity.this,
                                            "P2P failed, " + connectionRetryCounter + " times. Retrying",
                                            Toast.LENGTH_LONG)
                                    .show();
                            startDiscovery();
                            connectionRetryCounter++;
                        }
                    }
                }
        );
    }

    @Override
    public void updateList(WifiP2pDeviceList peerList) {
        for(WifiP2pDevice device:peerList.getDeviceList()){
            Log.d("d--mua",device.toString());
        }
        //todo: update ui/viewmodel
    }

    @Override
    public void wifiP2PStatus(boolean status) {
        //todo: update ui/viewmodel
    }
}


class WifiDirectBroadcastReceiver extends BroadcastReceiver {
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
