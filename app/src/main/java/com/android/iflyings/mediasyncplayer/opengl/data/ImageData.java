package com.android.iflyings.mediasyncplayer.opengl.data;

import androidx.annotation.NonNull;

import com.android.iflyings.mediasyncplayer.opengl.BaseGLObject;
import com.android.iflyings.mediasyncplayer.opengl.MediaContext;
import com.android.iflyings.mediasyncplayer.info.ImageInfo;

import java.io.IOException;


public class ImageData extends MediaData {

    private final BaseGLObject.ImageGLObject mGLObject;
    private final ImageInfo mImageInfo;
    private final ImageLoader mImageLoader;

    private final Runnable mCompleteRunning = this::notifyMediaCompletion;

    ImageData(MediaContext mediaContext, ImageInfo imageInfo) {
        super(mediaContext, imageInfo);
        mImageInfo = imageInfo;
        mGLObject = new BaseGLObject.ImageGLObject();
        mImageLoader = new ImageLoader(mGLObject, this, imageInfo.getImagePath(), imageInfo.getAssertName());
    }

    @Override
    protected BaseGLObject getGLObject() {
        return mGLObject;
    }

    @Override
    protected void onLoadMedia() throws IOException {
        mImageLoader.loadMedia(mImageInfo.getBlockWidth(), mImageInfo.getBlockHeight());
    }

    @Override
    protected void onStartMedia() {
        if (mImageInfo.getDuringMs() >= 0) {
            runInUserThreadDelay(mCompleteRunning, mImageInfo.getDuringMs());
        }
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
        mImageLoader.onUnloadMedia();
    }

    @NonNull
    @Override
    public String toString() {
        return "[ImageData]:" + mImageInfo.getImagePath() + "," + "DuringMs = " + mImageInfo.getDuringMs() + "," + super.toString();
    }
}
