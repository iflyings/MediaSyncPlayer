package com.android.iflyings.mediasyncplayer.opengl.data;

import android.opengl.Matrix;
import android.os.Handler;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.android.iflyings.mediasyncplayer.BuildConfig;
import com.android.iflyings.mediasyncplayer.Constant;
import com.android.iflyings.mediasyncplayer.CrashHandler;
import com.android.iflyings.mediasyncplayer.info.TextInfo;
import com.android.iflyings.mediasyncplayer.opengl.BaseGLObject;
import com.android.iflyings.mediasyncplayer.opengl.component.Component;
import com.android.iflyings.mediasyncplayer.opengl.component.EffectComponent;
import com.android.iflyings.mediasyncplayer.info.MaskInfo;
import com.android.iflyings.mediasyncplayer.opengl.MediaContext;
import com.android.iflyings.mediasyncplayer.opengl.component.SyncComponent;
import com.android.iflyings.mediasyncplayer.info.ImageInfo;
import com.android.iflyings.mediasyncplayer.info.MediaInfo;
import com.android.iflyings.mediasyncplayer.info.VideoInfo;
import com.android.iflyings.mediasyncplayer.sync.SyncManager;

import java.io.IOException;
import java.util.HashMap;

import hugo.weaving.DebugLog;

import static com.android.iflyings.videoplayer.VideoPlayer.ERR_MEDIA_CODEC_DIE;

/*
 *                      unload()
 *           |-----------------------------→error
 *           |
 *           |      load()            start()             stop()
 *        unload -----------→ load ------------→ start -----------→ stop
 *           ↑                 ↑                  load()              |
 *           |                 |--------------------------------------|
 *           |                      unload()                          |
 *           |--------------------------------------------------------|
 *
 *
 */
public abstract class MediaData implements LoaderCallback {

    private final static int STATE_UNLOAD = 0;
    private final static int STATE_LOAD = 1;
    private final static int STATE_START = 2;
    private final static int STATE_STOP = 4;
    private final static int STATE_ERROR = 5;

    private final MediaContext mMediaContext;
    private final MediaInfo mMediaInfo;
    private final HashMap<Class<?>, Component> mComponents;

    private final float[] mModelMatrixData = new float[16];
    private final float[] mViewMatrixData = new float[16];
    private final float[] mProjMatrixData = new float[16];

    private volatile int mState = STATE_UNLOAD;

    private Handler mUserHandler;

    private int mTextureWidth;
    private int mTextureHeight;

    private long mStartTickUs = 0L;
    private long mDrawFrameCount = 0L;
    private boolean isVisible = true;
    private boolean isSupported = true;

    protected MediaData(MediaContext mediaContext, MediaInfo mediaInfo) {
        mMediaContext = mediaContext;
        mMediaInfo = mediaInfo;
        mComponents = new HashMap<>();

        Matrix.setIdentityM(mModelMatrixData, 0);

        Matrix.setIdentityM(mViewMatrixData, 0);
        //Matrix.setLookAtM(viewMatrix, 0, 0f, 5f, 10f, 0f, 0f, 0f, 0f, 1f, 0F);

        Matrix.setIdentityM(mProjMatrixData, 0);
        //Matrix.frustumM(projMatrix, 0, -1, 1, -1f, 1f, 3f, 20f);
        Matrix.perspectiveM(mProjMatrixData, 0, 45, 1, 1f, 100f);
        //Matrix.orthoM(projMatrix, 0, -1f, 1f, -1, 1, -1f, 1f);
    }

    public int getBlockWidth() {
        return mMediaInfo.getBlockWidth();
    }
    public int getBlockHeight() {
        return mMediaInfo.getBlockHeight();
    }

    public int getTextureWidth() {
        return mTextureWidth;
    }
    public int getTextureHeight() {
        return mTextureHeight;
    }

