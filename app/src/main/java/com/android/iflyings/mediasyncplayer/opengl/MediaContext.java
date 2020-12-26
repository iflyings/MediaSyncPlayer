package com.android.iflyings.mediasyncplayer.opengl;

import android.os.Looper;

import com.android.iflyings.mediasyncplayer.opengl.data.MediaData;


public interface MediaContext {

    void onCompletion(MediaData md);

    void onError(MediaData md, int type, String msg);

    void runInGLThread(Runnable runnable);

    Looper getUserThreadLooper();

    void requestRender();
}
