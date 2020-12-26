package com.android.iflyings.mediasyncplayer.info;

import org.json.JSONException;
import org.json.JSONObject;

public class MaskInfo extends MediaInfo {
    private String mVideoPath;
    private String mMaskPath;
    private boolean isHardwareCodec;

    public String getVideoPath() {
        return mVideoPath;
    }
    public String getMaskPath() {
        return mMaskPath;
    }
    public boolean getHardwareCodec() {
        return isHardwareCodec;
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = super.toJSONObject();
        jsonObject.put("path", mVideoPath);
        jsonObject.put("mask", mMaskPath);
        return jsonObject;
    }

    static MaskInfo from(JSONObject jsonObject) throws JSONException {
        MaskInfo maskInfo = new MaskInfo();
        maskInfo.mVideoPath = jsonObject.getString("path");
        maskInfo.mMaskPath = jsonObject.getString("mask");
        maskInfo.isHardwareCodec = jsonObject.getBoolean("codec");
        return maskInfo;
    }
}