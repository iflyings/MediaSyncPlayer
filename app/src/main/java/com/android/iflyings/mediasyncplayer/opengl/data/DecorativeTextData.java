package com.android.iflyings.mediasyncplayer.opengl.data;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;

import androidx.annotation.NonNull;

import com.android.iflyings.mediasyncplayer.MyApplication;
import com.android.iflyings.mediasyncplayer.info.DecorativeTextInfo;
import com.android.iflyings.mediasyncplayer.opengl.BaseGLObject;
import com.android.iflyings.mediasyncplayer.opengl.MediaContext;

import java.io.IOException;


public class DecorativeTextData implements TextLoader.TextLoaderCallback {
    private final MediaContext mMediaContext;
    private final BaseGLObject.ImageGLObject mGLObject;
    private final DecorativeTextInfo mTextInfo;
    private final TextPaint mTextPaint;

    private TextLoader mTextLoader;

    private final Runnable mUpdateRunning = this::updateText;

    private int mScrollPos = 0;
    private boolean isMediaPlaying = false;

    private HandlerThread mUpdateThread;
    private Handler mUpdateHandler;

    public DecorativeTextData(MediaContext mediaContext, DecorativeTextInfo textInfo) {
        mMediaContext = mediaContext;
        mTextInfo = textInfo;
        mGLObject = new BaseGLObject.ImageGLObject();
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextInfo.getFontSize());
        mTextPaint.setColor(mTextInfo.getFontColor());
        if (mTextInfo.getTypeface() != null) {
            Typeface typeface = Typeface.createFromAsset(MyApplication.getAssetManager(),
                    "fonts/" + textInfo.getTypeface() + ".ttf");
            if (typeface != null) {
                mTextPaint.setTypeface(typeface);
            }
        }
    }

    private void updateText() {
        if (isMediaPlaying) {
            mScrollPos += mTextInfo.getUpdateStep();
            if (mTextInfo.getLocation() == DecorativeTextInfo.LOCAL_OF_TOP ||
                    mTextInfo.getLocation() == DecorativeTextInfo.LOCAL_OF_BOTTOM) {
                mTextLoader.updateText(-mScrollPos, 0);
                if (mScrollPos >= mTextLoader.getWidth()) {
                    mScrollPos = -mTextInfo.getBlockWidth();
                }
            } else {
                mTextLoader.updateText(0, -mScrollPos);
                if (mScrollPos >= mTextLoader.getHeight()) {
                    mScrollPos = -mTextInfo.getBlockHeight();
                }
            }

            mUpdateHandler.postDelayed(mUpdateRunning, mTextInfo.getUpdateDelay());
        }
    }

    public int drawMedia(int index) {
        if (isMediaPlaying) {
            return mGLObject.draw(index);
        }
        return index;
    }

    public void create() throws IOException {
        mUpdateThread = new HandlerThread("TextData");
        mUpdateThread.start();
        mUpdateHandler = new Handler(mUpdateThread.getLooper());

        int fontSize = mTextPaint.getFontMetricsInt(null);
        float[] tmpPVMMatrix = new float[16];
        Matrix.setIdentityM(tmpPVMMatrix, 0);

        if (mTextInfo.getLocation() == DecorativeTextInfo.LOCAL_OF_TOP ||
                mTextInfo.getLocation() == DecorativeTextInfo.LOCAL_OF_BOTTOM) {
            mTextLoader = new TextLoader(mGLObject, this, mTextInfo.getContent(), mTextPaint,
                    TextLoader.TEXT_MODE_MATCH);
            mTextLoader.loadMedia(mTextInfo.getBlockWidth(), fontSize);
            mScrollPos = -mTextInfo.getBlockWidth();
            float ratio = (float) fontSize / mTextInfo.getBlockHeight();
            if (mTextInfo.getLocation() == DecorativeTextInfo.LOCAL_OF_TOP) {
                Matrix.translateM(tmpPVMMatrix, 0, 0f, 1f - ratio, 0f);
            } else {
                Matrix.translateM(tmpPVMMatrix, 0, 0f, ratio - 1f, 0f);
            }
            Matrix.scaleM(tmpPVMMatrix, 0, 1f, ratio, 1f);
        } else {
            mTextLoader = new TextLoader(mGLObject, this, mTextInfo.getContent(), mTextPaint,
                    TextLoader.TEXT_MODE_WRAP);
            mTextLoader.loadMedia(fontSize, mTextInfo.getBlockHeight());
            mScrollPos = -mTextInfo.getBlockHeight();
            float ratio = (float) fontSize / mTextInfo.getBlockWidth();
            if (mTextInfo.getLocation() == DecorativeTextInfo.LOCAL_OF_LEFT) {
                Matrix.translateM(tmpPVMMatrix, 0, ratio - 1f, 0f, 0f);
            } else {
                Matrix.translateM(tmpPVMMatrix, 0, 1f - ratio, 0f, 0f);
            }
            Matrix.scaleM(tmpPVMMatrix, 0, ratio, 1f, 1f);
        }
        mGLObject.setMVPMatrixData(tmpPVMMatrix);

        isMediaPlaying = true;
        mUpdateHandler.postDelayed(mUpdateRunning, mTextInfo.getUpdateDelay());
    }

    public void destroy() {
        isMediaPlaying = false;
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

    @NonNull
    @Override
    public String toString() {
        return "[DecorativeTextData]:" + "ScrollPos = " + mScrollPos;
    }

    @Override
    public void runInGLThread(Runnable runnable) {
        mMediaContext.runInGLThread(runnable);
    }
}
