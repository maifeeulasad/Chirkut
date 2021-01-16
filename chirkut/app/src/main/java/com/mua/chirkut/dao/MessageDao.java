package com.mua.chirkut.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mua.chirkut.entity.Message;

import java.util.List;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM Message WHERE message_address=:address")
    LiveData<List<Message>> getAll(String address);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Message message);

}
