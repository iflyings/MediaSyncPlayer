package com.android.iflyings.mediasyncplayer.opengl.transformer;

import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;

public class MoveBottomTransformer implements MediaTransformer {
    @Override
    public void transformMedia(MediaData mediaData, float position) {
        mediaData.translate(0f, -position);
    }
}
