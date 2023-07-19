package com.example.myplayer.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
//音频数据采集
public class MyAudioTrack {

    /**
     * mSampleRateInHz 采样率
     * mChannelConfig  音频通道
     * mAudioFormat    音频格式
     */
    private int mStreamType;
    private int mSampleRateInHz;
    private int mChannelConfig;
    private int mAudioFormat;
    private int mBufferSizeInBytes;
    private int mMode;

    private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int SAMPLE_RATE_IN_SIZE = 16_000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE_IN_BYTES = 1024;
    private static final int MODE = AudioTrack.MODE_STREAM;

    private AudioTrack mAudioTrack;

    private MyAudioTrack(int mStreamType, int mSampleRateInHz, int mChannelConfig, int mAudioFormat, int mBufferSizeInBytes, int mMode) {
        this.mStreamType = mStreamType;
        this.mSampleRateInHz = mSampleRateInHz;
        this.mChannelConfig = mChannelConfig;
        this.mAudioFormat = mAudioFormat;
        this.mBufferSizeInBytes = mBufferSizeInBytes;
        this.mMode = mMode;
        mAudioTrack = new AudioTrack(mStreamType, mSampleRateInHz, mChannelConfig, mAudioFormat, mBufferSizeInBytes, mMode);
    }

    public void play() {mAudioTrack.play();}
    public void write(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        mAudioTrack.write(audioData, offsetInBytes, sizeInBytes);
    }
    public void stop() {mAudioTrack.stop();}
    public int getBufferSize() {return AudioTrack.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);}


    public static final class Builder {
        int mStreamType;
        int mSampleRateInHz;
        int mChannelConfig;
        int mAudioFormat;
        int mBufferSizeInBytes;
        int mMode;
        public Builder() {this (STREAM_TYPE, SAMPLE_RATE_IN_SIZE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE_IN_BYTES, MODE);}

        public Builder(int mStreamType, int mSampleRateInHz, int mChannelConfig, int mAudioFormat, int mBufferSizeInBytes, int mMode) {
            this.mStreamType = mStreamType;
            this.mSampleRateInHz = mSampleRateInHz;
            this.mChannelConfig = mChannelConfig;
            this.mAudioFormat = mAudioFormat;
            this.mBufferSizeInBytes = mBufferSizeInBytes;
            this.mMode = mMode;
        }

        public Builder setStreamType (int streamType) {
            this.mStreamType = streamType;
            return this;
        }

        public Builder setSampleRateInHz(int sampleRateInHz) {
            this.mSampleRateInHz = sampleRateInHz;
            return this;
        }

        public Builder setChannelConfig(int channelConfig) {
            this.mChannelConfig = channelConfig;
            return this;
        }

        public Builder setAudioFormat(int audioFormat) {
            this.mAudioFormat = audioFormat;
            return this;
        }

        public Builder setBufferSizeInBytes(int bufferSizeInBytes) {
            this.mBufferSizeInBytes = bufferSizeInBytes;
            return this;
        }

        public Builder setMode(int mode) {
            this.mMode = mode;
            return this;
        }

        public MyAudioTrack build() {
            return new MyAudioTrack(mStreamType, mSampleRateInHz, mChannelConfig, mAudioFormat, mBufferSizeInBytes, mMode);
        }
    }


}
