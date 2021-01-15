package com.mua.chirkut.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.mua.chirkut.R;
import com.mua.chirkut.databinding.ActivityChatBinding;
import com.mua.chirkut.viewmodel.ChatViewModel;

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

    }
}