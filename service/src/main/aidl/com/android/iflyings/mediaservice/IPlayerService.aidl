// IPlayerService.aidl
package com.android.iflyings.mediaservice;

// Declare any non-default types here with import statements
 import com.android.iflyings.mediaservice.IPlayerCallback;

interface IPlayerService {

    void register(IPlayerCallback cb);

    void unregister(IPlayerCallback cb);

    String getProgrammeInfo();

}