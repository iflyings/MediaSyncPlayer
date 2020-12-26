package com.android.iflyings.mediasyncplayer.opengl.component;

import com.android.iflyings.mediasyncplayer.impl.SyncPlayerImpl;

import org.json.JSONException;
import org.json.JSONObject;

public class SyncComponent extends Component implements SyncPlayerImpl {

    private final String mMediaToken;
    private long mStartTickUs = 0L;

    public SyncComponent(String token) {
        mMediaToken = token;
    }

    public String getTokenName() {
        return mMediaToken;
    }

    @Override
    public JSONObject getStartInfo() throws JSONException {
        JSONObject wo = new JSONObject();
        wo.put("token", mMediaToken);
        wo.put("start", mStartTickUs);
        return wo;
    }

    @Override
    public int setStartInfo(JSONObject jsonObject) throws JSONException {
        String name = jsonObject.getString("name");
        return 0;
    }

    @Override
    public JSONObject getUpdateInfo() throws JSONException {
        return null;
    }

    @Override
    public int setUpdateInfo(JSONObject jsonObject) throws JSONException {
        return 0;
    }
}
