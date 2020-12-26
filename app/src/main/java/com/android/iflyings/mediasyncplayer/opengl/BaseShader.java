package com.android.iflyings.mediasyncplayer.opengl;

import android.opengl.GLES30;

import androidx.annotation.CallSuper;
import com.android.iflyings.mediasyncplayer.R;

import static com.android.iflyings.mediasyncplayer.opengl.ShaderUtils.assembleProgram;
import static com.android.iflyings.mediasyncplayer.opengl.ShaderUtils.checkError;


public abstract class BaseShader {

    private static final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;
    protected static final String MVP_MATRIX = "uMVPMatrix";
    protected static final String TEX_MATRIX = "uTexMatrix";
    protected static final String TEXTURE_SAMPLER = "uTexSampler";

    private final String mVertexShader;
    private final String mFragmentShader;

    private int mProgramHandle = 0;

    private final int mVerCoordHandle = 0; // layout(location = 0)
    private final int mTexCoordHandle = 1; // layout(location = 1)

    private int mTextureSamplerHandle = -1;
    private int mTexMatrixHandle = -1;
    private int mMVPMatrixHandle = -1;

    private BaseShader(String shaderCode, String fragmentShader) {
        mVertexShader = shaderCode;
        mFragmentShader = fragmentShader;
    }

