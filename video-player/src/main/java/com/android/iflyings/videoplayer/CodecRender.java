package com.android.iflyings.videoplayer;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import static com.android.iflyings.videoplayer.VideoPlayer.ERR_MEDIA_CODEC_DIE;
import static com.android.iflyings.videoplayer.VideoPlayer.ERR_SURFACE_IS_NULL;


abstract class CodecRender {

    private final static long TIMEOUT_US = 20 * 1000;//-1L;
    private final static boolean DEBUG = true;
    private final static String TAG = "CodecRender";

    private final static int MSG_MEDIA_CREATE = 0;
    private final static int MSG_MEDIA_START = 1;
    private final static int MSG_MEDIA_CODEC = 2;
    private final static int MSG_MEDIA_SEEK = 3;
    private final static int MSG_MEDIA_PAUSE = 4;
    private final static int MSG_MEDIA_RELEASE = 5;

    private final static int MEDIA_STATE_UNINIT = 0;
    private final static int MEDIA_STATE_INIT = 1;
    private final static int MEDIA_STATE_START = 2;
    private final static int MEDIA_STATE_PAUSE = 3;
    private final static int MEDIA_STATE_COMPLETE = 4;
    private final static int MEDIA_STATE_ERROR = 5;

    private final static Object mRenderObject = new Object();

    private Surface mSurface;
    private String mFilePath;
    private boolean isSoftwareCodec = true;

    private MediaCodec mMediaCodec;
    private MediaExtractor mMediaExtractor;

    private HandlerThread mThread;
    private CodecHandler mHandler;
    private VideoPlayer.OnTickListener mOnTickListener;

    private int mMediaState = MEDIA_STATE_UNINIT;

    private boolean sawInputEOS = false; // 输入解码完成
    private boolean sawOutputEOS = false; // 输出解码完成

    private boolean isMediaPlaying = true;

    CodecRender() {
    }

    private VideoPlayer.OnErrorListener mOnErrorListener;
    void setOnErrorListener(VideoPlayer.OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    protected abstract String getCodecType();

    protected abstract void onProcessOutputBuffer(ByteBuffer byteBuffer, int size, long presentationTimeUs);

    protected abstract void onFormatChanged(MediaFormat mediaFormat);

    protected abstract void onCompletion();

    protected void onCreated() {
    }
    protected void onStarted() {
    }
    protected void onReleased() {
    }

    public void setOnTickListener(VideoPlayer.OnTickListener listener) {
        mOnTickListener = listener;
    }

    public void setSurface(Surface surface) {
        if (mSurface != null) {
            mSurface.release();
        }
        mSurface = surface;
    }

    public void setDataSource(String path) {
        mFilePath = path;
    }

    public void setSoftwareCodec(boolean b) {
        isSoftwareCodec = b;
    }

    public final void create() throws IOException {
        if (mFilePath == null) {
            throw new IOException("file path is null");
        }
        if (mSurface == null) {
            throw new IOException("out surface is null");
        }
        if (mThread != null) {
            mHandler.removeCallbacksAndMessages(null);
            mThread.quit();
        }
        if (mMediaExtractor != null) {
            mMediaExtractor.release();
        }
        if (mMediaCodec != null) {
            mMediaCodec.release();
        }
        mMediaExtractor = new MediaExtractor();
        mMediaExtractor.setDataSource(mFilePath);

        MediaFormat mediaFormat = null;
        for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
            MediaFormat format = mMediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith(getCodecType())) {
                mMediaExtractor.selectTrack(i);
                mediaFormat = format;
                int width = 0;
                int height = 0;
                if (this instanceof VideoCodecRender) {
                    width = format.getInteger(MediaFormat.KEY_WIDTH);
                    height = format.getInteger(MediaFormat.KEY_HEIGHT);
                }
                mMediaCodec = MediaCodecUtils.getMediaCodec(mime, isSoftwareCodec, !isSoftwareCodec, 0, 0);
                onFormatChanged(format);
                break;
            }
        }

