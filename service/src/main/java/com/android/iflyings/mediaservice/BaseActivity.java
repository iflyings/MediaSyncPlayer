package com.android.iflyings.mediaservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public abstract class BaseActivity extends AppCompatActivity  {

    private static final int REQUEST_EXTERNAL_STORAGE = 68;
    private static final String PERMISSIONS_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    private final IPlayerCallback.Stub mPlayerCallback = new IPlayerCallback.Stub() {
        @Override
        public String dumpPlayer() {
            return dumpPlayerInfo();
        }

        @Override
        public void playProgrammeInfo(String info) {
            playProgramme(info);
        }
    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIPlayerService = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIPlayerService = (IPlayerService) service;
            try {
                mIPlayerService.register(mPlayerCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private IPlayerService mIPlayerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        verifyStoragePermissions();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mIPlayerService != null) {
            try {
                mIPlayerService.unregister(mPlayerCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(mServiceConnection);
            mIPlayerService = null;
        }
        super.onDestroy();
    }

    private void verifyStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(this, PERMISSIONS_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {PERMISSIONS_STORAGE}, REQUEST_EXTERNAL_STORAGE);
        } else {
            initView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int grant : grantResults) {
            if (grant != PERMISSION_GRANTED) {
                return;
            }
        }
        initView();
    }

    private void initView() {
        Intent intent = new Intent(this, MediaService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    public abstract String dumpPlayerInfo();

    public abstract void playProgramme(String info);
}