    private void addComponent(Class<? extends Component> cls, Component c) {
        mComponents.put(cls, c);
    }
    public <T extends Component> T getComponent(Class<T> cls) {
        return (T) mComponents.get(cls);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("State = ");
        switch (mState) {
            case STATE_UNLOAD:
                sb.append("Unload");
                break;
            case STATE_LOAD:
                sb.append("Load");
                break;
            case STATE_START:
                sb.append("Start");
                break;
            case STATE_STOP:
                sb.append("Stop");
                break;
            case STATE_ERROR:
                sb.append("Error");
                break;
            default:
                sb.append("Unknow");
                break;
        }
        sb.append(",");
        sb.append("isSupported = ").append(isSupported).append(",");
        sb.append("StartTickUs = ").append(mStartTickUs).append(",");
        sb.append("DrawFrameCount = ").append(mDrawFrameCount).append(",");
        return sb.toString();
    }

    public static MediaData create(MediaContext mediaContext, MediaInfo mediaInfo) {
        MediaData mediaData = null;
        if (mediaInfo instanceof VideoInfo) {
            mediaData = new VideoData(mediaContext, (VideoInfo) mediaInfo);
        } else if (mediaInfo instanceof ImageInfo) {
            mediaData = new ImageData(mediaContext, (ImageInfo) mediaInfo);
        } else if (mediaInfo instanceof MaskInfo) {
            mediaData = new MaskData(mediaContext, (MaskInfo) mediaInfo);
        } else if (mediaInfo instanceof TextInfo) {
            mediaData = new TextData(mediaContext, (TextInfo) mediaInfo);
        }
        assert mediaData != null;
        if (mediaData.getGLObject() instanceof BaseGLObject.EffectGLObject) {
            mediaData.addComponent(EffectComponent.class, new EffectComponent((BaseGLObject.EffectGLObject)mediaData.getGLObject(), mediaInfo));
        }
        if (mediaInfo.getMediaToken() != null) {
            mediaData.addComponent(SyncComponent.class, new SyncComponent(mediaInfo.getMediaToken()));
        }
        return mediaData;
    }

    private void checkInUserThread() {
        if (Thread.currentThread() != mMediaContext.getUserThreadLooper().getThread()) {
            throw new IllegalStateException("must run in user thread!!!");
        }
    }

    @Override
    public final void notifyMediaSize(int width, int height) {
        mTextureWidth = width;
        mTextureHeight = height;
    }
    @Override
    public final void notifyMediaRender() {
        mMediaContext.requestRender();
    }
    @Override
    public final void notifyMediaCompletion() {
        runInUserThreadDelay(()-> {
            stopMedia();
            mState = STATE_STOP;
            mMediaContext.onCompletion(this);
        }, 0);
    }
    @Override
    public final void notifyMediaError(int type, String msg) {
        runInUserThreadDelay(() -> {
            unloadMedia();
            mState = STATE_ERROR;
            mMediaContext.onError(this, type, msg);
            if (type == ERR_MEDIA_CODEC_DIE) {
                CrashHandler.writeErrorInfoToFile( toString() + " media.codec is died");
            }
        }, 0);
    }
    @Override
    public final void runInGLThread(Runnable runnable) {
        mMediaContext.runInGLThread(runnable);
    }
    public final void runInUserThreadDelay(Runnable runnable, long delay) {
        mUserHandler.postDelayed(runnable, delay);
    }

    public float[] lockModelMatrix() {
        Matrix.setIdentityM(mModelMatrixData, 0);
        Matrix.translateM(mModelMatrixData, 0, 0f, 0f, -mProjMatrixData[0]);
        return mModelMatrixData;
    }
    public void unlockModelMatrix() {
        float[] tmpVMMatrix = new float[16];
        Matrix.multiplyMM(tmpVMMatrix, 0, mViewMatrixData, 0, mModelMatrixData, 0);
        float[] tmpPVMMatrix = new float[16];
        Matrix.multiplyMM(tmpPVMMatrix, 0, mProjMatrixData, 0, tmpVMMatrix, 0);
        getGLObject().setMVPMatrixData(tmpPVMMatrix);
    }
    public void reset() {
        isVisible = true;
        float[] tmpPVMMatrix = new float[16];
        Matrix.setIdentityM(tmpPVMMatrix, 0);
        getGLObject().setMVPMatrixData(tmpPVMMatrix);
        EffectComponent component = getComponent(EffectComponent.class);
        if (component != null) {
            component.reset();
        }
    }
    public void translate(float dx, float dy) {
        float[] tmpPVMMatrix = new float[16];
        Matrix.setIdentityM(tmpPVMMatrix, 0);
        Matrix.translateM(tmpPVMMatrix, 0, 2*dx, 2*dy, 0);
        getGLObject().setMVPMatrixData(tmpPVMMatrix);
    }
    public void scale(float x, float y) {
        float[] tmpPVMMatrix = new float[16];
        Matrix.setIdentityM(tmpPVMMatrix, 0);
        Matrix.scaleM(tmpPVMMatrix, 0, x, y, 1f);
        getGLObject().setMVPMatrixData(tmpPVMMatrix);
    }

