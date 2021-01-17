package com.mua.chirkut.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.mua.chirkut.converter.DateConverter;

import java.sql.Date;

@Entity(tableName = "message")
@TypeConverters(DateConverter.class)
public class Message {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "message_id")
    private Long chatId;

    @ColumnInfo(name = "message_address")
    private String address;

    @ColumnInfo(name = "message_message")
    private String message;

    @ColumnInfo(name = "message_date")
    private Date date;

    @ColumnInfo(name = "message_incoming")
    private Boolean incoming;

    public Message(String address, String message, Date date, Boolean incoming) {
        this.address = address;
        this.message = message;
        this.date = date;
        this.incoming = incoming;
    }

    @Ignore
    public Message(String address, String message, Boolean incoming) {
        this(address, message, new Date(new java.util.Date().getTime()), incoming);
    }


    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getIncoming() {
        return incoming;
    }

    public void setIncoming(Boolean incoming) {
        this.incoming = incoming;
    }
}