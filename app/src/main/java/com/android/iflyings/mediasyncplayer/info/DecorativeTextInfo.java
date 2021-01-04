package com.android.iflyings.mediasyncplayer.info;

import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

public class DecorativeTextInfo {
    public final static int LOCAL_OF_BOTTOM = 0;
    public final static int LOCAL_OF_TOP = 1;
    public final static int LOCAL_OF_LEFT = 2;
    public final static int LOCAL_OF_RIGHT = 3;

    private int mBlockWidth;
    private int mBlockHeight;
    private String mContent;
    private int mFontColor;
    private float mFontSize;
    private long mUpdateDelay;
    private int mUpdateStep;
    private int mLocation;
    private String mTypeface;

    public int getBlockWidth() {
        return mBlockWidth;
    }
    public int getBlockHeight() {
        return mBlockHeight;
    }
    public String getContent() {
        return mContent;
    }
    public int getFontColor() {
        return mFontColor;
    }
    public float getFontSize() {
        return mFontSize;
    }
    public long getUpdateDelay() {
        return mUpdateDelay;
    }
    public int getUpdateStep() {
        return mUpdateStep;
    }
    public int getLocation() {
        return mLocation;
    }
    public String getTypeface() {
        return mTypeface;
    }

    public static DecorativeTextInfo from(BlockInfo blockInfo, JSONObject jsonObject) throws JSONException {
        DecorativeTextInfo textInfo = new DecorativeTextInfo();
        textInfo.mBlockWidth = blockInfo.getBlockWidth();
        textInfo.mBlockHeight = blockInfo.getBlockHeight();
        textInfo.mContent = jsonObject.getString("content").replace("[\n\r\t]", "");
        if (jsonObject.has("fontColor")) {
            textInfo.mFontColor = Color.parseColor(jsonObject.getString("fontColor"));
        } else {
            textInfo.mFontColor = Color.RED;
        }
        if (jsonObject.has("fontSize")) {
            textInfo.mFontSize = (float) jsonObject.getInt("fontSize");
        } else {
            textInfo.mFontSize = 32f;
        }
        if (jsonObject.has("delay")) {
            textInfo.mUpdateDelay = jsonObject.getLong("delay");
        } else {
            textInfo.mUpdateDelay = 200;
        }
        if (jsonObject.has("step")) {
            textInfo.mUpdateStep = jsonObject.getInt("step");
        } else {
            textInfo.mUpdateStep = 10;
        }
        if (jsonObject.has("location")) {
            switch (jsonObject.getString("location")) {
                case "left": {
                    textInfo.mLocation = LOCAL_OF_LEFT;
                    break;
                }
                case "right": {
                    textInfo.mLocation = LOCAL_OF_RIGHT;
                    break;
                }
                case "top": {
                    textInfo.mLocation = LOCAL_OF_TOP;
                    break;
                }
                default: {
                    textInfo.mLocation = LOCAL_OF_BOTTOM;
                    break;
                }
            }
        }
        if (jsonObject.has("typeface")) {
            textInfo.mTypeface = jsonObject.getString("typeface");
        }
        return textInfo;
    }
}
