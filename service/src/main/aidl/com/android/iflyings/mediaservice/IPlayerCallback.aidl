// IPlayerCallback.aidl
package com.android.iflyings.mediaservice;

// Declare any non-default types here with import statements
interface IPlayerCallback {

    String dumpPlayer();

    void playProgrammeInfo(String info);

}