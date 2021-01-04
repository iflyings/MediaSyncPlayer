package com.android.iflyings.mediasyncplayer.opengl;

import android.opengl.GLES30;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

import com.android.iflyings.mediasyncplayer.info.BlockInfo;
import com.android.iflyings.mediasyncplayer.info.MediaInfo;
import com.android.iflyings.mediasyncplayer.opengl.data.DecorativeTextData;
import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BlockWindow implements MediaContext {

    private final BlockContext mBlockContext;
    private final BlockInfo mBlockInfo;

    private DecorativeTextData mDecorativeTextData;

    private MediaManager mMediaManager;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    public int getBlockLeft() {
        return mBlockInfo.getBlockLeft();
    }
    public int getBlockTop() {
        return mBlockInfo.getBlockTop();
    }
    public int getBlockWidth() {
        return mBlockInfo.getBlockWidth();
    }
    public int getBlockHeight() {
        return mBlockInfo.getBlockHeight();
    }
    public int getWindowWidth() {
        return mBlockInfo.getWindowWidth();
    }
    public int getWindowHeight() {
        return mBlockInfo.getWindowHeight();
    }

    public BlockWindow(BlockContext blockContext, BlockInfo blockInfo) {
        mBlockContext = blockContext;
        mBlockInfo = blockInfo;
    }

    private MediaData createNoMediaData() {
        MediaInfo mi = MediaInfo.createNoMedia(mBlockInfo.getBlockWidth(), mBlockInfo.getBlockHeight());
        return MediaData.create(this, mi);
    }

    public void create() {
        mBackgroundThread = new HandlerThread(mBlockInfo.getBlockName(), Process.THREAD_PRIORITY_FOREGROUND);
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        List<MediaData> list = new ArrayList<>(mBlockInfo.getMediaInfoList().size());
        for (MediaInfo mediaInfo : mBlockInfo.getMediaInfoList()) {
            MediaData md = MediaData.create(this, mediaInfo);
            list.add(md);
        }
        mMediaManager = new MediaManager(list, createNoMediaData());
        mMediaManager.startPlayer(mBackgroundThread.getLooper());

        mBackgroundHandler.post(() -> {
            if (mBlockInfo.getDecorativeTextInfo() != null) {
                mDecorativeTextData = new DecorativeTextData(BlockWindow.this, mBlockInfo.getDecorativeTextInfo());
                try {
                    mDecorativeTextData.create();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public int draw(int index) {
        index = mMediaManager.drawPlayer(index);
        if (mDecorativeTextData != null) {
            index = mDecorativeTextData.drawMedia(index);
        }
        return index;
    }
    public void destroy() {
        mBackgroundHandler.removeCallbacksAndMessages(null);
        mMediaManager.stopPlayer();
        if (mDecorativeTextData != null) {
            mDecorativeTextData.destroy();
        }
        mBackgroundThread.quitSafely();
    }

    public String dumpPlayer() {
        return "BlockWindow = " + "[" +
                getBlockLeft() + "," +
                getBlockTop() + "," +
                (getBlockLeft() + getBlockWidth()) + "," +
                (getBlockTop() + getBlockHeight()) + "]\n" +
                mMediaManager.dumpPlayer();
    }

    @Override
    public void onCompletion(MediaData md) {
        mMediaManager.playNextMedia();
    }

    @Override
    public void onError(MediaData mediaData, int type, String msg) {
        mMediaManager.errorMedia(mediaData);
    }

    @Override
    public void runInGLThread(Runnable runnable) {
        mBlockContext.runInGLThread(runnable);
    }

    @Override
    public Looper getUserThreadLooper() {
        return mBackgroundThread.getLooper();
    }

    @Override
    public void requestRender() {
        mBlockContext.requestRender();
    }

}
