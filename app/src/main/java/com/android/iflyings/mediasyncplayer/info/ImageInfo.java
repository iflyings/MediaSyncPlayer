package com.android.iflyings.mediasyncplayer.info;

import org.json.JSONException;
import org.json.JSONObject;


public class ImageInfo extends MediaInfo {
    private String mImagePath;
    private String mAssertName;
    private long mDuringMs;

    public String getImagePath() {
        return mImagePath;
    }
    public String getAssertName() {
        return mAssertName;
    }
    public long getDuringMs() {
        return mDuringMs;
    }

    private ImageInfo() { }

    ImageInfo(String path, String name, long duringMs) {
        mImagePath = path;
        mAssertName = name;
        mDuringMs = duringMs;
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = super.toJSONObject();
        jsonObject.put("during", mDuringMs);
        jsonObject.put("path", mImagePath);
        return jsonObject;
    }

    static ImageInfo from(JSONObject jsonObject) throws JSONException {
        long during = 5000;
        if (jsonObject.has("during")) {
            during = jsonObject.getLong("during");
        }
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.mAssertName = null;
        imageInfo.mDuringMs = during;
        imageInfo.mImagePath = jsonObject.getString("path");
        return imageInfo;
    }
}
