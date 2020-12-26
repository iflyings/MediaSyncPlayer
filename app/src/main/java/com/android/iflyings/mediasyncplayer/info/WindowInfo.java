package com.android.iflyings.mediasyncplayer.info;

import android.graphics.Rect;

import androidx.annotation.IntDef;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;


public class WindowInfo {

    public static final int WINDOW_TYPE_SYNC = 1;
    public static final int WINDOW_TYPE_SOURCE = 2;
    public static final int WINDOW_TYPE_MULTI = 3;

    @IntDef({WINDOW_TYPE_SYNC, WINDOW_TYPE_SOURCE, WINDOW_TYPE_MULTI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface WindowType {}

    private ArrayList<BlockInfo> mBlockList;
    @WindowType private int mWindowType;
    private String mWindowName;
    private int mWindowLeft;
    private int mWindowTop;
    private int mWindowWidth;
    private int mWindowHeight;

    private WindowInfo() { }

    public @WindowType int getWindowType() {
        return mWindowType;
    }
    public String getWindowName() {
        return mWindowName;
    }
    public int getWindowLeft() {
        return mWindowLeft;
    }
    public int getWindowTop() {
        return mWindowTop;
    }
    public int getWindowWidth() {
        return mWindowWidth;
    }
    public int getWindowHeight() {
        return mWindowHeight;
    }
    public Rect getWindowRect() {
        return new Rect(mWindowLeft, mWindowTop, mWindowLeft + mWindowWidth, mWindowTop + mWindowHeight);
    }

    public List<BlockInfo> getBlockList() {
        return mBlockList;
    }
    public BlockInfo getBlockInfo(int index) {
        return mBlockList.get(index);
    }
    public int getSize() {
        return mBlockList.size();
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", mWindowName);
        if (mWindowType == WINDOW_TYPE_SYNC) {
            jsonObject.put("type", "sync");
        } else if (mWindowType == WINDOW_TYPE_SOURCE) {
            jsonObject.put("type", "source");
        } else if (mWindowType == WINDOW_TYPE_MULTI) {
            jsonObject.put("type", "multi");
        }
        jsonObject.put("left", mWindowLeft);
        jsonObject.put("top", mWindowTop);
        jsonObject.put("width", mWindowWidth);
        jsonObject.put("height", mWindowHeight);

        JSONArray list = new JSONArray();
        for (int i = 0;i < mBlockList.size();i ++) {
            list.put(mBlockList.get(i).toJSONObject());
        }
        jsonObject.put("list", list);
        return jsonObject;
    }

    public static WindowInfo from(JSONObject jsonObject) throws JSONException {
        WindowInfo wi = new WindowInfo();
        wi.mWindowName = jsonObject.getString("name");
        switch (jsonObject.getString("type")) {
            case "sync":
                wi.mWindowType = WINDOW_TYPE_SYNC;
                break;
            case "source":
                wi.mWindowType = WINDOW_TYPE_SOURCE;
                break;
            case "multi":
                wi.mWindowType = WINDOW_TYPE_MULTI;
                break;
            default:
                throw new IllegalStateException("unknown window type->" + jsonObject.getString("type"));
        }
        wi.mWindowLeft = jsonObject.getInt("left");
        wi.mWindowTop = jsonObject.getInt("top");
        wi.mWindowWidth = jsonObject.getInt("width");
        wi.mWindowHeight = jsonObject.getInt("height");
        JSONArray list = jsonObject.getJSONArray("list");
        wi.mBlockList = new ArrayList<>(list.length());
        for (int i = 0; i < list.length(); i++) {
            BlockInfo bi = BlockInfo.from(list.getJSONObject(i), wi.mWindowWidth, wi.mWindowHeight);
            wi.mBlockList.add(bi);
        }
        return wi;
    }
}
