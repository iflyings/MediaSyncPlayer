package com.android.iflyings.mediasyncplayer.opengl.data;

import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;

import androidx.annotation.NonNull;

import com.android.iflyings.mediasyncplayer.util.FileUtils;
import com.android.iflyings.mediasyncplayer.info.TextInfo;
import com.android.iflyings.mediasyncplayer.opengl.BaseGLObject;
import com.android.iflyings.mediasyncplayer.opengl.MediaContext;

import java.io.IOException;

public class TextData extends MediaData implements TextLoader.TextLoaderCallback {
    private final BaseGLObject.ImageGLObject mGLObject;
    private final TextInfo mTextInfo;
    private final TextPaint mTextPaint;

    private final Runnable mUpdateRunning = this::updateText;

    private TextLoader mTextLoader;

    private int mMaxScroll = 0;
    private int mScrollPos = 0;

    private HandlerThread mUpdateThread;
    private Handler mUpdateHandler;

    TextData(MediaContext mediaContext, TextInfo textInfo) {
        super(mediaContext, textInfo);
        mTextInfo = textInfo;
        mGLObject = new BaseGLObject.ImageGLObject();
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextInfo.getFontSize());
        mTextPaint.setColor(mTextInfo.getFontColor());
    }

    private void updateText() {
        mScrollPos += mTextInfo.getStep();
        mTextLoader.updateText(0, -mScrollPos);
        if (mScrollPos >= mMaxScroll) {
            notifyMediaCompletion();
            return;
        }

        mUpdateHandler.postDelayed(mUpdateRunning, mTextInfo.getDelay());
    }

    @Override
    protected void onLoadMedia() throws IOException {
        if (mTextLoader == null) {
            String text = FileUtils.readStrFromFile(mTextInfo.getFilePath());
            mTextLoader = new TextLoader(mGLObject, this, text, mTextPaint, mTextInfo.getBlockWidth());
            mTextLoader.loadMedia(mTextInfo.getBlockWidth(), mTextInfo.getBlockHeight());
            notifyMediaSize(mTextLoader.getWidth(), mTextLoader.getHeight());

            mUpdateThread = new HandlerThread("TextData");
            mUpdateThread.start();
            mUpdateHandler = new Handler(mUpdateThread.getLooper());
        }
    }

    @Override
    protected void onStartMedia() {
        mScrollPos = -mTextInfo.getBlockHeight();
        mMaxScroll = mTextLoader.getHeight();

        mUpdateHandler.postDelayed(mUpdateRunning, mTextInfo.getDelay());
    }

    @Override
    protected int onDrawMedia(int index) {
        return mGLObject.draw(index);
    }

    @Override
    protected void onStopMedia() {

    }

    @Override
    protected void onUnloadMedia() {
        if (mUpdateThread != null) {
            mUpdateHandler.removeCallbacksAndMessages(null);
            mUpdateThread.quitSafely();
            mUpdateThread = null;
        }
        if (mTextLoader != null) {
            mTextLoader.unloadMedia();
            mTextLoader = null;
        }
    }

    @Override
    protected BaseGLObject getGLObject() {
        return mGLObject;
    }

    @NonNull
    @Override
    public String toString() {
        return "[TextData]:" + mTextInfo.getFilePath() + "," + "DelayMs = " + mTextInfo.getDelay() + "," + super.toString();
    }
}
