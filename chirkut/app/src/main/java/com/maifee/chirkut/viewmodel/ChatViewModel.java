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
    public MutableLiveData<String> address = new MutableLiveData<>("");

    public ChatViewModel(@NonNull Application application) {
        super(application);
        messageRepository = new MessageRepository(application);
        //todo:fix hardcoded
        messages = messageRepository.getAllByAddress(address.getValue());
    }

    public void updateAddress(String address){
        this.address.postValue(address);
        messages = messageRepository.getAllByAddress(address);
    }

    public void insert(String address,String message){
        messageRepository.insert(new Message(address,message,false));
    }

}
