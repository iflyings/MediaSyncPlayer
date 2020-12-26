package com.android.iflyings.mediasyncplayer.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.RemoteException;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.android.iflyings.mediasyncplayer.info.SourceInfo;
import com.android.iflyings.mediasyncplayer.info.WindowInfo;
import com.hisilicon.android.tv.ITvSource;
import com.hisilicon.android.tv.TvManager;
import com.hisilicon.android.tv.TvSourceManager;
import com.hisilicon.android.tv.vo.HSRectInfo;


@SuppressLint("ViewConstructor")
public class SourcePlayerView extends SurfaceView implements SurfaceHolder.Callback {
    private final WindowInfo mWindowInfo;

    public SourcePlayerView(Context context, WindowInfo windowInfo) {
        super(context);
        mWindowInfo = windowInfo;
        init();
    }

    private void init() {
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);
    }

    private void setWindowRect(Rect rect) {
        try {
            ITvSource service = TvManager.getInstance().getTvSource();
            HSRectInfo hsRect = new HSRectInfo();
            hsRect.setX(rect.left);
            hsRect.setY(rect.top);
            hsRect.setW(rect.width());
            hsRect.setH(rect.height());
            service.setWindowRect(hsRect, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        SourceInfo si = (SourceInfo) mWindowInfo.getBlockInfo(0).getMediaInfo(0);
        TvSourceManager.getInstance().setSourceHolder("SourcePlayerView");
        TvSourceManager.getInstance().selectSource(si.getSourceType(), 0);
        TvSourceManager.getInstance().setFullWindow(false);
        setWindowRect(mWindowInfo.getWindowRect());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        SourceInfo si = (SourceInfo) mWindowInfo.getBlockInfo(0).getMediaInfo(0);
        TvSourceManager.getInstance().deselectSource(si.getSourceType(), true);
        TvSourceManager.getInstance().selectSource(SourceInfo.SOURCE_MEDIA, 0);
    }
}
