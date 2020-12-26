package com.android.iflyings.glview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


public class GLPlayerView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "GLPlayerView";

    protected GLThread mGLThread;
    protected GLThread.Builder glThreadBuilder;
    private final List<Runnable> cacheEvents = new ArrayList<>();
    private SurfaceTextureListener surfaceTextureListener;
    private GLThread.OnCreateGLContextListener onCreateGLContextListener;

    private boolean surfaceAvailable = false;
    private GLViewRenderer renderer;

    public GLPlayerView(Context context) {
        super(context);
        init();
    }

    public GLPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GLPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // TODO: 2018/3/25 This may be duplicated. onSurfaceTextureSizeChanged is doing same thing.
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged: ");
        super.onSizeChanged(w, h, oldw, oldh);
        if (mGLThread != null) {
            mGLThread.onWindowResize(w, h);
        }
    }

    public void onPause() {
        if (mGLThread != null) {
            mGLThread.onPause();
        }
    }

    public void onResume() {
        if (mGLThread != null) {
            mGLThread.onResume();
        }
    }

    public void queueEvent(Runnable r) {
        if (mGLThread == null) {
            cacheEvents.add(r);
            return;
        }
        mGLThread.queueEvent(r);
    }

    public void requestRender() {
        if (mGLThread != null) {
            mGLThread.requestRender();
        } else {
            Log.w(TAG, "GLThread is not created when requestRender");
        }
    }

    /**
     * Wait until render command is sent to OpenGL
     */
    public void requestRenderAndWait() {
        if (mGLThread != null) {
            mGLThread.requestRenderAndWait();
        } else {
            Log.w(TAG, "GLThread is not created when requestRender");
        }
    }

    protected void surfaceCreated() {
        mGLThread.surfaceCreated();
    }

    protected void surfaceDestroyed() {
        // Surface will be destroyed when we return
        if (mGLThread != null) {
            mGLThread.surfaceDestroyed();
            mGLThread.requestExitAndWait();
        }
        surfaceAvailable = false;
        mGLThread = null;
    }

    protected void surfaceChanged(int w, int h) {
        mGLThread.onWindowResize(w, h);
    }

    protected void surfaceRedrawNeeded() {
        if (mGLThread != null) {
            mGLThread.requestRenderAndWait();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow: ");
        if (mGLThread != null) {
            mGLThread.requestExitAndWait();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mGLThread != null) {
                // GLThread may still be running if this view was never
                // attached to a window.
                mGLThread.requestExitAndWait();
            }
        } finally {
            super.finalize();
        }
    }

    protected void init() {
        super.setSurfaceTextureListener(this);
    }

    /**
     * @return If the context is not created, then EGL10.EGL_NO_CONTEXT will be returned.
     */
    @Nullable
    public EglContextWrapper getCurrentEglContext() {
        return mGLThread == null ? null : mGLThread.getEglContext();
    }

    public void setOnCreateGLContextListener(GLThread.OnCreateGLContextListener onCreateGLContextListener) {
        this.onCreateGLContextListener = onCreateGLContextListener;
    }

    public void setSurfaceTextureListener(SurfaceTextureListener surfaceTextureListener) {
        this.surfaceTextureListener = surfaceTextureListener;
    }

    protected int getRenderMode() {
        return GLThread.RENDERMODE_WHEN_DIRTY;
    }

    public void setRenderer(GLViewRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: ");
        surfaceAvailable = true;
        glThreadBuilder = new GLThread.Builder();
        if (mGLThread == null) {
            glThreadBuilder.setRenderMode(getRenderMode())
                    .setSurface(surface)
                    .setRenderer(renderer);
            createGLThread();

        } else {
            mGLThread.setSurface(surface);
            freshSurface(width, height);
        }
        if (surfaceTextureListener != null) {
            surfaceTextureListener.onSurfaceTextureAvailable(surface, width, height);
        }
    }

    protected void createGLThread() {
        Log.d(TAG, "createGLThread: ");
        if (!surfaceAvailable) {
            return;
        }
        mGLThread = glThreadBuilder.createGLThread();
        mGLThread.setOnCreateGLContextListener(new GLThread.OnCreateGLContextListener() {
            @Override
            public void onCreate(final EglContextWrapper eglContext) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (onCreateGLContextListener != null) {
                            onCreateGLContextListener.onCreate(eglContext);
                        }
                    }
                });
            }
        });
        mGLThread.start();
        freshSurface(getWidth(), getHeight());
        for (Runnable cacheEvent : cacheEvents) {
            mGLThread.queueEvent(cacheEvent);
        }
        cacheEvents.clear();
    }

    /**
     * surface inited or updated.
     */
    private void freshSurface(int width, int height) {
        surfaceCreated();
        surfaceChanged(width, height);
        surfaceRedrawNeeded();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureSizeChanged: ");
        surfaceChanged(width, height);
        surfaceRedrawNeeded();
        if (surfaceTextureListener != null) {
            surfaceTextureListener.onSurfaceTextureSizeChanged(surface, width, height);
        }
    }

    /**
     * This will be called when windows detached. Activity onStop will cause window detached.
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed: ");
        surfaceDestroyed();
        if (surfaceTextureListener != null) {
            surfaceTextureListener.onSurfaceTextureDestroyed(surface);
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (surfaceTextureListener != null) {
            surfaceTextureListener.onSurfaceTextureUpdated(surface);
        }
    }
}
