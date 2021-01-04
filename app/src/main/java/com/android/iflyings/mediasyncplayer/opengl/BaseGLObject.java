package com.android.iflyings.mediasyncplayer.opengl;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.Matrix;

import androidx.annotation.CallSuper;

import com.android.iflyings.mediasyncplayer.opengl.data.ImageLoader;
import com.android.iflyings.mediasyncplayer.opengl.data.TextLoader;
import com.android.iflyings.mediasyncplayer.opengl.data.VideoLoader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static com.android.iflyings.mediasyncplayer.opengl.ShaderUtils.checkError;


public class BaseGLObject {

    private static final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;

    private final FloatBuffer mVertexBuffer;
    private final float[] mTexMatrixData;
    private final float[] mMVPMatrixData;

    private int mVertexBufferId = 0;

    protected BaseGLObject() {
        mVertexBuffer = ByteBuffer.allocateDirect(20 * FLOAT_SIZE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        float[] vertexData = new float[]{
                1f, 1f, 0f, 1f, 0f,
                -1f, 1f, 0f, 0f, 0f,
                1f, -1f, 0f, 1f, 1f,
                -1f, -1f, 0f, 0f, 1f,
        };
        swapVertexData(vertexData);
        mVertexBuffer.put(vertexData);
        mVertexBuffer.position(0);

        mTexMatrixData = new float[16];
        Matrix.setIdentityM(mTexMatrixData, 0);
        mMVPMatrixData = new float[16];
        Matrix.setIdentityM(mMVPMatrixData, 0);
    }

    private void swapVertexData(float[] textureData) {
        if (!(this instanceof VideoGLObject)) {
            return;
        }
        float tmp;
        tmp = textureData[3]; textureData[3] = textureData[13]; textureData[13] = tmp;
        tmp = textureData[4]; textureData[4] = textureData[14]; textureData[14] = tmp;
        tmp = textureData[8]; textureData[8] = textureData[18]; textureData[18] = tmp;
        tmp = textureData[9]; textureData[9] = textureData[19]; textureData[19] = tmp;
    }

    public float[] getTexMatrixData() {
        return mTexMatrixData;
    }
    public float[] getMVPMatrixData() {
        synchronized (mMVPMatrixData) {
            return mMVPMatrixData;
        }
    }
    public void setMVPMatrixData(float[] matrix) {
        synchronized (mMVPMatrixData) {
            System.arraycopy(matrix, 0, mMVPMatrixData, 0, 16);
        }
    }

    public int getVertexBufferId() {
        return mVertexBufferId;
    }

    @CallSuper
    public void reset() {
        Matrix.setIdentityM(mMVPMatrixData, 0);
    }

    public final void create() {
        if (mVertexBufferId == 0) {
            mVertexBufferId = ShaderUtils.glGenBuffers(mVertexBuffer, FLOAT_SIZE);
            if (mVertexBufferId == 0) {
                throw new IllegalStateException("create array buffer error!!!");
            }
            checkError();
            onCreated();
        }
    }
    public final int draw(int index) {
        return onDraw(index);
    }
    public final void destroy() {
        if (mVertexBufferId != 0) {
            ShaderUtils.glDeleteBuffers(mVertexBufferId);
            mVertexBufferId = 0;

            onDestroy();
        }
    }

    protected void onCreated() {}
    protected int onDraw(int index) {
        return index;
    }
    protected void onDestroy() {}

    public static class EffectGLObject extends BaseGLObject {
        private final float[] mParameterData;

        EffectGLObject() {
            mParameterData = new float[5];
            reset();
        }
        @Override
        public void reset() {
            super.reset();
            mParameterData[0] = 100.0f;
        }
        public void setCircle(float centerX, float centerY, float radius) {
            mParameterData[0] = 0.5f;
            mParameterData[1] = centerX;
            mParameterData[2] = centerY;
            mParameterData[3] = radius;
        }
        public void setMosaic(int texWidth,int texHeight,int size) {
            mParameterData[0] = 1.5f;
            mParameterData[1] = (float) texWidth;
            mParameterData[2] = (float) texHeight;
            mParameterData[3] = (float) size;
        }
        public void cutOff(float l, float t, float r, float b) {
            mParameterData[0] = 2.5f;
            mParameterData[1] = l;
            mParameterData[2] = r;
            mParameterData[3] = t;
            mParameterData[4] = b;
        }
        public void setHShutter(float blockSize, float showSize) {
            mParameterData[0] = 3.5f;
            mParameterData[1] = blockSize;
            mParameterData[2] = showSize;
        }
        public void setVShutter(float blockSize, float showSize) {
            mParameterData[0] = 4.5f;
            mParameterData[1] = blockSize;
            mParameterData[2] = showSize;
        }
        public void setHVShutter(float blockSize, float showSize) {
            mParameterData[0] = 5.5f;
            mParameterData[1] = blockSize;
            mParameterData[2] = showSize;
        }
        public void setBrightThreshold(float bright) {
            mParameterData[0] = 6.5f;
            mParameterData[1] = bright;
        }
        public void setAlpha(float alpha) {
            mParameterData[0] = 7.5f;
            mParameterData[1] = alpha;
        }

        public float[] getParameterData() {
            return mParameterData;
        }
    }

    public static class ImageGLObject extends EffectGLObject implements
            ImageLoader.ImageGLObjectImpl, TextLoader.TextGLObjectImpl {

        private final BaseShader.ImageShader mImageShader;
        private final BaseTexture.ImageTexture mImageTexture;

        public BaseTexture.ImageTexture getImageTexture() {
            return mImageTexture;
        }

        public ImageGLObject() {
            mImageShader = BaseShader.ShaderFactory.getImageShaderInstance();
            mImageTexture = new BaseTexture.ImageTexture();
        }

        public void loadBitmap(Bitmap bitmap) {
            mImageTexture.load(bitmap, getTexMatrixData());
            ShaderUtils.checkError();
        }

        public void updateBitmap(Bitmap bitmap) {
            mImageTexture.update(bitmap);
            ShaderUtils.checkError();
        }

        @CallSuper
        @Override
        protected void onCreated() {
            mImageShader.create();
            mImageTexture.create();
        }

        @CallSuper
        @Override
        protected int onDraw(int index) {
            index = mImageShader.draw(index, this);
            return index;
        }

        @CallSuper
        @Override
        protected void onDestroy() {
            mImageTexture.destroy();
        }
    }

    public static class VideoGLObject extends EffectGLObject implements VideoLoader.VideoGLObjectImpl {
        private final BaseShader.VideoShader mVideoShader;
        private final BaseTexture.VideoTexture mVideoTexture;

        public BaseTexture.VideoTexture getVideoTexture() {
            return mVideoTexture;
        }

        public VideoGLObject() {
            mVideoShader = BaseShader.ShaderFactory.getVideoShaderInstance();
            mVideoTexture = new BaseTexture.VideoTexture();
        }

        @Override
        protected void onCreated() {
            mVideoShader.create();
            mVideoTexture.create();
        }
        @Override
        protected int onDraw(int index) {
            index = mVideoShader.draw(index, this);
            return index;
        }
        @Override
        protected void onDestroy() {
            mVideoTexture.destroy();
        }

        public SurfaceTexture getSurfaceTexture() {
            return new SurfaceTexture(mVideoTexture.mTextureId);
        }
    }

    public static class MaskGLObject extends BaseGLObject implements VideoLoader.VideoGLObjectImpl,
            ImageLoader.ImageGLObjectImpl {

        private final BaseShader.MaskShader mMaskShader;
        private final BaseTexture.ImageTexture mMaskTexture;
        private final BaseTexture.VideoTexture mVideoTexture;
        private final FloatBuffer mMaskBuffer;

        public BaseTexture.ImageTexture getMaskTexture() {
            return mMaskTexture;
        }
        public FloatBuffer getMaskBuffer() {
            return mMaskBuffer;
        }

        public MaskGLObject() {
            mMaskShader = BaseShader.ShaderFactory.getMaskShaderInstance();
            mMaskTexture = new BaseTexture.ImageTexture();
            mVideoTexture = new BaseTexture.VideoTexture();
            final float[] textureData = new float[] {
                    1f, 0f,
                    0f, 0f,
                    1f, 1f,
                    0f, 1f,
            };
            mMaskBuffer = ByteBuffer.allocateDirect(12 * FLOAT_SIZE)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mMaskBuffer.put(textureData);
            mMaskBuffer.position(0);
        }

        @Override
        protected void onCreated() {
            mMaskShader.create();
            mMaskTexture.create();
            mVideoTexture.create();
        }

        @Override
        protected int onDraw(int index) {
            index = mMaskShader.draw(index, this);
            return index;
        }

        @Override
        protected void onDestroy() {
            mMaskTexture.destroy();
            mVideoTexture.destroy();
        }

        public SurfaceTexture getSurfaceTexture() {
            return new SurfaceTexture(mVideoTexture.mTextureId);
        }

        @Override
        public void loadBitmap(Bitmap bitmap) {
            mMaskTexture.load(bitmap, getTexMatrixData());
            ShaderUtils.checkError();
        }
    }
}
