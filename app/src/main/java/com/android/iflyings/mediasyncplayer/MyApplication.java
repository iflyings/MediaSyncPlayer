package com.android.iflyings.mediasyncplayer;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;


public class MyApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context instance;
    public static Resources getApplicationResources() {
        return instance.getResources();
    }
    public static AssetManager getAssetManager() {
        return instance.getAssets();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler.getInstance().init(this);
        instance = getApplicationContext();
    }

}
