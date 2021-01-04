package com.android.iflyings.mediasyncplayer.opengl.data;

import androidx.annotation.NonNull;

import com.android.iflyings.mediasyncplayer.info.MaskInfo;
import com.android.iflyings.mediasyncplayer.opengl.BaseGLObject;
import com.android.iflyings.mediasyncplayer.opengl.MediaContext;
import com.android.iflyings.mediasyncplayer.opengl.ShaderUtils;

import java.io.IOException;


public class MaskData extends MediaData {

    private final MaskInfo mMaskInfo;
    private final BaseGLObject.MaskGLObject mGLObject;
    private final VideoLoader mVideoLoader;
    private final ImageLoader mImageLoader;

    MaskData(MediaContext mediaContext, MaskInfo maskInfo) {
        super(mediaContext, maskInfo);
        mMaskInfo = maskInfo;
        mGLObject = new BaseGLObject.MaskGLObject();
        mVideoLoader = new VideoLoader(mGLObject, this, mMaskInfo.getVideoPath(), mMaskInfo.getHardwareCodec());
        mImageLoader = new ImageLoader(mGLObject, this, null, "no_media.jpg");
    }

    @Override
    protected BaseGLObject getGLObject() {
        return mGLObject;
    }

    @NonNull
    @Override
    public String toString() {
        return "[MaskData]:" + mMaskInfo.getVideoPath() + "," + mVideoLoader.toString()  + "," +super.toString();
    }

    @Override
    protected void onLoadMedia() throws IOException {
        mVideoLoader.loadMedia();
        mImageLoader.loadMedia(mMaskInfo.getBlockWidth(), mMaskInfo.getBlockHeight());
    }

    @Override
    protected void onStartMedia() {
        mVideoLoader.startMedia();
    }

    @Override
    protected int onDrawMedia(int index) {
        mVideoLoader.drawMedia();
        index = mGLObject.draw(index);
        ShaderUtils.checkError();
        return index;
    }

    @Override
    protected void onStopMedia() {
        mVideoLoader.stopMedia();
    }

    @Override
    protected void onUnloadMedia() {
        mVideoLoader.unloadMedia();
        mImageLoader.unloadMedia();
    }

}
