package com.android.iflyings.mediasyncplayer.opengl.transformer;

import com.android.iflyings.mediasyncplayer.opengl.component.EffectComponent;
import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;

public class CutInTransformer implements MediaTransformer {
    @Override
    public void transformMedia(MediaData mediaData, float position) {
        if (position >= 0 && position <= 1) {
            mediaData.getComponent(EffectComponent.class).cutOff(mediaData.getBlockWidth() * position / 2,
                    mediaData.getBlockHeight() * position / 2,mediaData.getBlockWidth() * (1 - position / 2),
                    mediaData.getBlockHeight() * (1 - position / 2));
        }
    }
}