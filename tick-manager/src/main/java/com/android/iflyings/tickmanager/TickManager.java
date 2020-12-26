package com.android.iflyings.tickmanager;

public class TickManager {

    public native int nativeOpen(int ip);
    public native void nativeClose();
    public native static boolean nativeIsSyncReady();
    public native static boolean nativeIsMasterDevice();
    public native static long nativeGetNetworkTickUs();
    public native static void nativeSleepTo(long tickUs);

    static {
        System.loadLibrary("sync-manager-jni");
    }
}
