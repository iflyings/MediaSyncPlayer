package com.android.iflyings.mediaservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.PrintWriter;


public class MediaService extends Service {
    private final static String TAG = "MediaService";
    private final MediaManager mMediaManager = new MediaManager();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "MediaService::onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "MediaService::onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "MediaService::onBind");
        return mMediaManager;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "MediaService::onUnbind");
        return super.onUnbind(intent);
    }
    // am startservice -a com.android.iflyings.action.mediaservice
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {

        }
        return super.onStartCommand(intent, flags, startId);
    }

    // dumpsys activity service MediaService
    @Override
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        String info = null;
        try {
            info = mMediaManager.dumpPlayer();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        writer.println("===================== Media Service =====================");
        if (info != null) {
            writer.printf(info);
        }
        writer.println("=========================================================");
    }

}