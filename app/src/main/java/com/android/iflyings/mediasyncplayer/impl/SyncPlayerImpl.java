package com.android.iflyings.mediasyncplayer.impl;

import org.json.JSONException;
import org.json.JSONObject;

public interface SyncPlayerImpl {

    JSONObject getStartInfo() throws JSONException;
    int setStartInfo(JSONObject jsonObject) throws JSONException;

    JSONObject getUpdateInfo() throws JSONException;
    int setUpdateInfo(JSONObject jsonObject) throws JSONException;

}