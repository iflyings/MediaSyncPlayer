package com.android.iflyings.mediasyncplayer.sync;

import android.util.Log;

import com.android.iflyings.mediasyncplayer.impl.SyncPlayerImpl;
import com.android.iflyings.tickmanager.TickManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


public class SyncManager {
    private static final int UDP_PORT = 16347;
    private static final int UDP_TIMEOUT_MS = 500;

    private static final SyncManager instance = new SyncManager();

    private SyncThread mSyncThread;
    private TickManager mTickManager;

    private SyncManager() {
    }

    public static SyncManager getInstance() {
        return instance;
    }

    public int open(String localIP) {
        if (localIP == null) {
            return 0;
        }

        String[] s_ip = localIP.split("\\.");
        if (s_ip.length != 4) {
            return -1;
        }
        int[] intAddresses = new int[] {
                Integer.parseInt(s_ip[0]),
                Integer.parseInt(s_ip[1]),
                Integer.parseInt(s_ip[2]),
                Integer.parseInt(s_ip[3]),
        };

        try {
            InetAddress broadcastIp = InetAddress.getByAddress(new byte[] {(byte)intAddresses[0],
                    (byte)intAddresses[1],(byte)intAddresses[2],(byte)0xFF});
            mSyncThread = new SyncThread(broadcastIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return -1;
        }

        mTickManager = new TickManager();
        int address = (intAddresses[0] << 24) | (intAddresses[1] << 16) |
                (intAddresses[2] << 8) | (intAddresses[3]);
        if (0 > mTickManager.nativeOpen(address)) {
            mTickManager = null;
            return -1;
        }

        return 0;
    }
    public void close() {
        if (mSyncThread != null) {
            mSyncThread.stopThread();
            mSyncThread = null;
        }
        if (mTickManager != null) {
            mTickManager.nativeClose();
            mTickManager = null;
        }
    }

    public void startSync(SyncPlayerImpl sp) {
        //mSyncThread.startThread(sp);
    }

    public boolean IsSyncReady() {
        return mTickManager == null || TickManager.nativeIsSyncReady();
    }

    public static long getNetWorkTickUs() {
        return TickManager.nativeGetNetworkTickUs();
    }

    public static void sleepToNetWorkTickUs(long tickUs) {
        TickManager.nativeSleepTo(tickUs);
    }

    private static class SyncThread extends Thread {

        private final byte[] mBuffer = new byte[128];

        private InetAddress mBroadcastIp;
        private DatagramSocket mUdpSocket;
        private WeakReference<SyncPlayerImpl> mSyncPlayer;

        private volatile boolean isThreadRunning = false;

        SyncThread(InetAddress broadcastIp) {
            mBroadcastIp = broadcastIp;
        }

        private int startThread(SyncPlayerImpl sp) {
            if (!isThreadRunning) {
                try {
                    mUdpSocket = new DatagramSocket(UDP_PORT);
                    mUdpSocket.setSoTimeout(UDP_TIMEOUT_MS);
                } catch (SocketException e) {
                    e.printStackTrace();
                    return -1;
                }
                isThreadRunning = true;
                start();
            }
            mSyncPlayer = new WeakReference<>(sp);
            return 0;
        }
        private void stopThread() {
            isThreadRunning = false;
        }

        private void sendStartInfo() {
            try {
                SyncPlayerImpl sp = mSyncPlayer.get();
                if (sp != null) {
                    JSONObject so = sp.getStartInfo();
                    if (so != null) {
                        JSONObject mo = new JSONObject();
                        mo.put("cmd", "start");
                        mo.put("data", so);
                        byte[] bytes = mo.toString().getBytes();
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, mBroadcastIp, UDP_PORT);
                        mUdpSocket.send(packet);
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
        private void sendUpdateInfo() {
            try {
                SyncPlayerImpl sp = mSyncPlayer.get();
                if (sp != null) {
                    JSONObject uo = sp.getUpdateInfo();
                    if (uo != null) {
                        JSONObject mo = new JSONObject();
                        mo.put("cmd", "update");
                        mo.put("data", uo);
                        byte[] bytes = mo.toString().getBytes();
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, mBroadcastIp, UDP_PORT);
                        mUdpSocket.send(packet);
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }

        private int command(DatagramPacket pack) throws JSONException {
            String message = new String(pack.getData(), 0, pack.getLength());
            JSONObject jsonObject = new JSONObject(message);
            SyncPlayerImpl sp = mSyncPlayer.get();
            if (sp != null) {
                String cmd = jsonObject.getString("cmd");
                if ("start".equals(cmd) && !TickManager.nativeIsMasterDevice()) {
                    return sp.setStartInfo(jsonObject.getJSONObject("data"));
                } else if ("update".equals(cmd)) {
                    return sp.setUpdateInfo(jsonObject.getJSONObject("data"));
                }
            }
            return 0;
        }

        @Override
        public void run() {
            while (isThreadRunning) {
                try {
                    DatagramPacket pack = new DatagramPacket(mBuffer, mBuffer.length);
                    mUdpSocket.receive(pack);
                    if (0 > command(pack)) {
                        Log.e("zw", "set command error");
                    }
                } catch (SocketTimeoutException e) {
                    //e.printStackTrace();
                    if (TickManager.nativeIsMasterDevice()) {
                        sendStartInfo();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
            mUdpSocket.close();
        }

    }
}
