package com.android.iflyings.mediasyncplayer.opengl.component;

import com.android.iflyings.mediasyncplayer.info.MediaInfo;
import com.android.iflyings.mediasyncplayer.opengl.BaseGLObject;
import com.android.iflyings.mediasyncplayer.opengl.transformer.CircleTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.CubeVTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.CutInTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.FadeInOutTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.MediaTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.MeltUpTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.MosaicTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.MoveBottomTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.MoveLeftTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.MoveRightTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.MoveTopTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.NoneTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.RotateLeftTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.RotateTopTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.ShutterHTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.ShutterHVTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.ShutterVTransformer;
import com.android.iflyings.mediasyncplayer.opengl.transformer.ZoomInTransformer;

import java.util.Random;


public class EffectComponent extends Component {

    private final BaseGLObject.EffectGLObject mGLObject;
    private final MediaInfo mMediaInfo;

    public EffectComponent(BaseGLObject.EffectGLObject glObject, MediaInfo mediaInfo) {
        mGLObject = glObject;
        mMediaInfo = mediaInfo;
    }

    public void reset() {
        mGLObject.reset();
    }
    public void setAlpha(float alpha) {
        mGLObject.setAlpha(alpha);
    }
    public void setCircle(float x, float y, float radius) {
        float centerX = mMediaInfo.getBlockLeft() + x;
        float centerY = mMediaInfo.getWindowHeight() - mMediaInfo.getBlockTop() - y;
        mGLObject.setCircle(centerX, centerY, radius);
    }
    public void cutOff(float l, float t, float r, float b) {
        float left = mMediaInfo.getBlockLeft() + l;
        float right = mMediaInfo.getBlockLeft() + r;
        float top = mMediaInfo.getWindowHeight() - mMediaInfo.getBlockTop() - b;
        float bottom = mMediaInfo.getWindowHeight() - mMediaInfo.getBlockTop() - t;
        mGLObject.cutOff(left, top, right, bottom);
    }
    public void setHShutter(float blockSize, float showSize) {
        mGLObject.setHShutter(blockSize, showSize);
    }
    public void setVShutter(float blockSize, float showSize) {
        mGLObject.setVShutter(blockSize, showSize);
    }
    public void setHVShutter(float blockSize, float showSize) {
        mGLObject.setHVShutter(blockSize, showSize);
    }
    public void setBrightThreshold(float bright) {
        mGLObject.setBrightThreshold(bright);
    }
    public void setMosaic(int texWidth,int texHeight,int size) {
        mGLObject.setMosaic(texWidth,texHeight, size);
    }

    public MediaTransformer getMediaTransformer() {
        int type = mMediaInfo.getAnimationType();
        if (type <= 0 || type > 16) {
            type = new Random().nextInt(16) + 1;
        }
        switch (type) {
            case 0: return new NoneTransformer();
            case 16: return new MosaicTransformer();
            case 15: return new CubeVTransformer();
            case 14: return new CircleTransformer();
            case 13: return new MeltUpTransformer();
            case 12: return new ShutterHVTransformer();
            case 11: return new ShutterVTransformer();
            case 10: return new ShutterHTransformer();
            case 9: return new CutInTransformer();
            case 8: return new FadeInOutTransformer();
            case 7: return new ZoomInTransformer();
            case 6: return new RotateLeftTransformer();
            case 5: return new RotateTopTransformer();
            case 4: return new MoveBottomTransformer();
            case 3: return new MoveTopTransformer();
            case 2: return new MoveRightTransformer();
            case 1: return new MoveLeftTransformer();
            default: return new NoneTransformer();
        }
    }
}
