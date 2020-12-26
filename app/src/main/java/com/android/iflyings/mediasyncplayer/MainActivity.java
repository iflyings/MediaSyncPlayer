package com.android.iflyings.mediasyncplayer;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.android.iflyings.mediaservice.BaseActivity;
import com.android.iflyings.mediasyncplayer.info.ProgrammeInfo;
import com.android.iflyings.mediasyncplayer.util.NetworkUtils;
import com.android.iflyings.mediasyncplayer.view.ProgrammeView;
import com.android.iflyings.mediasyncplayer.sync.SyncManager;
import com.android.iflyings.nativeplayer.NativePlayer;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends BaseActivity {

    private ProgrammeView mProgrammeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgrammeView = findViewById(R.id.pv_programme);
        String localIp = NetworkUtils.getIPAddress(this);
        if (localIp == null) {
            Toast.makeText(this, R.string.unable_to_get_local_IP, Toast.LENGTH_SHORT).show();
        }

        /*
        if (0 != SyncManager.getInstance().open(localIp)) {
            Toast.makeText(this, R.string.unable_to_open_sync_thread, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }*/
    }

    @Override
    protected void onStop() {
        SyncManager.getInstance().close();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        mProgrammeView.stopPlayer();
        finish();
        System.exit(0);
    }

    @Override
    public String dumpPlayerInfo() {
        return mProgrammeView.dumpPlayer();
    }

    @Override
    public void playProgramme(String info) {
        try {
            ProgrammeInfo pi = ProgrammeInfo.from(new JSONObject(info));
            mProgrammeView.startPlayer(pi);
        } catch (JSONException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}