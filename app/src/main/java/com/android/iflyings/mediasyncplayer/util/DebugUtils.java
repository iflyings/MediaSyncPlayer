package com.android.iflyings.mediasyncplayer.util;

import android.os.SystemClock;

import com.android.iflyings.mediasyncplayer.BuildConfig;


public class DebugUtils {

    private static long mStartTickMs = 0;

    public static void startDebug() {
        mStartTickMs = SystemClock.currentThreadTimeMillis();
    }
    public static void stopDebug(int tickMs, String tag) {
        if (BuildConfig.DEBUG && SystemClock.currentThreadTimeMillis() - mStartTickMs > tickMs) {
            throw new IllegalStateException(tag + "->function call is timeout!!!");
        }
    }
}