    public void setVisible(boolean b) {
        isVisible = b;
    }

    public final boolean isLoadMedia() {
        return mState != STATE_UNLOAD && mState != STATE_ERROR;
    }
    public final boolean isMediaSupported() {
        return isSupported;
    }

    @DebugLog
    public final boolean loadMedia() {
        checkInUserThread();
        if (mUserHandler == null) {
            mUserHandler = new Handler();
        }
        if (mState == STATE_UNLOAD || mState == STATE_STOP || mState == STATE_ERROR) {
            mState = STATE_LOAD;
            try {
                long startTickMs = SystemClock.currentThreadTimeMillis();
                onLoadMedia();
                if (BuildConfig.DEBUG && SystemClock.currentThreadTimeMillis() - startTickMs > Constant.MEDIA_LOAD_MAX_TIME_MS) {
                    throw new IllegalStateException(this + "->function call is timeout!!!");
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                isSupported = false;
                mState = STATE_ERROR;
                return false;
            }
        } else if (mState == STATE_LOAD) {
            return true;
        }
        throw new IllegalStateException("can not run load media" + "@" + toString());
    }
    public final void startMedia() {
        checkInUserThread();
        if (mState == STATE_LOAD || mState == STATE_STOP) {
            mState = STATE_START;
            onStartMedia();
            mDrawFrameCount = 0;
            if (mStartTickUs <= 0) {
                mStartTickUs = SyncManager.getNetWorkTickUs();
            }
            return;
        } else if (mState == STATE_START || mState == STATE_ERROR) {
            return;
        }
        throw new IllegalStateException("can not run start media" + "@" + toString());
    }
    public final int drawMedia(int index) {
        if (mState == STATE_START || mState == STATE_STOP) {
            if (isVisible) {
                index = onDrawMedia(index);
                mDrawFrameCount++;
            }
        }
        return index;
    }
    //@DebugLog
    public final void stopMedia() {
        checkInUserThread();
        if (mState == STATE_START) {
            mStartTickUs = -1;
            mDrawFrameCount = 0;
            mState = STATE_STOP;
            onStopMedia();
            return;
        } else if (mState == STATE_STOP || mState == STATE_ERROR) {
            return;
        }
        throw new IllegalStateException("can not run stop media" + "@" + toString());
    }
    //@DebugLog
    public final void unloadMedia() {
        //checkInUserThread();
        if (mState == STATE_START) {
            mStartTickUs = -1;
            mDrawFrameCount = 0;
            mState = STATE_UNLOAD;
            onStopMedia();
            onUnloadMedia();
            return;
        } else if (mState == STATE_STOP || mState == STATE_LOAD) {
            mState = STATE_UNLOAD;
            onUnloadMedia();
            return;
        } else if (mState == STATE_UNLOAD || mState == STATE_ERROR) {
            return;
        }
        throw new IllegalStateException("can not run unload media" + "@" + toString());
    }

    public long getStartTickUs() {
        return mStartTickUs;
    }

    public void setStartTickUs(long start) {
        mStartTickUs = start;
    }

    protected abstract void onLoadMedia() throws IOException;

    protected abstract void onStartMedia();

    protected abstract int onDrawMedia(int index);

    protected abstract void onStopMedia();

    protected abstract void onUnloadMedia();

    protected abstract BaseGLObject getGLObject();
}
