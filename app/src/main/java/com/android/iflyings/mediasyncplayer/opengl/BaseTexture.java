package com.android.iflyings.mediasyncplayer.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;


public abstract class BaseTexture {
    protected int mTextureId = 0;

    public abstract void create();
    public abstract void draw(int handle, int index);

    public void destroy() {
        if (mTextureId != 0) {
            GLES30.glDeleteTextures(1, new int[] { mTextureId }, 0);
            mTextureId = 0;
        }
    }

    private static int nextPowerOf2(int n) {
        if (n <= 0 || n > (1 << 30))
            throw new IllegalArgumentException("n is invalid: " + n);
        n -= 1;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        n |= n >> 1;
        return n + 1;
    }

    public static class ImageTexture extends BaseTexture {

        private final static int MAX_TEXTURE_SIZE = 4096;

        protected int mTextureWidth = 0;
        protected int mTextureHeight = 0;

        @Override
        public void create() {
            if (mTextureId == 0) {
                int[] textures = new int[1];
                //生成纹理
                GLES30.glGenTextures(1, textures, 0);
                //绑定生成的纹理
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
                //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
                //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

                mTextureId = textures[0];
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            }
        }

        @Override
        public void draw(int handle, int index) {
            if (mTextureId != 0) {
                GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + index);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
                GLES30.glUniform1i(handle, index);
            }
        }

        public void load(Bitmap bitmap, float[] matrix) {
            if (mTextureId != 0) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                mTextureWidth = width > 0 ? nextPowerOf2(width) : 0;
                mTextureHeight = height > 0 ? nextPowerOf2(height) : 0;
                if (mTextureWidth > MAX_TEXTURE_SIZE || mTextureHeight > MAX_TEXTURE_SIZE) {
                    throw new IllegalStateException(String.format("Bitmap is too large: %d x %d", mTextureWidth, mTextureHeight));
                }
                if (mTextureWidth <= 0 || mTextureHeight <= 0) {
                    throw new IllegalStateException("Texture size is error");
                }

                Matrix.setIdentityM(matrix, 0);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
                if (true/*mTextureWidth == bitmap.getWidth() && mTextureHeight == bitmap.getHeight()*/) {
                    GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
                } else {
                    int format = GLUtils.getInternalFormat(bitmap);
                    int type = GLUtils.getType(bitmap);
                    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, format, mTextureWidth, mTextureHeight, 0, format, type, null);
                    GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, bitmap);

                    Matrix.scaleM(matrix, 0, (float) bitmap.getWidth() / mTextureWidth, (float) bitmap.getHeight() / mTextureHeight, 1f);
                }
                ShaderUtils.checkError();
                //解除纹理绑定
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            }
        }

        public void update(Bitmap bitmap) {
            if (mTextureId != 0) {
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
                GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, bitmap);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            }
        }
    }

    public static class VideoTexture extends BaseTexture {

        @Override
        public void create() {
            if (mTextureId == 0) {
                int[] textures = new int[1];
                GLES30.glGenTextures(1, textures, 0);

                GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
                GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
                GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
                mTextureId = textures[0];
                GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
            }
        }

        @Override
        public void draw(int handle, int index) {
            if (mTextureId != 0) {
                GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + index);
                GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
                GLES30.glUniform1i(handle, index);
            }
        }
    }
}