    @CallSuper
    protected void onCreated(int programHandle) {
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, MVP_MATRIX);
        if (mMVPMatrixHandle == -1) {
            throw new IllegalStateException("glGetUniformLocation mMVPMatrixHandle error!!!");
        }
        mTexMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, TEX_MATRIX);
        if (mTexMatrixHandle == -1) {
            throw new IllegalStateException("glGetUniformLocation mTexMatrixHandle error!!!");
        }
        mTextureSamplerHandle = GLES30.glGetUniformLocation(mProgramHandle, TEXTURE_SAMPLER);
        if (mTextureSamplerHandle == -1) {
            throw new IllegalStateException("glGetUniformLocation mTextureSamplerHandle error!!!");
        }
    }

    public final void create() {
        if (mProgramHandle == 0) {
            mProgramHandle = assembleProgram(mVertexShader, mFragmentShader);
            if (mProgramHandle == 0) {
                throw new IllegalStateException("create program error!!!");
            }
            GLES30.glUseProgram(mProgramHandle);

            onCreated(mProgramHandle);
            checkError();
        }
    }
    @CallSuper
    protected void bindParameter(BaseGLObject glObject) {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, glObject.getVertexBufferId());
        GLES30.glEnableVertexAttribArray(mVerCoordHandle);
        GLES30.glVertexAttribPointer(mVerCoordHandle, 3, GLES30.GL_FLOAT, false, 5 * FLOAT_SIZE, 0);
        GLES30.glEnableVertexAttribArray(mTexCoordHandle);
        GLES30.glVertexAttribPointer(mTexCoordHandle, 2, GLES30.GL_FLOAT, false, 5 * FLOAT_SIZE, 3 * FLOAT_SIZE);
        checkError();
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, glObject.getMVPMatrixData(), 0);
        GLES30.glUniformMatrix4fv(mTexMatrixHandle, 1, false, glObject.getTexMatrixData(), 0);
        checkError();
    }

    protected abstract int drawTexture(int textureHandler, int index, BaseGLObject glObject);

    @CallSuper
    protected void unbindParameter() {
        GLES30.glDisableVertexAttribArray(mVerCoordHandle);
        GLES30.glDisableVertexAttribArray(mTexCoordHandle);
        checkError();
    }

    public final int draw(int index, BaseGLObject glObject) {
        if (mProgramHandle != 0) {

            GLES30.glUseProgram(mProgramHandle);
            checkError();
            bindParameter(glObject);

            index = drawTexture(mTextureSamplerHandle, index, glObject);
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

            unbindParameter();
            checkError();

            return index;
        }
        return index;
    }
    @CallSuper
    protected void onDestroy() { }

    public final void destroy() {
        if (mProgramHandle != 0) {
            GLES30.glDeleteProgram(mProgramHandle);
            mProgramHandle = 0;
        }
        onDestroy();
    }

    private static abstract class EffectShader extends BaseShader {

        private int mParameterHandle = -1;

        private EffectShader(String shaderCode, String fragmentShader) {
            super(shaderCode, fragmentShader);
        }

        @CallSuper
        @Override
        protected void onCreated(int programHandle) {
            super.onCreated(programHandle);

            mParameterHandle = GLES30.glGetUniformLocation(programHandle, "uParameter");
            if (mParameterHandle == -1) {
                throw new IllegalStateException("glGetUniformLocation uParameter error");
            }
        }

        @CallSuper
        @Override
        protected void bindParameter(BaseGLObject glObject) {
            super.bindParameter(glObject);

            BaseGLObject.EffectGLObject effectGLObject = (BaseGLObject.EffectGLObject) glObject;
            float[] parameters = effectGLObject.getParameterData();
            GLES30.glUniform1fv(mParameterHandle,parameters.length, parameters, 0);
        }
    }

    public static class ImageShader extends EffectShader {

        private ImageShader() {
            super(ShaderUtils.readRawTextFile(R.raw.default_vertex_shader),
                    ShaderUtils.readRawTextFile(R.raw.image_fragment_shader) + ShaderUtils.readRawTextFile(R.raw.effect_fragment_shader));
        }

        @Override
        protected int drawTexture(int textureHandler, int index, BaseGLObject glObject) {
            ((BaseGLObject.ImageGLObject) glObject).getImageTexture().draw(textureHandler, index ++);
            return index;
        }
    }

    public static class VideoShader extends EffectShader {

        private VideoShader() {
            super(ShaderUtils.readRawTextFile(R.raw.default_vertex_shader),
                    ShaderUtils.readRawTextFile(R.raw.video_fragment_shader) + ShaderUtils.readRawTextFile(R.raw.effect_fragment_shader));
        }

        @Override
        protected int drawTexture(int textureHandler, int index, BaseGLObject glObject) {
            ((BaseGLObject.VideoGLObject) glObject).getVideoTexture().draw(textureHandler, index ++);
            return index;
        }
    }

    public static class MaskShader extends BaseShader {

        private final int mMaskTextureHandle = 2;// layout(location = 2)
        private int mMaskSamplerHandle = -1;

        MaskShader() {
            super(ShaderUtils.readRawTextFile(R.raw.mask_vertex_shader),
                    ShaderUtils.readRawTextFile(R.raw.mask_fragment_shader));
        }
        @Override
        protected void onCreated(int programHandle) {
            super.onCreated(programHandle);
            mMaskSamplerHandle = GLES30.glGetUniformLocation(programHandle, "uMaskSampler");
            if (mMaskSamplerHandle == -1) {
                throw new IllegalStateException("glGetUniformLocation uMaskSampler error!!!");
            }
        }
        @Override
        protected void bindParameter(BaseGLObject glObject) {
            super.bindParameter(glObject);

            BaseGLObject.MaskGLObject maskGLObject = (BaseGLObject.MaskGLObject) glObject;
            GLES30.glEnableVertexAttribArray(mMaskTextureHandle);
            GLES30.glVertexAttribPointer(mMaskTextureHandle, 2, GLES30.GL_FLOAT, false,
                    2 * FLOAT_SIZE, maskGLObject.getMaskBuffer());
        }

        @Override
        protected int drawTexture(int textureHandler, int index, BaseGLObject glObject) {
            BaseGLObject.MaskGLObject maskGLObject = (BaseGLObject.MaskGLObject) glObject;
            maskGLObject.getMaskTexture().draw(mMaskSamplerHandle, index ++);
            return index;
        }

        @Override
        protected void unbindParameter() {
            super.unbindParameter();
            GLES30.glDisableVertexAttribArray(mMaskTextureHandle);
        }
    }

    public static class ShaderFactory {
        private final static ImageShader mImageShaderInstance = new ImageShader();
        private final static VideoShader mVideoShaderInstance = new VideoShader();
        private final static MaskShader mMaskShaderInstance = new MaskShader();

        public static ImageShader getImageShaderInstance() {
            return mImageShaderInstance;
        }
        public static VideoShader getVideoShaderInstance() {
            return mVideoShaderInstance;
        }
        public static MaskShader getMaskShaderInstance() {
            return mMaskShaderInstance;
        }

        public static void destroy() {
            mImageShaderInstance.destroy();
            mVideoShaderInstance.destroy();
            mMaskShaderInstance.destroy();
        }
    }
}
