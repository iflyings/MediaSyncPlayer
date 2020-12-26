package com.android.iflyings.mediasyncplayer.opengl.transformer;

import com.android.iflyings.mediasyncplayer.opengl.component.EffectComponent;
import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;

public class MeltUpTransformer implements MediaTransformer {
    @Override
    public void transformMedia(MediaData mediaData, float position) {
        if (position >= 0 && position <= 1) {
            mediaData.getComponent(EffectComponent.class).setBrightThreshold(1 - position);
        }
    }
}
