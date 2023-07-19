package com.example.myplayer.mediaCode.aac;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;


import androidx.core.app.ActivityCompat;

import java.io.IOException;

//编码保存
public class AacAudioRecord {

    private static final String TAG = "AacAudioRecord";
    private AudioRecord mAudioRecord;
    private MediaCodec mAudioEncoder;
    private int mAudioSource;
    private int mSampleRateInHz;
    private int mChannelConfig;
    private int mAudioFormat;
    private int mBufferSizeInBytes;
    private MediaFormat mMediaFormat;


    public AacAudioRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat,
                          int bufferSizeInBytes) {
        mAudioSource = audioSource;
        mSampleRateInHz = sampleRateInHz;
        mChannelConfig = channelConfig;
        mAudioFormat = audioFormat;
        mBufferSizeInBytes = bufferSizeInBytes;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        try {
            mAudioEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        } catch (IOException e) {e.printStackTrace();}
        try {
            mAudioEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mMediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRateInHz,
                    channelConfig == AudioFormat.CHANNEL_OUT_MONO ? 1 : 2);
            mAudioEncoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {e.printStackTrace();}


    }
}
