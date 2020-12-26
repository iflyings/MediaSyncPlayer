package com.android.iflyings.mediasyncplayer.opengl.transformer;

import android.opengl.Matrix;

import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;

public class RotateLeftTransformer implements MediaTransformer {
    @Override
    public void transformMedia(MediaData mediaData, float position) {
        if ((position > -0.5 && position <= 0.5)) {
            mediaData.setVisible(true);
            float[] modelMatrix = mediaData.lockModelMatrix();
            Matrix.translateM(modelMatrix, 0, 0f, 0f, -5 * Math.abs(position));
            Matrix.rotateM(modelMatrix, 0, 180f * position, 0f, 1f, 0f);
            mediaData.unlockModelMatrix();
        } else {
            mediaData.setVisible(false);
        }
    }
}
