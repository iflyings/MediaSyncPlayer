package com.android.iflyings.mediasyncplayer.opengl.transformer;

import android.opengl.Matrix;

import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;

public class CubeVTransformer implements MediaTransformer {
    @Override
    public void transformMedia(MediaData mediaData, float position) {
        if (position > -1 && position <= 0) {
            float[] modelMatrix = mediaData.lockModelMatrix();

            Matrix.translateM(modelMatrix,0,0f,2f * position + 1,0f);
            Matrix.rotateM(modelMatrix,0,-90f * position,1f,0f,0f);
            Matrix.translateM(modelMatrix,0,0f,-1f,0f);

            mediaData.unlockModelMatrix();
        } else if (position > 0 && position < 0.83) {
            float[] modelMatrix = mediaData.lockModelMatrix();

            Matrix.translateM(modelMatrix,0,0f,2f * position - 1,0f);
            Matrix.rotateM(modelMatrix,0,-90f * position,1f,0f,0f);
            Matrix.translateM(modelMatrix,0,0f,1f,0f);

            mediaData.unlockModelMatrix();
        } else if (position > 0.83) {
            mediaData.setVisible(false);
        }
    }
}
