package com.android.iflyings.mediasyncplayer.info;

import org.json.JSONException;
import org.json.JSONObject;


public abstract class MediaInfo {

    private int mBlockLeft;
    private int mBlockTop;
    private int mBlockWidth;
    private int mBlockHeight;
    private int mWindowWidth;
    private int mWindowHeight;
    private String mMediaToken;
    private String mMediaType;
    private int mAnimationType;


    public int getBlockLeft() {
        return mBlockLeft;
    }
    public int getBlockTop() {
        return mBlockTop;
    }
    public int getBlockWidth() {
        return mBlockWidth;
    }
    public int getBlockHeight() {
        return mBlockHeight;
    }
    public int getWindowWidth() {
        return mWindowWidth;
    }
    public int getWindowHeight() {
        return mWindowHeight;
    }
    public String getMediaToken() {
        return mMediaToken;
    }
    public int getAnimationType() {
        return mAnimationType;
    }

    protected MediaInfo() {}

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", mMediaType);
        jsonObject.put("token", mMediaToken);
        return jsonObject;
    }

    public static MediaInfo from(JSONObject jsonObject, BlockInfo blockInfo) throws JSONException {
        MediaInfo mediaInfo;
        String type = jsonObject.getString("type");
        switch (type) {
            case "video":
                mediaInfo = VideoInfo.from(jsonObject);
                break;
            case "image":
                mediaInfo = ImageInfo.from(jsonObject);
                break;
            case "mask":
                mediaInfo = MaskInfo.from(jsonObject);
                break;
            case "source":
                mediaInfo = SourceInfo.from(jsonObject);
                break;
            case "text":
                mediaInfo = TextInfo.from(jsonObject);
                break;
            default:
                throw new IllegalArgumentException("type:" + type + " is unsupported");
        }
        mediaInfo.mMediaType = type;
        mediaInfo.mBlockLeft = blockInfo.getBlockLeft();
        mediaInfo.mBlockTop = blockInfo.getBlockTop();
        mediaInfo.mBlockWidth = blockInfo.getBlockWidth();
        mediaInfo.mBlockHeight = blockInfo.getBlockHeight();
        mediaInfo.mWindowWidth = blockInfo.getWindowWidth();
        mediaInfo.mWindowHeight = blockInfo.getWindowHeight();
        mediaInfo.mMediaToken = jsonObject.has("token") ? jsonObject.getString("token") : null;
        mediaInfo.mAnimationType = jsonObject.has("animation") ? jsonObject.getInt("animation") : 0;
        return mediaInfo;
    }

    public static MediaInfo createNoMedia(int blockWidth, int blockHeight) {
        MediaInfo mediaInfo = new NoMediaInfo();
        mediaInfo.mBlockWidth = blockWidth;
        mediaInfo.mBlockHeight = blockHeight;
        return mediaInfo;
    }
}
