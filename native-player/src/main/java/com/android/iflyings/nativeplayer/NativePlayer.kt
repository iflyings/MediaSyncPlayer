package com.android.iflyings.nativeplayer

import android.view.Surface
import java.io.IOException


class NativePlayer {

    private var mNativeContext: Long = 0L

    private var mOnCompletionListener: OnCompletionListener? = null

    init {
        create()
    }

    external fun setSoftwareCodec(soft: Boolean)
    external fun setSurface(surface: Surface)
    external fun setDataSource(filePath: String)
    //@Throws(CodecException::class)
    private external fun create()
    @Throws(CodecException::class)
    external fun prepare(): Int
    //@Throws(CodecException::class)
    external fun start()
    //@Throws(CodecException::class)
    external fun renderOneFrame()
    //@Throws(CodecException::class)
    external fun pause()
    //@Throws(CodecException::class)
    external fun release()

    private fun onNativeCallBack(type: Int, action: Int) {
        if (type == 0) {
            mOnCompletionListener?.onCompletion()
        }
    }

    fun setOnCompletionListener(l: OnCompletionListener) {
        mOnCompletionListener = l
    }

    companion object {
        init {
            System.loadLibrary("native-codec-jni")
        }
    }

    class CodecException(errorCode: Int, actionCode: Int, errorMsg: String) : IOException(errorMsg)

    interface OnCompletionListener {
        fun onCompletion()
    }
}