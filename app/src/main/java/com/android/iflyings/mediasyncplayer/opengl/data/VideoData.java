package com.android.iflyings.mediasyncplayer.opengl.data;

import androidx.annotation.NonNull;
import com.android.iflyings.mediasyncplayer.info.VideoInfo;
import com.android.iflyings.mediasyncplayer.opengl.BaseGLObject;
import com.android.iflyings.mediasyncplayer.opengl.MediaContext;
import com.android.iflyings.mediasyncplayer.opengl.ShaderUtils;

import java.io.IOException;


public class VideoData extends MediaData {

    private final VideoInfo mVideoInfo;
    private final BaseGLObject.VideoGLObject mGLObject;
    private final VideoLoader mVideoLoader;

    VideoData(MediaContext mediaContext, VideoInfo videoInfo) {
        super(mediaContext, videoInfo);
        mVideoInfo = videoInfo;
        mGLObject = new BaseGLObject.VideoGLObject();
        mVideoLoader = new VideoLoader(mGLObject, this, videoInfo.getVideoPath(), mVideoInfo.getHardwareCodec());
    }

    @NonNull
    @Override
    public String toString() {
        return "[VideoData]:" + mVideoInfo.getVideoPath() + "," + mVideoLoader.toString()  + "," + super.toString();
    }

    @Override
    protected void onLoadMedia() throws IOException {
        mVideoLoader.loadMedia();
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
    }

    @Override
    protected BaseGLObject getGLObject() {
        return mGLObject;
    }
}
