package com.android.iflyings.mediasyncplayer.info;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class BlockInfo {

    private String mBlockName;
    private int mBlockLeft;
    private int mBlockTop;
    private int mBlockWidth;
    private int mBlockHeight;
    private int mWindowWidth;
    private int mWindowHeight;
    private List<MediaInfo> mMediaList;

    public String getBlockName() {
        return mBlockName;
    }
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
    public List<MediaInfo> getMediaInfoList() {
        return mMediaList;
    }
    public MediaInfo getMediaInfo(int index) {
        return mMediaList.get(index);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", mBlockName);
        jsonObject.put("left", mBlockLeft);
        jsonObject.put("top", mBlockTop);
        jsonObject.put("width", mBlockWidth);
        jsonObject.put("height", mBlockHeight);

        JSONArray jsonArray = new JSONArray();
        for (MediaInfo mediaInfo : mMediaList) {
            jsonArray.put(mediaInfo.toJSONObject());
        }
        jsonObject.put("list", jsonArray);
        return jsonObject;
    }

    static BlockInfo from(JSONObject jsonObject, int windowWidth, int windowHeight) throws JSONException {
        BlockInfo bi = new BlockInfo();
        bi.mWindowWidth = windowWidth;
        bi.mWindowHeight = windowHeight;
        bi.mBlockName = jsonObject.getString("name");
        bi.mBlockLeft = jsonObject.getInt("left");
        bi.mBlockTop = jsonObject.getInt("top");
        bi.mBlockWidth = jsonObject.getInt("width");
        bi.mBlockHeight = jsonObject.getInt("height");
        JSONArray list = jsonObject.getJSONArray("list");
        bi.mMediaList = new ArrayList<>(list.length());
        for (int i = 0; i < list.length(); i++) {
            MediaInfo mi = MediaInfo.from(list.getJSONObject(i), bi);
            bi.mMediaList.add(mi);
        }
        return bi;
    }
}
