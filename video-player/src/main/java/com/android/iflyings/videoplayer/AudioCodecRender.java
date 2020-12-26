package com.android.iflyings.videoplayer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaFormat;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.AudioTrack.WRITE_BLOCKING;


public class AudioCodecRender extends CodecRender {

    private AudioTrack mAudioTrack;

    AudioCodecRender() {
        super();
    }

    private VideoPlayer.OnErrorListener mOnErrorListener;
    void setOnErrorListener(VideoPlayer.OnErrorListener listener) {
        mOnErrorListener = listener;
    }


    @Override
    protected void onCreated() {
        int sampleRate = 44100;
        int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        int bufferSizeInBytes = Math.max(minBufferSize, 2048);
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,// 指定流的类型
                sampleRate,// 设置音频数据的採样率 32k，假设是44.1k就是44100
                channelConfig,// 设置输出声道为双声道立体声，而CHANNEL_OUT_MONO类型是单声道
                audioFormat,// 设置音频数据块是8位还是16位。这里设置为16位。
                bufferSizeInBytes,//缓冲区大小
                AudioTrack.MODE_STREAM // 设置模式类型，在这里设置为流类型，第二种MODE_STATIC貌似没有什么效果
        );
    }

    @Override
    protected void onStarted() {
        mAudioTrack.play();
    }

    @Override
    protected void onReleased() {
        mAudioTrack.stop();
        mAudioTrack.release();
        mAudioTrack = null;
    }

    @Override
    protected String getCodecType() {
        return "audio";
    }

    @Override
    protected void onProcessOutputBuffer(ByteBuffer byteBuffer, int size, long presentationTimeUs) {
        mAudioTrack.write(byteBuffer, size, WRITE_BLOCKING);
    }

    @Override
    public void onFormatChanged(MediaFormat mediaFormat) {

    }

    @Override
    public void onCompletion() {

    }
}
