package com.example.myplayer.mediaCode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Vector;

public class AvcDecoder {

    private static final String TAG = "AvcDecoder";
    private MediaCodec mediaCodec;
    int count = 0;
    byte[] nv12;
    Vector mVertor;

    public AvcDecoder(int width, int height, SurfaceHolder surfaceHolder) {
        Log.d(TAG, "AvcDecoder start");
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
            nv12 = new byte[width * height * 3 / 2];
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            mediaCodec.configure(mediaFormat, null, null, 0);
            mediaCodec.start();

        } catch (IOException e) {e.printStackTrace();}
    }

    public void pushData(byte[] buffer, int len) {

    }

    public void onFrame(byte[] buffer, int len) {
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
        Log.d(TAG, "inputBufferIndex: " + inputBufferIndex);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buffer, 0, len);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, len, count * 10000, 0);
            count++;
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, -1);
        while (outputBufferIndex >= 0) {
            outputBuffers[outputBufferIndex].get(nv12, 0, nv12.length);

        }
    }

    public void close() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {e.printStackTrace();}
    }


    private static class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
        }
    }


}
