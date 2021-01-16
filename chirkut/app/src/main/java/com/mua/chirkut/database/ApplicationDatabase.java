package com.mua.chirkut.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mua.chirkut.dao.MessageDao;
import com.mua.chirkut.entity.Message;

@Database(entities = {Message.class}, version = 1)
public abstract class ApplicationDatabase extends RoomDatabase {
    private static volatile ApplicationDatabase INSTANCE;

    public abstract MessageDao chatDao();

    public static ApplicationDatabase getInstance(Context context) {
        if(INSTANCE == null) {
            synchronized(ApplicationDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ApplicationDatabase.class, "chirkut.db").build();
                }
            }
        }
        return INSTANCE;
    }
}