        if (mMediaCodec == null) {
            mMediaExtractor.release();
            mMediaExtractor = null;
            throw new MediaNoTrackException(mFilePath + " does not contain trackers!!!");
        }

        mMediaCodec.configure(mediaFormat, mSurface, null, 0);
        mSurface.release();
        mSurface = null;

        mThread = new HandlerThread(getCodecType() + "-codec-thread", android.os.Process.THREAD_PRIORITY_FOREGROUND);
        mThread.start();
        mHandler = new CodecHandler(this, mThread.getLooper());
        onCreated();
        mMediaState = MEDIA_STATE_INIT;
    }

    public void startAndWaitAtFirstFrame() {
        isMediaPlaying = false;
        mHandler.sendStart();
        synchronized (mRenderObject) {
            try {
                mRenderObject.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public final void start() {
        isMediaPlaying = true;
        mHandler.sendStart();
    }
    public final void seekTo() {
        mHandler.sendSeekTo();
    }
    public final void pause() {
        isMediaPlaying = false;
        mHandler.sendPause();
    }
    public final void release() {
        mHandler.sendRelease();
        mThread.quitSafely();
        mThread = null;
        if (DEBUG) Log.d(TAG,"doMediaReleaseInternal 123");
    }

    private void doMediaStartInternal() {
        if (DEBUG) Log.d(TAG,"doMediaStartInternal");
        if (mMediaState == MEDIA_STATE_INIT) {
            mMediaCodec.start();
            mMediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            sawOutputEOS = false;
            sawInputEOS = false;
            mMediaState = MEDIA_STATE_START;
            onStarted();
            mHandler.sendCodec();
        } else if (mMediaState == MEDIA_STATE_PAUSE) {
            mMediaState = MEDIA_STATE_START;
            mHandler.sendCodec();
        }
    }

    private void doMediaCodecInternal() {
        if (mMediaState != MEDIA_STATE_START) {
            return;
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        if (!sawInputEOS) {
            try {
                int inputBufferId = mMediaCodec.dequeueInputBuffer(TIMEOUT_US); // 获取空闲输入通道
                if (inputBufferId >= 0) {
                    ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferId);
                    assert inputBuffer != null;

                    int sampleSize = mMediaExtractor.readSampleData(inputBuffer, 0);
                    if (sampleSize < 0) {
                        if (DEBUG) Log.d(TAG, "saw input EOS.");
                        sawInputEOS = true;
                        mMediaCodec.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        long presentationTimeUs = mMediaExtractor.getSampleTime();
                        mMediaCodec.queueInputBuffer(inputBufferId, 0, sampleSize, presentationTimeUs, 0);
                        mMediaExtractor.advance();
                    }
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                // 此处很可能就是"media.codec"崩溃了
                mMediaCodec = null;
                doMediaErrorInternal(ERR_MEDIA_CODEC_DIE);
                return;
            }
        }
        if (!sawOutputEOS) {
            int outputBufferIndex;
            try {
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                if (outputBufferIndex >= 0) {
                    if (bufferInfo.size > 0) {
                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, bufferInfo.presentationTimeUs);
                    } else {
                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    }
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                // 此处很可能就是"media.codec"崩溃了
                mMediaCodec = null;
                doMediaErrorInternal(ERR_MEDIA_CODEC_DIE);
                return;
            }
            if (outputBufferIndex >= 0) {
                if (mOnTickListener != null && bufferInfo.size > 0) {
                    mOnTickListener.onRenderer(bufferInfo.presentationTimeUs);
                }
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (DEBUG) Log.d(TAG, "saw output EOS.");
                    sawOutputEOS = true;
                }
                if (bufferInfo.size > 0 && !isMediaPlaying) {
                    pause();
                    synchronized (mRenderObject) {
                        mRenderObject.notify();
                    }
                    return;
                }
            } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                //if (DEBUG) Log.d(TAG, "media codec is timeout");
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                onFormatChanged(mMediaCodec.getOutputFormat());
            } else {
                if (DEBUG) Log.d(TAG, "media codec is error");
            }
        }
        if (!sawOutputEOS) {
            mHandler.sendCodec();
        } else {
            mMediaState = MEDIA_STATE_COMPLETE;
            onCompletion();
        }
    }

    private void doMediaSeekInternal() {
        if (DEBUG) Log.d(TAG,"CodecRender doMediaSeekInternal");
        /*
        mMediaExtractor.seekTo(mTickTimerHelper.getPositionTickUs(), MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        long sampleTime = mMediaExtractor.getSampleTime();
        while (sampleTime >= 0 && mTickTimerHelper.getPositionTickUs() >= sampleTime) {
            if (!mMediaExtractor.advance()) {
                break;
            }
            sampleTime = mMediaExtractor.getSampleTime();
        }*/
    }

    private void doMediaPauseInternal() {
        if (DEBUG) Log.d(TAG,"CodecRender doMediaPauseInternal");
        if (mMediaState == MEDIA_STATE_START) {
            mMediaState = MEDIA_STATE_PAUSE;
        }
    }

    private void doMediaReleaseInternal() {
        if (DEBUG) Log.d(TAG,"CodecRender doMediaReleaseInternal start");
        mMediaState = MEDIA_STATE_UNINIT;
        mHandler.removeCallbacksAndMessages(null);
        if (mMediaExtractor != null) {
            mMediaExtractor.release();
            mMediaExtractor = null;
        }
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
        if (mSurface != null && mSurface.isValid()) {
            mSurface.release();
            mSurface = null;
        }
        onReleased();
        if (DEBUG) Log.d(TAG,"CodecRender doMediaReleaseInternal end");
    }

    private void doMediaErrorInternal(int type) {
        if (DEBUG) Log.d(TAG,"CodecRender doMediaErrorInternal");
        mMediaState = MEDIA_STATE_ERROR;
        mHandler.removeCallbacksAndMessages(null);
        if (mOnErrorListener != null) {
            mOnErrorListener.onError(type, "Error:" + mFilePath);
        } else {
            mHandler.sendRelease();
        }
    }

    static class CodecHandler extends Handler {

        private final WeakReference<CodecRender> mWeakBaseCodec;

        CodecHandler(CodecRender codecRender, Looper looper) {
            super(looper);
            mWeakBaseCodec = new WeakReference<>(codecRender);
        }

        void sendCreate() {
            sendEmptyMessage(MSG_MEDIA_CREATE);
        }
        void sendStart() {
            sendEmptyMessage(MSG_MEDIA_START);
        }
        void sendCodec() {
            sendEmptyMessage(MSG_MEDIA_CODEC);
        }
        void sendPause() {
            sendEmptyMessage(MSG_MEDIA_PAUSE);
        }
        void sendSeekTo() {
            sendEmptyMessage(MSG_MEDIA_SEEK);
        }
        void sendRelease() {
            sendEmptyMessage(MSG_MEDIA_RELEASE);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            CodecRender codecRender = mWeakBaseCodec.get();
            if (codecRender == null) {
                return;
            }
            if (msg.what == MSG_MEDIA_CODEC) {
                codecRender.doMediaCodecInternal();
            } else if (msg.what == MSG_MEDIA_START) {
                codecRender.doMediaStartInternal();
            } else if (msg.what == MSG_MEDIA_SEEK) {
                codecRender.doMediaSeekInternal();
            } else if (msg.what == MSG_MEDIA_PAUSE) {
                codecRender.doMediaPauseInternal();
            } else if (msg.what == MSG_MEDIA_RELEASE) {
                codecRender.doMediaReleaseInternal();
            }
        }
    }

}
