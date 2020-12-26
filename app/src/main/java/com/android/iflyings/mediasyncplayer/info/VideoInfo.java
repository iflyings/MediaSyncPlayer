package com.android.iflyings.mediasyncplayer.info;

import org.json.JSONException;
import org.json.JSONObject;


public class VideoInfo extends MediaInfo {
    private String mVideoPath;
    private boolean isHardwareCodec;

    public String getVideoPath() {
        return mVideoPath;
    }
    public boolean getHardwareCodec() {
        return isHardwareCodec;
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = super.toJSONObject();
        jsonObject.put("path", mVideoPath);
        return jsonObject;
    }

    static VideoInfo from(JSONObject jsonObject) throws JSONException {
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.mVideoPath = jsonObject.getString("path");
        videoInfo.isHardwareCodec = jsonObject.has("codec") && jsonObject.getBoolean("codec");
        return videoInfo;
    }
}
