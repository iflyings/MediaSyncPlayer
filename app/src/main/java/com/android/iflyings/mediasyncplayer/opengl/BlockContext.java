package com.android.iflyings.mediasyncplayer.opengl;


public interface BlockContext {

    void runInGLThread(Runnable runnable);

    void requestRender();

}
