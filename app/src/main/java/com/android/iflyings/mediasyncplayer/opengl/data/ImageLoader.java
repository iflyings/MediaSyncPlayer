package com.android.iflyings.mediasyncplayer.opengl.data;

import android.graphics.Bitmap;

import com.android.iflyings.mediasyncplayer.Constant;
import com.android.iflyings.mediasyncplayer.MyApplication;
import com.android.iflyings.mediasyncplayer.util.ImageUtils;

import java.io.IOException;
import java.io.InputStream;


public class ImageLoader {

    private final String mImagePath;
    private final String mAssertName;

    private final ImageGLObjectImpl mGLObjectImpl;
    private final LoaderCallback mLoaderCallback;

    public ImageLoader(ImageGLObjectImpl impl, LoaderCallback cb, String imagePath, String assertName) {
        mGLObjectImpl = impl;
        mLoaderCallback = cb;
        mImagePath = imagePath;
        mAssertName = assertName;
    }

    public void loadMedia(int reqWidth, int reqHeight) throws IOException {
        final Object lock = new Object();
        Bitmap bitmap = null;
        if (mImagePath != null) {
            bitmap = ImageUtils.createBitmap(mImagePath, reqWidth, reqHeight);
        } else if (mAssertName != null) {
            InputStream inputStream = MyApplication.getApplicationResources().getAssets().open(mAssertName);
            bitmap = ImageUtils.createBitmap(inputStream, reqWidth, reqHeight);
            inputStream.close();
        }
        if (bitmap == null) {
            throw new IOException(mImagePath + " can not decode image!!!");
        }
        mLoaderCallback.notifyMediaSize(bitmap.getWidth(), bitmap.getHeight());
        final Bitmap finalBitmap = bitmap;
        mLoaderCallback.runInGLThread(() -> {
            mGLObjectImpl.create();
            mGLObjectImpl.loadBitmap(finalBitmap);
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
        bitmap.recycle();
    }

    public void unloadMedia() {
        mLoaderCallback.runInGLThread(mGLObjectImpl::destroy);
    }

    public interface ImageGLObjectImpl {

        void create();

        void destroy();

        void loadBitmap(Bitmap bitmap);

    }
}
