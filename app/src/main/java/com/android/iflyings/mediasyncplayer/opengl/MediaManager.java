package com.android.iflyings.mediasyncplayer.opengl;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.iflyings.mediasyncplayer.Constant;
import com.android.iflyings.mediasyncplayer.opengl.component.EffectComponent;
import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;
import com.android.iflyings.mediasyncplayer.opengl.transformer.MediaTransformer;

import java.lang.ref.WeakReference;
import java.util.List;


public class MediaManager {

    private final static int MSG_PLAY_NEXT_MEDIA = 1;
    private final static int MSG_REMOVE_MEDIA = 2;
    private final static int MSG_START_ANIMATION = 3;
    private final static int MSG_UPDATE_ANIMATION = 4;
    private final static int MSG_END_ANIMATION = 5;
    private final static int MSG_CLEAN_MEDIA = 6;
    private final static int MSG_ERROR_MEDIA = 7;

    private final List<MediaData> mMediaDataList;
    private final ScrollHelper mScrollHelper;
    private final MediaData mDefaultNoMediaData;

    private MediaHandler mMediaHandler;
    private int mCurrentMediaIndex = -1;

    private final Object mDrawLock = new Object();
    private MediaData mCurrentMediaData;
    private MediaData mNextMediaData;
    private MediaTransformer mMediaTransformer;

    public MediaManager(List<MediaData> list, MediaData noMediaData) {
        mMediaDataList = list;
        mScrollHelper = new ScrollHelper();
        mDefaultNoMediaData = noMediaData;
    }

    public void startPlayer(Looper looper) {
        mMediaHandler = new MediaHandler(looper, this);
        mCurrentMediaIndex = -1;
        playNextMedia();
    }

    public void stopPlayer() {
        for (int i = 0;i < mMediaDataList.size();i ++) {
            MediaData mediaData = mMediaDataList.get(i);
            if (mediaData.isLoadMedia()) {
                mMediaDataList.get(i).unloadMedia();
            }
        }
    }

    public int drawPlayer(int index) {
        synchronized (mDrawLock) {
            MediaData currMediaData = mCurrentMediaData;
            MediaData nextMediaData = mNextMediaData;
            if (nextMediaData != null) {
                index = nextMediaData.drawMedia(index);
            }
            if (currMediaData != null) {
                index = currMediaData.drawMedia(index);
            }
        }
        return index;
    }

    public void playNextMedia() {
        mMediaHandler.obtainMessage(MSG_PLAY_NEXT_MEDIA).sendToTarget();
    }
    public void removeMedia(MediaData mediaData) {
        mMediaHandler.obtainMessage(MSG_REMOVE_MEDIA, mediaData).sendToTarget();
    }
    public void errorMedia(MediaData mediaData) {
        mMediaHandler.obtainMessage(MSG_ERROR_MEDIA, mediaData).sendToTarget();
    }

    private void startPlayAnimation(MediaData nextMedia) {
        EffectComponent component = nextMedia.getComponent(EffectComponent.class);
        if (mCurrentMediaData != null && component != null) {
            mMediaTransformer = component.getMediaTransformer();
            mScrollHelper.startScroll(0f, 1f, 1000);
            float currPos = mScrollHelper.getCurrPos();
            mMediaTransformer.transformMedia(mCurrentMediaData, currPos);
            mMediaTransformer.transformMedia(nextMedia, currPos - 1f);
            mNextMediaData = nextMedia;
            mMediaHandler.sendMessageDelayed(mMediaHandler.obtainMessage(MSG_UPDATE_ANIMATION, nextMedia), 16);
        } else {
            mMediaHandler.sendMessage(mMediaHandler.obtainMessage(MSG_END_ANIMATION, nextMedia));
        }
    }
    private void updatePlayAnimation(MediaData nextMedia) {
        if (mScrollHelper.computeScrollOffset()) {
            MediaTransformer mediaTransformer = mMediaTransformer;
            float currPos = mScrollHelper.getCurrPos();
            mediaTransformer.transformMedia(mCurrentMediaData, currPos);
            mediaTransformer.transformMedia(mNextMediaData, currPos - 1);
            mMediaHandler.sendMessageDelayed(mMediaHandler.obtainMessage(MSG_UPDATE_ANIMATION, nextMedia), 16);
        } else {
            mMediaHandler.sendMessage(mMediaHandler.obtainMessage(MSG_END_ANIMATION, nextMedia));
        }
    }
    private void endPlayAnimation(MediaData nextMedia) {
        MediaData currMedia = mCurrentMediaData;
        // 结束动画
        synchronized (mDrawLock) {
            mCurrentMediaData = nextMedia;
            mNextMediaData = null;
        }
        if (currMedia != null) {
            currMedia.reset();
        }
        nextMedia.reset();

        mMediaHandler.sendMessage(mMediaHandler.obtainMessage(MSG_CLEAN_MEDIA));
    }

