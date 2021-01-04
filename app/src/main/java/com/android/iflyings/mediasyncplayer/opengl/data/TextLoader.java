package com.android.iflyings.mediasyncplayer.opengl.data;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.opengl.Matrix;
import android.text.BoringLayout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.android.iflyings.mediasyncplayer.Constant;

import java.io.IOException;

public class TextLoader {
    public final static int TEXT_MODE_MATCH = -1;
    public final static int TEXT_MODE_WRAP = -2;

    private final String mTextSource;
    private final TextPaint mTextPaint;
    private final TextGLObjectImpl mGLObjectImpl;
    private final TextLoaderCallback mLoaderCallback;

    private Bitmap mTextBitmap = null;
    private StaticLayout mStaticLayout = null;

    private int mTextWidth;
    private int mTextHeight;

    TextLoader(TextGLObjectImpl impl, TextLoaderCallback cb, String source, TextPaint paint, int width) {
        mGLObjectImpl = impl;
        mLoaderCallback = cb;
        mTextSource = source;
        mTextPaint = paint;

        if (width == TEXT_MODE_MATCH) {
            BoringLayout.Metrics metrics = BoringLayout.isBoring(source, mTextPaint);
            mTextWidth = metrics.width;
        } else if (width == TEXT_MODE_WRAP) {
            mTextWidth = mTextPaint.getFontMetricsInt(null);
        } else {
            mTextWidth = width;
        }
    }

    public int getWidth() {
        return mTextWidth;
    }

    public int getHeight() {
        return mTextHeight;
    }

    public void loadMedia(int width, int height) throws IOException {
        mStaticLayout = StaticLayout.Builder.obtain(mTextSource, 0, mTextSource.length(),
                mTextPaint, mTextWidth).build();
        mTextHeight = mStaticLayout.getHeight();

        mTextBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (mTextBitmap == null) {
            throw new IOException("can not create image!!!");
        }
        final Object lock = new Object();
        mLoaderCallback.runInGLThread(() -> {
            mGLObjectImpl.create();
            mGLObjectImpl.loadBitmap(mTextBitmap);
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

    public void unloadMedia() {
        mLoaderCallback.runInGLThread(mGLObjectImpl::destroy);
        if (mTextBitmap != null) {
            mTextBitmap.recycle();
            mTextBitmap = null;
        }
    }

    public void updateText(int offsetX, int offsetY) {
        Canvas canvas = new Canvas(mTextBitmap);
        mTextBitmap.eraseColor(Color.TRANSPARENT);
        canvas.translate(offsetX, offsetY);
        mStaticLayout.draw(canvas);

        mLoaderCallback.runInGLThread(() -> mGLObjectImpl.updateBitmap(mTextBitmap));
    }

    public interface TextGLObjectImpl {

        void create();

        void destroy();

        void loadBitmap(Bitmap bitmap);

        void updateBitmap(Bitmap bitmap);

    }

    public interface TextLoaderCallback {

        void runInGLThread(Runnable runnable);

    }
}
