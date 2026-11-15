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
import com.mua.chirkut.listener.P2PDeviceClickListener;

import java.util.ArrayList;
import java.util.List;

public class P2PListAdapter
        extends RecyclerView.Adapter<P2PListAdapter.AppUsageListViewHolder> {

    private WifiP2pDeviceList peerList = new WifiP2pDeviceList();
    private List<WifiP2pDevice> peerArrayList = new ArrayList<>();
    private P2PDeviceClickListener clickListener;

    public P2PListAdapter(P2PDeviceClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public P2PListAdapter(WifiP2pDeviceList peerList) {
        this.peerList = peerList;
        peerArrayList = new ArrayList<>(peerList.getDeviceList());
    }

    public void setClickListener(P2PDeviceClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setPeerList(WifiP2pDeviceList peerList) {
        this.peerList = peerList;
        peerArrayList = new ArrayList<>(peerList.getDeviceList());
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
        WifiP2pDevice device = peerArrayList.get(position);
        holder.name.setText(device.deviceName);
        holder.address.setText(device.deviceAddress);
        holder.mItemView.setOnClickListener(v -> clickListener.onDeviceClick(device));
    }

    @Override
    public int getItemCount() {
        return peerList.getDeviceList().size();
    }

    protected class AppUsageListViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView address;
        private final View mItemView;

        AppUsageListViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tv_p2p_device_name);
            address = view.findViewById(R.id.tv_p2p_device_address);

            mItemView = itemView;
            //WifiP2pDevice device = peerArrayList.get(getAdapterPosition());
            //itemView.setOnClickListener(v -> clickListener.onDeviceClick(device));
        }
    }

}
