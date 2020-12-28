package com.android.iflyings.mediasyncplayer.info;

import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

public class TextInfo extends MediaInfo {

    private String mFilePath;
    private int mFontSize;
    private int mFontColor;
    private int mDelay = 0;
    private int mStep = 0;

    private TextInfo() {}

    public String getFilePath() {
        return mFilePath;
    }
    public int getFontSize() {
        return mFontSize;
    }
    public int getFontColor() {
        return mFontColor;
    }
    public int getDelay() {
        return mDelay;
    }
    public int getStep() {
        return mStep;
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = super.toJSONObject();
        jsonObject.put("delay", mDelay);
        jsonObject.put("path", mFilePath);
        return jsonObject;
    }

    static TextInfo from(JSONObject jsonObject) throws JSONException {
        TextInfo textInfo = new TextInfo();
        textInfo.mFilePath = jsonObject.getString("path");
        if (jsonObject.has("delay")) {
            textInfo.mDelay = jsonObject.getInt("delay");
        } else {
            textInfo.mDelay = 500;
        }
        if (jsonObject.has("step")) {
            textInfo.mStep = jsonObject.getInt("step");
        } else {
            textInfo.mStep = 500;
        }
        if (jsonObject.has("fontSize")) {
            textInfo.mFontSize = jsonObject.getInt("fontSize");
        } else {
            textInfo.mFontSize = 35;
        }
        if (jsonObject.has("fontColor")) {
            textInfo.mFontColor = Color.parseColor(jsonObject.getString("fontColor"));
        } else {
            textInfo.mFontColor = Color.RED;
        }
        return textInfo;
    }

}
