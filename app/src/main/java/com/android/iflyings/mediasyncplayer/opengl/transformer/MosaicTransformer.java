package com.android.iflyings.mediasyncplayer.opengl.transformer;

import com.android.iflyings.mediasyncplayer.opengl.component.EffectComponent;
import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;

public class MosaicTransformer implements MediaTransformer {
    @Override
    public void transformMedia(MediaData mediaData, float position) {
        if (position < -0.5) {
            mediaData.setVisible(false);
        } else if (position <= 0) {
            mediaData.setVisible(true);
            mediaData.getComponent(EffectComponent.class).setMosaic(mediaData.getTextureWidth(),mediaData.getTextureHeight(),(int) (100 * -position));
        } else if (position <= 0.5) {
            mediaData.setVisible(true);
            mediaData.getComponent(EffectComponent.class).setMosaic(mediaData.getTextureWidth(),mediaData.getTextureHeight(),(int) (100 * position));
        } else {
            mediaData.setVisible(false);
        }
    }
}