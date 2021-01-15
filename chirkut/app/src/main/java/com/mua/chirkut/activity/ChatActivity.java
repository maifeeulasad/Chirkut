package com.mua.chirkut.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.mua.chirkut.R;
import com.mua.chirkut.databinding.ActivityChatBinding;
import com.mua.chirkut.socket.Client;
import com.mua.chirkut.socket.Server;
import com.mua.chirkut.viewmodel.ChatViewModel;

import java.io.IOException;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding mBinding;
    private ChatViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        mBinding.setChat(viewModel);
        mBinding.setLifecycleOwner(this);

        String val = "";
        try {
            val = getIntent().getExtras().getString("GROUP_OWNER_IP");
        }catch (Exception ignored){

        }

        try {
            if(val.equals("")){
                new Server().start();
                Toast.makeText(this,"server only",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this,"not ok ??",Toast.LENGTH_LONG).show();
                new Server().start();
                new Client(val).start();
                Toast.makeText(this,"server + client",Toast.LENGTH_LONG).show();
            }
        } catch (Exception ignored) {

        }
    }
}