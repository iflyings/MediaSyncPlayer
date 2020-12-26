package com.android.iflyings.mediasyncplayer.opengl;

import android.os.SystemClock;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;


public class ScrollHelper {
    private final Interpolator mInterpolator = new LinearInterpolator();//BounceInterpolator();

    private boolean mFinished = true;
    private long mStartTime = 0L;
    private int mDuration = 0;
    private float mDurationReciprocal;
    private float mStartPos = 0;
    private float mCurrPos = 0;
    private float mFinalPos = 0;
    private float mDeltaPos = 0L;

    public float getCurrPos() {
        return mCurrPos;
    }

    public void startScroll(float start, float end, int duration) {
        mFinished = false;
        mDuration = duration;
        mStartTime = SystemClock.uptimeMillis();
        mStartPos = start;
        mFinalPos = end;
        mCurrPos = mStartPos;
        mDeltaPos = end - start;
        mDurationReciprocal = 1.0f / duration;
    }

    public void abortAnimation() {
        mCurrPos = mFinalPos;
        mFinished = true;
    }

    public boolean computeScrollOffset() {
        if (mFinished) {
            return false;
        }

        int timePassed = (int)(SystemClock.uptimeMillis() - mStartTime);

        if (timePassed < mDuration) {
            float x = timePassed * mDurationReciprocal;
            x = mInterpolator.getInterpolation(x);
            mCurrPos = mStartPos + x * mDeltaPos;
        }
        else {
            mCurrPos = mFinalPos;
            mFinished = true;
        }
        return true;
    }
}
