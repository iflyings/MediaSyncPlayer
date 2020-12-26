package com.android.iflyings.mediasyncplayer.opengl.transformer;

import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;

public class NoneTransformer implements MediaTransformer {
    @Override
    public void transformMedia(MediaData mediaData, float position) {
        if (position > 0 && position <= 1) {
            mediaData.setVisible(false);
        }
    }
}
