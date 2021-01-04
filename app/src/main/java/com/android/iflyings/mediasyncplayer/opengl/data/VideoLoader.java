package com.android.iflyings.mediasyncplayer.opengl.data;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.android.iflyings.mediasyncplayer.Constant;
import com.android.iflyings.mediasyncplayer.sync.SyncManager;
import com.android.iflyings.nativeplayer.NativePlayer;
import com.android.iflyings.videoplayer.VideoPlayer;

import java.io.IOException;


public class VideoLoader implements VideoPlayer.OnTickListener,
        VideoPlayer.OnCompletionListener, VideoPlayer.OnErrorListener {

    private final String mVideoPath;
    private final boolean isHardWareCodec;
    private final Object mRenderLock = new Object();

    private final VideoGLObjectImpl mGLObjectImpl;
    private final LoaderCallback mLoaderCallback;

    private VideoPlayer mVideoPlayer;
    private SurfaceTexture mSurfaceTexture;
    private long mPresentationTimeUs = 0L;

    public VideoLoader(VideoGLObjectImpl impl, LoaderCallback cb, String videoPath, boolean isHardCodec) {
        mGLObjectImpl = impl;
        mLoaderCallback = cb;
        mVideoPath = videoPath;
        isHardWareCodec = isHardCodec;
    }

    public void loadMedia() throws IOException {
        //Log.i(TAG, "onLoadMedia");
        if (mVideoPlayer == null) {
            mVideoPlayer = new VideoPlayer();
            mVideoPlayer.setSoftwareCodec(!isHardWareCodec);
            mVideoPlayer.setDataSource(mVideoPath);
            mVideoPlayer.setOnTickListener(this);
            mVideoPlayer.setOnCompletionListener(this);
            mVideoPlayer.setOnErrorListener(this);
            //mVideoPlayer.setOnVideoSizeChangedListener(mLoaderCallback::notifyMediaSize);
            final Object lock = new Object();
            mLoaderCallback.runInGLThread(() -> {
                Log.i("zw", "mLoaderCallback.runInGLThread");
                mGLObjectImpl.create();
                mSurfaceTexture = mGLObjectImpl.getSurfaceTexture();
                synchronized (lock) {
                    lock.notifyAll();
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
        if (mSurfaceTexture == null) {
            throw new IllegalStateException("mSurfaceTexture is null in load media");
        }
        mVideoPlayer.setSurface(new Surface(mSurfaceTexture));
        mVideoPlayer.prepare();
        mVideoPlayer.startAndWaitAtFirstFrame();
    }

    public void startMedia() {
        if (mSurfaceTexture == null) {
            throw new IllegalStateException("SurfaceTexture is null");
        }
        mPresentationTimeUs = 0;
        mVideoPlayer.start();
    }
    public void drawMedia() {
        if (mSurfaceTexture == null) {
            throw new IllegalStateException("VideoLoader:" + mVideoPath + " mSurfaceTexture is null");
        }
        synchronized (mRenderLock) {
            mRenderLock.notifyAll();
        }
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mGLObjectImpl.getTexMatrixData());
    }
    public void stopMedia() {
        mPresentationTimeUs = 0;
    }
    public void unloadMedia() {
        mPresentationTimeUs = 0;
        mVideoPlayer.release();
        mLoaderCallback.runInGLThread(mGLObjectImpl::destroy);
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mVideoPlayer = null;
    }

    @Override
    public void onRenderer(long presentationTimeUs) {
        mPresentationTimeUs = presentationTimeUs;
        //SyncManager.sleepToNetWorkTickUs(presentationTimeUs + getStartTickUs());
        long startTickUs = SyncManager.getNetWorkTickUs();
        while (presentationTimeUs >= SyncManager.getNetWorkTickUs() - mLoaderCallback.getStartTickUs()) {
            synchronized (mRenderLock) {
                try {
                    mRenderLock.wait(Constant.VIDEO_RENDERER_MAX_DELAY_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 避免视频卡主不动了
            if (SyncManager.getNetWorkTickUs() > startTickUs + Constant.VIDEO_RENDERER_MAX_DELAY_MS * 1000) {
                break;
            }
        }
    }

    @Override
    public void onCompletion() {
        mLoaderCallback.notifyMediaCompletion();
    }

    @Override
    public void onError(int type, String msg) {
        mLoaderCallback.notifyMediaError(type, msg);
    }

    @NonNull
    @Override
    public String toString() {
        return "PresentationTimeUs = " + mPresentationTimeUs;
    }

    public interface VideoGLObjectImpl {

        void create();

        void destroy();

        SurfaceTexture getSurfaceTexture();

        float[] getTexMatrixData();

    }

}
