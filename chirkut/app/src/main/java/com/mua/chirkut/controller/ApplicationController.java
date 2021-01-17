package com.mua.chirkut.controller;

import android.app.Application;

import com.mua.chirkut.service.MessagingService;

public class ApplicationController extends Application {

    private static ApplicationController mInstance;
    private MessagingService mService;
    public static ApplicationController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    public void startService(){


    }
    public void stopService(){

    }
    public MessagingService getService(){
        return mService;
    }
}