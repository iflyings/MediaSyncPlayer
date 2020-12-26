package com.android.iflyings.mediasyncplayer.sync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLES30;

import com.android.iflyings.glview.GLPlayerView;
import com.android.iflyings.glview.GLThread;
import com.android.iflyings.glview.GLViewRenderer;
import com.android.iflyings.mediasyncplayer.opengl.BaseShader;
import com.android.iflyings.mediasyncplayer.opengl.BlockContext;
import com.android.iflyings.mediasyncplayer.opengl.BlockWindow;
import com.android.iflyings.mediasyncplayer.opengl.ShaderUtils;
import com.android.iflyings.mediasyncplayer.impl.DebugPlayerImpl;
import com.android.iflyings.mediasyncplayer.info.BlockInfo;
import com.android.iflyings.mediasyncplayer.info.WindowInfo;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("ViewConstructor")
public class SyncPlayerView extends GLPlayerView implements BlockContext, DebugPlayerImpl {

    private final WindowInfo mWindowInfo;
    private final List<BlockWindow> mBlockWindowList;

    public SyncPlayerView(Context context, WindowInfo windowInfo) {
        super(context);
        mWindowInfo = windowInfo;
        mBlockWindowList = new ArrayList<>(windowInfo.getSize());
        for (BlockInfo blockInfo : windowInfo.getBlockList()) {
            mBlockWindowList.add(new BlockWindow(this, blockInfo));
        }

        setRenderer(new SyncPlayerRender());
    }

    private void startPlayer() {
        for (BlockWindow blockWindow : mBlockWindowList) {
            blockWindow.create();
        }
    }
    private void stopPlayer() {
        for (BlockWindow blockWindow : mBlockWindowList) {
            blockWindow.destroy();
        }
        queueEvent(BaseShader.ShaderFactory::destroy);
    }

    @Override
    public String dumpPlayer() {
        StringBuilder sb = new StringBuilder();
        sb.append("SyncPlayerView name = ").append(mWindowInfo.getWindowName()).append("\n");
        for (BlockWindow blockWindow : mBlockWindowList) {
            sb.append("    ").append(blockWindow).append("\n");
        }
        return sb.toString();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopPlayer();
        super.onDetachedFromWindow();
    }

    @Override
    public void runInGLThread(Runnable runnable) {
        queueEvent(runnable);
    }

    @Override
    protected int getRenderMode() {
        return GLThread.RENDERMODE_CONTINUOUSLY;
    }

    class SyncPlayerRender implements GLViewRenderer {
        @Override
        public void onSurfaceCreated() {
            GLES30.glEnable(GLES30.GL_BLEND);
            //glEnable(GL_DEPTH_TEST)
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
            ShaderUtils.checkError();
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
            startPlayer();
        }

        @Override
        public void onDrawFrame() {
            GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);
            GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            int index = 0;
            for (BlockWindow blockWindow : mBlockWindowList) {
                GLES30.glViewport(blockWindow.getBlockLeft(), blockWindow.getWindowHeight() - blockWindow.getBlockTop() - blockWindow.getBlockHeight(),
                        blockWindow.getBlockWidth(), blockWindow.getBlockHeight());
                index = blockWindow.draw(index);
            }
            ShaderUtils.checkError();
        }
    }
}
