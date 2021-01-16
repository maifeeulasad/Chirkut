package com.mua.chirkut.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mua.chirkut.BuildConfig;
import com.mua.chirkut.R;
import com.mua.chirkut.adapter.ChatAdapter;
import com.mua.chirkut.databinding.ActivityChatBinding;
import com.mua.chirkut.model.Message;
import com.mua.chirkut.network.Client;
import com.mua.chirkut.network.Server;
import com.mua.chirkut.viewmodel.ChatViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding mBinding;
    private ChatViewModel viewModel;

    private List<Thread> threads=new ArrayList<>();

    private RecyclerView mRvChat;
    private ChatAdapter mChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        mBinding.setChat(viewModel);
        mBinding.setLifecycleOwner(this);

        initList();
        //todo: disable next line - testing purpose
        initMessageTest();

        initServerClient();
    }

    void initServerClient(){
        threads.clear();

        String val = "";
        try {
            val = getIntent().getExtras().getString("GROUP_OWNER_IP");
        }catch (Exception ignored){ }

        if(val.equals("")){
            threads.add(new Server());
            Toast.makeText(this,"server only",Toast.LENGTH_LONG).show();
        }else{
            threads.add(new Server());
            threads.add(new Client(val));
            Toast.makeText(this,"server + client",Toast.LENGTH_LONG).show();
        }
    }

    void initMessageTest(){
        mBinding.btnSend.setOnClickListener(
                v -> {
                    mChatAdapter.appendMessage(new Message());
                    mBinding.rvChat.scrollToPosition(mChatAdapter.getItemCount()-1);
                }
        );
    }

    void initList(){
        mChatAdapter = new ChatAdapter();
        mRvChat = mBinding.rvChat;
        mRvChat.setAdapter(mChatAdapter);
        mRvChat.setLayoutManager(new LinearLayoutManager(this));
    }

}