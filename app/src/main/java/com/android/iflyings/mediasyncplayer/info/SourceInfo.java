package com.android.iflyings.mediasyncplayer.info;

import org.json.JSONException;
import org.json.JSONObject;


public class SourceInfo extends MediaInfo {
    public static final int SOURCE_HDMI1 = 9;
    public static final int SOURCE_HDMI2 = 10;
    public static final int SOURCE_HDMI3 = 11;
    public static final int SOURCE_HDMI4 = 12;
    public static final int SOURCE_MEDIA = 13;

    private int mSourceType;

    private SourceInfo() {}

    public int getSourceType() {
        return mSourceType;
    }

    static SourceInfo from(JSONObject jsonObject) throws JSONException {
        SourceInfo si = new SourceInfo();
        if ("HDMI1".equals(jsonObject.getString("src"))) {
            si.mSourceType = SOURCE_HDMI1;
        } else if ("HDMI2".equals(jsonObject.getString("src"))) {
            si.mSourceType = SOURCE_HDMI2;
        } else if ("HDMI3".equals(jsonObject.getString("src"))) {
            si.mSourceType = SOURCE_HDMI3;
        } else if ("HDMI4".equals(jsonObject.getString("src"))) {
            si.mSourceType = SOURCE_HDMI4;
        } else {
            throw new JSONException("unkown source type " + jsonObject.getString("src"));
        }
        return si;
    }

}
