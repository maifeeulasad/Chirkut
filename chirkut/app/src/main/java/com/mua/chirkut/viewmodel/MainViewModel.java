package com.mua.chirkut.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.mua.chirkut.entity.Message;
import com.mua.chirkut.repository.MessageRepository;

public class MainViewModel extends AndroidViewModel {

    public MutableLiveData<String> p2pStatus = new MutableLiveData<>("");
    private final MessageRepository messageRepository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.messageRepository = new MessageRepository(application);
    }

    public void insertChat(String address,String message, Boolean incoming){
        messageRepository.insert(new Message(address,message,incoming));
    }
}
