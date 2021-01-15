package com.mua.chirkut.adapter;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mua.chirkut.R;

import java.util.ArrayList;

public class P2PListAdapter extends RecyclerView.Adapter<P2PListAdapter.AppUsageListViewHolder> {

    private WifiP2pDeviceList peerList=new WifiP2pDeviceList();

    public P2PListAdapter() {
    }

    public P2PListAdapter(WifiP2pDeviceList peerList) {
        this.peerList = peerList;
    }

    public void setPeerList(WifiP2pDeviceList peerList) {
        this.peerList = peerList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public P2PListAdapter.AppUsageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppUsageListViewHolder((LayoutInflater.from(parent.getContext()))
                .inflate(R.layout.item_p2p_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull P2PListAdapter.AppUsageListViewHolder holder, int position) {
        WifiP2pDevice device = new ArrayList<>(peerList.getDeviceList()).get(position);
        holder.name.setText(device.deviceName);
        holder.address.setText(device.deviceAddress);
    }

    @Override
    public int getItemCount() {
        return peerList.getDeviceList().size();
    }

    protected class AppUsageListViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView address;

        AppUsageListViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tv_p2p_device_name);
            address = view.findViewById(R.id.tv_p2p_device_address);
        }
    }

}
