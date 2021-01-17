package com.mua.chirkut.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mua.chirkut.entity.Message;
import com.mua.chirkut.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final MessageRepository messageRepository;
    public LiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());

    public ChatViewModel(@NonNull Application application) {
        super(application);
        messageRepository = new MessageRepository(application);
        //todo:fix hardcoded
        messages = messageRepository.getAllByAddress("192.168.0.108");
    }

    public void insert(String address,String message){
        messageRepository.insert(new Message(address,message,false));
    }

}
