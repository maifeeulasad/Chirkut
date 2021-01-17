package com.mua.chirkut.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mua.chirkut.BuildConfig;
import com.mua.chirkut.R;
import com.mua.chirkut.adapter.ChatAdapter;
import com.mua.chirkut.databinding.ActivityChatBinding;
import com.mua.chirkut.listener.OutgoingMessageListener;
import com.mua.chirkut.model.Message;
import com.mua.chirkut.network.Client;
import com.mua.chirkut.network.Server;
import com.mua.chirkut.service.MessagingService;
import com.mua.chirkut.viewmodel.ChatViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity
        extends AppCompatActivity {

    private ActivityChatBinding mBinding;
    private ChatViewModel viewModel;

    private List<Thread> threads=new ArrayList<>();

    private RecyclerView mRvChat;
    private ChatAdapter mChatAdapter;


    private Messenger messenger;
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            messenger = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            messenger = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        mBinding.setChat(viewModel);
        mBinding.setLifecycleOwner(this);

        initList();
        //todo: disable next line - testing purpose only
        //initMessageTest();

        initServerClient();
        initSend();
        bindService();
    }

    void bindService(){
        bindService(
                new Intent(this, MessagingService.class),
                mConnection,
                Context.BIND_IMPORTANT);
    }

    void setTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    void initServerClient(){
        threads.clear();

        String val = "";
        try {
            val = getIntent().getExtras().getString("GROUP_OWNER_IP");
        }catch (Exception ignored){ }

        setTitle(val);
    }

    void initMessageTest(){
        mBinding.btnSend.setOnClickListener(
                v -> {
                    mChatAdapter.appendMessage(new Message());
                    mBinding.rvChat.scrollToPosition(mChatAdapter.getItemCount()-1);
                }
        );
    }

    void initIp(){

    }

    void initSend(){
        mBinding.btnSend.setOnClickListener(v -> {
            String messageString = mBinding.etMessage.getText().toString();
            if(messageString.trim().equals("")){
                return;
            }
            //todo:fix hardcoded
            viewModel.insert("192.168.0.108",messageString);
            mBinding.etMessage.setText("");
            if(messenger!=null){
                android.os.Message message
                        = android.os.Message.obtain(null,0,messageString);
                try {
                    messenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void initList(){
        mChatAdapter = new ChatAdapter();
        mRvChat = mBinding.rvChat;
        mRvChat.setAdapter(mChatAdapter);
        mRvChat.setLayoutManager(new LinearLayoutManager(this));

        viewModel.messages.observe(this, messages -> {
            mChatAdapter.setMessageList(messages);
            mBinding.rvChat.scrollToPosition(messages.size() - 1);
        });
    }
}