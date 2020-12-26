package com.android.iflyings.mediasyncplayer.opengl.data;

import android.text.StaticLayout;

import androidx.annotation.NonNull;

import com.android.iflyings.mediasyncplayer.info.TextInfo;
import com.android.iflyings.mediasyncplayer.opengl.BaseGLObject;
import com.android.iflyings.mediasyncplayer.opengl.MediaContext;

import java.io.IOException;

public class TextData extends MediaData {

    private final BaseGLObject.ImageGLObject mGLObject;
    private final TextInfo mTextInfo;

    private final Runnable mCompleteRunning = this::notifyMediaCompletion;

    TextData(MediaContext mediaContext, TextInfo textInfo) {
        super(mediaContext, textInfo);
        mTextInfo = textInfo;
        mGLObject = new BaseGLObject.ImageGLObject();
    }

    @Override
    protected void onLoadMedia() throws IOException {

    }

    @Override
    protected void onStartMedia() {

    }

    @Override
    protected int onDrawMedia(int index) {
        return 0;
    }

    @Override
    protected void onStopMedia() {

    }

    @Override
    protected void onUnloadMedia() {

    }

    @Override
    protected BaseGLObject getGLObject() {
        return mGLObject;
    }

    @NonNull
    @Override
    public String toString() {
        return "[TextData]:" + mTextInfo.getFilePath() + "," + "DelayMs = " + mTextInfo.getUpdateDelay() + "," + super.toString();
    }
}
