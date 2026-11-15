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
import com.mua.chirkut.model.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter
        extends RecyclerView.Adapter<ChatAdapter.AppUsageListViewHolder> {

    private List<Message> messageList = new ArrayList<>();

    public void appendMessage(Message message) {
        messageList.add(message);
        notifyItemRangeChanged(messageList.size()-1,1);
    }


    public void setMessageList(List<com.mua.chirkut.entity.Message> messageList){
        this.messageList.clear();
        for(com.mua.chirkut.entity.Message message:messageList){
            this.messageList.add(new Message(message.getMessage(),message.getIncoming(),message.getAddress()));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatAdapter.AppUsageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppUsageListViewHolder((LayoutInflater.from(parent.getContext()))
                .inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.AppUsageListViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.message.setText(message.getMessage());
        if(message.isIncoming()){
            holder.viewLeft.setVisibility(View.GONE);
        }else{
            holder.viewRight.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    protected class AppUsageListViewHolder extends RecyclerView.ViewHolder {
        private final TextView message;
        private final View viewLeft;
        private final View viewRight;

        AppUsageListViewHolder(View view) {
            super(view);
            message = view.findViewById(R.id.tv_message_message);
            viewLeft = view.findViewById(R.id.v_message_left);
            viewRight = view.findViewById(R.id.v_message_right);
        }
    }

}
