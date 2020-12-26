package com.android.iflyings.videoplayer;

import android.media.MediaFormat;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;


public class VideoCodecRender extends CodecRender {

    private static final String TAG = VideoCodecRender.class.getSimpleName();
    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_BOTTOM = "crop-bottom";
    private static final String KEY_CROP_TOP = "crop-top";

    private int mWidth;
    private int mHeight;

    private VideoPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    void setOnVideoSizeChangedListener(VideoPlayer.OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
    }
    private VideoPlayer.OnCompletionListener mOnCompletionListener;
    void setOnCompletionListener(VideoPlayer.OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    @Override
    protected String getCodecType() {
        return "video";
    }

    @Override
    protected void onProcessOutputBuffer(ByteBuffer byteBuffer, int size, long presentationTimeUs) {

    }

    @Override
    protected void onFormatChanged(MediaFormat mediaFormat) {
        Log.i(TAG, "onFormatChanged = " + mediaFormat);

        boolean hasCrop = mediaFormat.containsKey(KEY_CROP_RIGHT)
                && mediaFormat.containsKey(KEY_CROP_LEFT) && mediaFormat.containsKey(KEY_CROP_BOTTOM)
                && mediaFormat.containsKey(KEY_CROP_TOP);
        int currentWidth = hasCrop
                ? mediaFormat.getInteger(KEY_CROP_RIGHT) - mediaFormat.getInteger(KEY_CROP_LEFT) + 1
                : mediaFormat.getInteger(android.media.MediaFormat.KEY_WIDTH);
        int currentHeight = hasCrop
                ? mediaFormat.getInteger(KEY_CROP_BOTTOM) - mediaFormat.getInteger(KEY_CROP_TOP) + 1
                : mediaFormat.getInteger(android.media.MediaFormat.KEY_HEIGHT);

        if (mediaFormat.containsKey(MediaFormat.KEY_STRIDE) && mediaFormat.containsKey(MediaFormat.KEY_SLICE_HEIGHT)) {
            //解码后数据对齐的宽高，在有些设备上会返回0
            int keyStride = mediaFormat.getInteger(MediaFormat.KEY_STRIDE);
            int keyStrideHeight = mediaFormat.getInteger(MediaFormat.KEY_SLICE_HEIGHT);
            int bufferSize = 0;
            // 当对齐后高度返回0的时候，分两种情况，如果对齐后宽度有给值，
            // 则只需要计算高度从16字节对齐到128字节对齐这几种情况下哪个值跟对齐后宽度相乘再乘3/2等于对齐后大小，
            // 如果计算不出则默认等于视频宽高。
            // 当对齐后宽度也返回0，这时候也要对宽度做对齐处理，原理同上
            int alignWidth = keyStride;
            int alignHeight = keyStrideHeight;
            if (alignHeight == 0) {
                if (alignWidth == 0) {
                    align:
                    for (int w = 16; w <= 128; w = w << 1) {
                        for (int h = 16; h <= w; h = h << 1) {
                            alignWidth = ((currentWidth - 1) / w + 1) * w;
                            alignHeight = ((currentHeight - 1) / h + 1) * h;
                            int size = alignWidth * alignHeight * 3 / 2;
                            if (size == bufferSize) {
                                break align;
                            }
                        }
                    }
                } else {
                    for (int h = 16; h <= 128; h = h << 1) {
                        alignHeight = ((currentHeight - 1) / h + 1) * h;
                        break;
                    }
                }

                alignWidth = currentWidth;
                alignHeight = currentHeight;
            }

            alignWidth = currentWidth;
            alignHeight = currentHeight;

        }


        /*
        currentPixelWidthHeightRatio = pendingPixelWidthHeightRatio;
        if (Util.SDK_INT >= 21) {
            // On API level 21 and above the decoder applies the rotation when rendering to the surface.
            // Hence currentUnappliedRotation should always be 0. For 90 and 270 degree rotations, we need
            // to flip the width, height and pixel aspect ratio to reflect the rotation that was applied.
            if (pendingRotationDegrees == 90 || pendingRotationDegrees == 270) {
                int rotatedHeight = currentWidth;
                currentWidth = currentHeight;
                currentHeight = rotatedHeight;
                currentPixelWidthHeightRatio = 1 / currentPixelWidthHeightRatio;
            }
        } else {
            // On API level 20 and below the decoder does not apply the rotation.
            currentUnappliedRotationDegrees = pendingRotationDegrees;
        }

*/
        if (mWidth == 0 || mHeight == 0) {
            mWidth = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
            mHeight = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
            if (mWidth > 0 && mHeight > 0 && mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(mWidth, mHeight);
            }
        }
    }

    @Override
    protected void onCompletion() {
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion();
        }
    }
}
