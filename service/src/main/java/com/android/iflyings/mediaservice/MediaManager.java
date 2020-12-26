package com.android.iflyings.mediaservice;

import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


public class MediaManager extends IPlayerService.Stub {
    private final static String TAG = "MediaManager";

    private IPlayerCallback mCallback;

    @Override
    public void register(IPlayerCallback cb) throws RemoteException {
        Log.i(TAG, "MediaManager->register 1");
        mCallback = cb;
        try {
            JSONObject jsonObject = ProgrammeUtils.createProgrammeObject();
            mCallback.playProgrammeInfo(jsonObject.toString());
            Log.i(TAG, "MediaManager->register 2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregister(IPlayerCallback cb) throws RemoteException {
        Log.i(TAG, "MediaManager->unregister");
        mCallback = null;
    }

    @Override
    public String getProgrammeInfo() throws RemoteException {
        Log.i(TAG, "MediaManager->getProgrammeInfo");
        return null;
    }

    public String dumpPlayer() throws RemoteException {
        Log.i(TAG, "MediaManager->dumpPlayer");
        if (mCallback != null) {
            return mCallback.dumpPlayer();
        } else {
            return "No Callback";
        }
    }
}
