package com.android.iflyings.mediasyncplayer.multi;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.android.iflyings.mediasyncplayer.opengl.BaseShader;
import com.android.iflyings.mediasyncplayer.opengl.BlockContext;
import com.android.iflyings.mediasyncplayer.opengl.BlockWindow;
import com.android.iflyings.mediasyncplayer.opengl.ShaderUtils;
import com.android.iflyings.mediasyncplayer.impl.DebugPlayerImpl;
import com.android.iflyings.mediasyncplayer.info.BlockInfo;
import com.android.iflyings.mediasyncplayer.info.WindowInfo;
import com.android.iflyings.mediasyncplayer.opengl.data.DecorativeTextData;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


@SuppressLint("ViewConstructor")
public class MultiPlayerView extends GLSurfaceView implements BlockContext, DebugPlayerImpl {

    private final WindowInfo mWindowInfo;
    private final List<BlockWindow> mBlockWindowList;

    public MultiPlayerView(Context context, WindowInfo windowInfo) {
        super(context);
        mWindowInfo = windowInfo;
        mBlockWindowList = new ArrayList<>(windowInfo.getSize());
        for (BlockInfo blockInfo : windowInfo.getBlockList()) {
            mBlockWindowList.add(new BlockWindow(this, blockInfo));
        }
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        String v = info.getGlEsVersion(); //判断是否为3.0 ，一般4.4就开始支持3.0版本了。
        if (v.startsWith("3.")) {
            setEGLContextClientVersion(3);
        } else {
            setEGLContextClientVersion(2);
        }
        setRenderer(new MultiPlayerRender());
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public String dumpPlayer() {
        StringBuilder sb = new StringBuilder();
        sb.append("MultiPlayerView name = ").append(mWindowInfo.getWindowName()).append("\n");
        for (BlockWindow blockWindow : mBlockWindowList) {
            sb.append("    ").append(blockWindow.dumpPlayer()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void onDetachedFromWindow() {
        stopPlayer();
        super.onDetachedFromWindow();
    }

    @Override
    public void runInGLThread(Runnable runnable) {
        queueEvent(runnable);
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

    class MultiPlayerRender implements Renderer {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //GLES30.glEnable(GLES30.GL_DEPTH_TEST);
            GLES30.glEnable(GLES30.GL_BLEND);
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
            ShaderUtils.checkError();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            startPlayer();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);
            //GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES30.glClearDepthf(1.0f);
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
