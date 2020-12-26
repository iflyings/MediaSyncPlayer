package com.android.iflyings.mediasyncplayer.opengl.transformer;

import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;

public interface MediaTransformer {

    /*
     * position : 从 -1 --> 0 开始显示，从 0 --> 1 移除显示
     */
    void transformMedia(MediaData mediaData, float position);

}
