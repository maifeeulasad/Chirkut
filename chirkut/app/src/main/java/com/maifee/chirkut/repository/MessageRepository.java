package com.mua.chirkut.repository;


import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.mua.chirkut.dao.MessageDao;
import com.mua.chirkut.database.ApplicationDatabase;
import com.mua.chirkut.entity.Message;

import java.util.List;

public class MessageRepository {
    private MessageDao messageDao;

    public MessageRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getInstance(application);
        messageDao = db.chatDao();
    }

    public LiveData<List<Message>> getAllByAddress(String address){
        return messageDao.getAll(address);
    }

    public void insert(Message message) {
        new InsertAsyncTask(messageDao).execute(message);
    }

    private static class InsertAsyncTask extends AsyncTask<Message, Void, Void> {

        private MessageDao messageDao;

        InsertAsyncTask(MessageDao messageDao) {
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(final Message... params) {
            messageDao.insert(params[0]);
            return null;
        }
    }
}