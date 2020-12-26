package com.android.iflyings.mediasyncplayer.opengl.transformer;

import com.android.iflyings.mediasyncplayer.opengl.component.EffectComponent;
import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;

public class CircleTransformer implements MediaTransformer {
    @Override
    public void transformMedia(MediaData mediaData, float position) {
        if (position >= 0 && position <= 1) {
            double radius = Math.sqrt(mediaData.getBlockWidth() * mediaData.getBlockWidth() +
                    mediaData.getBlockHeight() * mediaData.getBlockHeight()) / 2;
            mediaData.getComponent(EffectComponent.class).setCircle(mediaData.getBlockWidth() / 2f,
                    mediaData.getBlockHeight() / 2f, (1 - position) * (float) radius);
        }
    }
}
