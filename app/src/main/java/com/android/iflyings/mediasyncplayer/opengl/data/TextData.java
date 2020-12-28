package com.android.iflyings.mediasyncplayer.opengl.data;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.NonNull;

import com.android.iflyings.mediasyncplayer.Constant;
import com.android.iflyings.mediasyncplayer.util.FileUtils;
import com.android.iflyings.mediasyncplayer.info.TextInfo;
import com.android.iflyings.mediasyncplayer.opengl.BaseGLObject;
import com.android.iflyings.mediasyncplayer.opengl.MediaContext;

import java.io.IOException;

public class TextData extends MediaData {
    private final BaseGLObject.ImageGLObject mGLObject;
    private final TextInfo mTextInfo;
    private final TextPaint mTextPaint;

    private final Runnable mUpdateRunning = this::updateText;

    private Bitmap mTextBitmap = null;
    private StaticLayout mStaticLayout = null;
    private int mTextHeight = 0;
    private int mScrollPos = 0;

    TextData(MediaContext mediaContext, TextInfo textInfo) {
        super(mediaContext, textInfo);
        mTextInfo = textInfo;
        mGLObject = new BaseGLObject.ImageGLObject();
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextInfo.getFontSize());
        mTextPaint.setColor(mTextInfo.getFontColor());
    }

    private void updateText() {
        Canvas canvas = new Canvas(mTextBitmap);
        mTextBitmap.eraseColor(Color.TRANSPARENT);
        canvas.translate(0, -mScrollPos);
        mStaticLayout.draw(canvas);

        mScrollPos += mTextInfo.getStep();
        runInGLThread(() -> mGLObject.updateBitmap(mTextBitmap));
        if (mScrollPos >= mTextHeight) {
            notifyMediaCompletion();
            return;
        }

        runInUserThreadDelay(mUpdateRunning, mTextInfo.getDelay());
    }

    @Override
    protected void onLoadMedia() throws IOException {
        if (mTextBitmap == null) {
            String text = FileUtils.readStrFromFile(mTextInfo.getFilePath());
            mStaticLayout = StaticLayout.Builder.obtain(text, 0, text.length(), mTextPaint,
                    mTextInfo.getBlockWidth()).build();
            mTextHeight = mStaticLayout.getHeight();

            mTextBitmap = Bitmap.createBitmap(mTextInfo.getBlockWidth(), mTextInfo.getBlockHeight(),
                    Bitmap.Config.RGB_565);
            notifyMediaSize(mTextBitmap.getWidth(), mTextBitmap.getHeight());

            final Object lock = new Object();
            runInGLThread(() -> {
                mGLObject.create();
                mGLObject.loadBitmap(mTextBitmap);
                synchronized (lock) {
                    lock.notify();
                }
            });
            synchronized (lock) {
                try {
                    lock.wait(Constant.MEDIA_LOAD_MAX_TIME_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        mScrollPos = -mTextInfo.getBlockHeight();
    }

    @Override
    protected void onStartMedia() {
        runInUserThreadDelay(mUpdateRunning, mTextInfo.getDelay());
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
        runInGLThread(mGLObject::destroy);
        if (mTextBitmap != null) {
            mTextBitmap.recycle();
            mTextBitmap = null;
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
