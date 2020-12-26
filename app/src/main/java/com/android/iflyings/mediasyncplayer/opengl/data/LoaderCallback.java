package com.android.iflyings.mediasyncplayer.opengl.data;


public interface LoaderCallback {

    void notifyMediaRender();

    void runInGLThread(Runnable runnable);

    void notifyMediaSize(int width, int height);

    void notifyMediaError(int type, String message);

    void notifyMediaCompletion();

    long getStartTickUs();
}
