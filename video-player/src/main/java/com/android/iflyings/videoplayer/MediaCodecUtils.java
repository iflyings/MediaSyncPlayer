package com.android.iflyings.videoplayer;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;
import android.util.Size;

import java.io.IOException;
import java.util.Arrays;


public class MediaCodecUtils {

    private static final String TAG = "MediaCodecUtils";

    public static MediaCodec getMediaCodec(String mimeType, boolean isSoftwareCodec, boolean isHardwareCodec, int width, int height) {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] codecInfos = mediaCodecList.getCodecInfos();
        MediaCodecInfo codecInfo = null;
        end_found:
        for (MediaCodecInfo info : codecInfos) {
            String codecName = info.getName();
            if (info.isEncoder() || (isSoftwareCodec && !codecName.startsWith("OMX.google")) ||
                    (isHardwareCodec && codecName.startsWith("OMX.google"))) {
                continue;
            }
            //Log.i(TAG, "codecInfo = " + codecName);
            String[] types = info.getSupportedTypes();
            for (String type : types) {
                if (type.equals(mimeType)) {
                    if (width > 0 && height > 0) {
                        try {
                            MediaCodecInfo.CodecCapabilities cc = info.getCapabilitiesForType(mimeType);
                            MediaCodecInfo.VideoCapabilities vc = cc.getVideoCapabilities();
                            if (width >= vc.getSupportedWidths().getLower() && width <= vc.getSupportedWidths().getUpper() &&
                                    height >= vc.getSupportedHeights().getLower() && height <= vc.getSupportedHeights().getUpper()) {
                                codecInfo = info;
                                break end_found;
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            codecInfo = info;
                            break end_found;
                        }
                    } else {
                        codecInfo = info;
                        break end_found;
                    }
                    break;
                }
            }
        }
        if (codecInfo != null) {
            try {
                return MediaCodec.createByCodecName(codecInfo.getName());
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private static MediaCodec createDecoder(String mime) {
        MediaCodec codec;
        if (true) {
            // change to force testing software codecs
            if (mime.contains("avc")) {
                try {
                    codec = MediaCodec.createByCodecName("OMX.google.h264.decoder");
                } catch (IOException e) {
                    return null;
                }
                return codec;
            } else if (mime.contains("3gpp")) {
                try {
                    codec = MediaCodec.createByCodecName("OMX.google.h263.decoder");
                } catch (IOException e) {
                    return null;
                }
                return codec;
            } else if (mime.contains("mp4v")) {
                try {
                    codec = MediaCodec.createByCodecName("OMX.google.mpeg4.decoder");
                } catch (IOException e) {
                    return null;
                }
                return codec;
            } else if (mime.contains("vp8")) {
                try {
                    codec = MediaCodec.createByCodecName("OMX.google.vp8.decoder");
                } catch (IOException e) {
                    return null;
                }
                return codec;
            } else if (mime.contains("vp9")) {
                try {
                    codec = MediaCodec.createByCodecName("OMX.google.vp9.decoder");
                } catch (IOException e) {
                    return null;
                }
                return codec;
            }
        }
        try {
            codec = MediaCodec.createDecoderByType(mime);
        } catch (IOException e) {
            return null;
        }
        return codec;
    }
}