    private void playNextMediaInUserThread() {
        MediaData nextMedia = null;
        if (mMediaDataList.size() > 1) {
            int nextIndex = (mCurrentMediaIndex + 1) % mMediaDataList.size();
            int startIndex = nextIndex;
            do {
                MediaData mediaData = mMediaDataList.get(nextIndex);
                if (mediaData.isMediaSupported()) {
                    if (mediaData.isLoadMedia()) {
                        nextMedia = mediaData;
                        mCurrentMediaIndex = nextIndex;
                        break;
                    } else if (mediaData.loadMedia()) {
                        nextMedia = mediaData;
                        mCurrentMediaIndex = nextIndex;
                        break;
                    }
                }
                nextIndex = (nextIndex + 1) % mMediaDataList.size();
            } while (startIndex != nextIndex);
        }
        // 显示默认界面
        if (nextMedia == null) {
            nextMedia = mDefaultNoMediaData;
            nextMedia.loadMedia();
            mCurrentMediaIndex = -1;
        }
        nextMedia.startMedia();
        if (mMediaDataList.size() < 2) {
            return;
        }
        mMediaHandler.sendMessage(mMediaHandler.obtainMessage(MSG_START_ANIMATION, nextMedia));
    }

    private void errorMediaInUserThread(MediaData mediaData) {
        if (mediaData == null) {
            return;
        }
        /*if (mediaData.isLoadMedia()) {
            mediaData.unloadMedia();
        }*/
        int errorIndex = mMediaDataList.indexOf(mediaData);
        if (errorIndex < 0) {
            return;
        }

        if (errorIndex == mCurrentMediaIndex) {
            playNextMediaInUserThread();
            return;
        }

        mMediaHandler.sendMessage(mMediaHandler.obtainMessage(MSG_CLEAN_MEDIA));
    }

    private void removeMediaInUserThread(MediaData mediaData) {

    }

    private void loadOrUnloadMedia() {
        if (mMediaDataList.size() <= 1) {
            return;
        }
        int currIndex = mCurrentMediaIndex;
        int nextIndex = currIndex;
        if (Constant.VIDEO_MEDIA_RELOAD_CACHE) {
            nextIndex = (currIndex + 1) % mMediaDataList.size();
            while (nextIndex != currIndex) {
                MediaData mediaData = mMediaDataList.get(nextIndex);
                if (mediaData.isMediaSupported() && (mediaData.isLoadMedia() || mediaData.loadMedia())) {
                    break;
                }
                nextIndex = (nextIndex + 1) % mMediaDataList.size();
            }
            if (nextIndex == currIndex) {
                return;
            }
        }

        int posIndex = (nextIndex + 1) % mMediaDataList.size();
        while (posIndex != currIndex) {
            MediaData mediaData = mMediaDataList.get(posIndex);
            if (mediaData.isLoadMedia()) {
                mediaData.unloadMedia();
            }
            posIndex = (posIndex + 1) % mMediaDataList.size();
        }
    }

    public String dumpPlayer() {
        StringBuilder sb = new StringBuilder();
        for (MediaData mediaData : mMediaDataList) {
            sb.append("      ").append(mediaData).append("\n");
        }
        return sb.toString();
    }

    static class MediaHandler extends Handler {
        private final WeakReference<MediaManager> mWRMediaManager;

        MediaHandler(Looper looper, MediaManager mediaManager) {
            super(looper);
            mWRMediaManager = new WeakReference<>(mediaManager);
        }

        @Override
        public void handleMessage(Message msg) {
            MediaManager mediaManager = mWRMediaManager.get();
            if (mediaManager == null) {
                return;
            }
            switch (msg.what) {
                case MSG_PLAY_NEXT_MEDIA: {
                    mediaManager.playNextMediaInUserThread();
                    break;
                }
                case MSG_REMOVE_MEDIA: {
                    mediaManager.removeMediaInUserThread((MediaData) msg.obj);
                    break;
                }
                case MSG_START_ANIMATION: {
                    mediaManager.startPlayAnimation((MediaData) msg.obj);
                    break;
                }
                case MSG_UPDATE_ANIMATION: {
                    mediaManager.updatePlayAnimation((MediaData) msg.obj);
                    break;
                }
                case MSG_END_ANIMATION: {
                    mediaManager.endPlayAnimation((MediaData) msg.obj);
                    break;
                }
                case MSG_CLEAN_MEDIA: {
                    mediaManager.loadOrUnloadMedia();
                    break;
                }
                case MSG_ERROR_MEDIA: {
                    mediaManager.errorMediaInUserThread((MediaData) msg.obj);
                    break;
                }
            }
        }
    }

}
