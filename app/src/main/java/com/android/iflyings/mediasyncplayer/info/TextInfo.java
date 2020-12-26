package com.android.iflyings.mediasyncplayer.info;

import org.json.JSONException;
import org.json.JSONObject;

public class TextInfo extends MediaInfo {

    private String mFilePath;
    private int mUpdateDelay = 0;

    private TextInfo() {}

    public String getFilePath() {
        return mFilePath;
    }
    public int getUpdateDelay() {
        return mUpdateDelay;
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = super.toJSONObject();
        jsonObject.put("delay", mUpdateDelay);
        jsonObject.put("path", mFilePath);
        return jsonObject;
    }

    static TextInfo from(JSONObject jsonObject) throws JSONException {
        TextInfo textInfo = new TextInfo();
        textInfo.mFilePath = jsonObject.getString("path");
        if (jsonObject.has("delay")) {
            textInfo.mUpdateDelay = jsonObject.getInt("delay");
        } else {
            textInfo.mUpdateDelay = 500;
        }
        return textInfo;
    }

}
