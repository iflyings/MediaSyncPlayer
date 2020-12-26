package com.android.iflyings.videoplayer;

import android.view.Surface;

import java.io.IOException;


public class VideoPlayer {

    private final static String TAG = "VideoPlayer";

    public final static int ERR_SURFACE_IS_NULL = 1;
    public final static int ERR_MEDIA_CODEC_DIE = 2;

    private final VideoCodecRender mVideoCodec;
    private final AudioCodecRender mAudioCodec;

    public VideoPlayer() {
        mVideoCodec = new VideoCodecRender();
        mAudioCodec = new AudioCodecRender();
    }
    public void setSoftwareCodec(boolean b) {
        mVideoCodec.setSoftwareCodec(b);
        mAudioCodec.setSoftwareCodec(b);
    }
    public void setDataSource(String mediaPath) {
        mVideoCodec.setDataSource(mediaPath);
        mAudioCodec.setDataSource(mediaPath);
    }
    public void setSurface(Surface surface) {
        mVideoCodec.setSurface(surface);
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(int width, int height);
    }
    public void setOnVideoSizeChangedListener(final OnVideoSizeChangedListener listener) {
        mVideoCodec.setOnVideoSizeChangedListener(listener);
    }

    public interface OnCompletionListener {
        void onCompletion();
    }
    public void setOnCompletionListener(final OnCompletionListener listener) {
        mVideoCodec.setOnCompletionListener(listener);
    }

    public interface OnErrorListener {
        void onError(int type, String msg);
    }
    public void setOnErrorListener(final OnErrorListener listener) {
        mVideoCodec.setOnErrorListener(listener);
    }

    public interface OnTickListener {
        void onRenderer(long presentationTimeUs);
    }
    public void setOnTickListener(final OnTickListener listener) {
        mVideoCodec.setOnTickListener(listener);
    }

    public void prepare() throws IOException {
        mVideoCodec.create();
        //mAudioCodec.create();
    }

    public void startAndWaitAtFirstFrame() {
        mVideoCodec.startAndWaitAtFirstFrame();
    }

    public void start() {
        mVideoCodec.start();
        //mAudioCodec.start(null);
    }

    public void pause() {
        //mVideoCodec.start();
        //mAudioCodec.start();
    }

    public void release() {
        mVideoCodec.release();
        //mAudioCodec.release();
    }

}
