package com.mua.chirkut;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mua.chirkut.activity.ChatActivity;
import com.mua.chirkut.adapter.P2PListAdapter;
import com.mua.chirkut.databinding.ActivityMainBinding;
import com.mua.chirkut.listener.P2PConnectionListener;
import com.mua.chirkut.listener.P2PDeviceClickListener;
import com.mua.chirkut.receiver.WifiDirectBroadcastReceiver;
import com.mua.chirkut.viewmodel.MainViewModel;


public class MainActivity
        extends AppCompatActivity
        implements P2PConnectionListener, P2PDeviceClickListener {

    private final int MAX_CONNECTION_TRY = 5;
    private int connectionRetryCounter = 0;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;

    private ActivityMainBinding mBinding;
    private MainViewModel viewModel;

    private P2PListAdapter mP2PListAdapter;
    private RecyclerView mRvP2PList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mBinding.setMain(viewModel);
        mBinding.setLifecycleOwner(this);

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

        initList();
        closeAll();
        initReceiver();
        startDiscovery();
        startListenIncoming();
        mBinding.pbLoading.show();
    }

    private void initList() {
        mP2PListAdapter = new P2PListAdapter(this);
        mRvP2PList = mBinding.rvP2pDevices;
        mRvP2PList.setAdapter(mP2PListAdapter);
        mRvP2PList.setLayoutManager(new LinearLayoutManager(this));
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

    public void startListenIncoming(){
        mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                Log.d("d--mua-lp",info.groupOwnerAddress+" ip address");
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
        mP2PListAdapter.setPeerList(peerList);
    }

    @Override
    public void wifiP2PStatus(boolean status) {
        viewModel.p2pStatus.postValue(status ? "Enabled" : "Disabled");
    }

    @Override
    public void incomingConnection(WifiP2pInfo wifiP2pInfo) {
        if(wifiP2pInfo.groupOwnerAddress==null){
            return;
        }
        //todo : bug, the device first opening app is trying to become the host
        //if(!wifiP2pInfo.isGroupOwner || wifiP2pInfo.groupOwnerAddress==null)
        //    return;
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Incoming Connection Request")
                .setMessage(wifiP2pInfo.groupOwnerAddress+" wants to connect")
                .setPositiveButton("Yes", (dialogInterface, i) ->
                        openChat(wifiP2pInfo.groupOwnerAddress.toString())
                )
                .setNegativeButton("No", (dialogInterface, i) ->{
                    openChat();
                    //todo: notify requester
                }
                )
                .show();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDeviceClick(WifiP2pDevice device) {

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("d--mua-lp","success");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("d--mua-lp","failed : "+reason);
            }
        });

    }

    private void openChat(){
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    private void openChat(String groupOwnerIP){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("GROUP_OWNER_IP",groupOwnerIP);
        startActivity(intent);
    }

}