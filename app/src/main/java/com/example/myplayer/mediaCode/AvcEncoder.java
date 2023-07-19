package com.example.myplayer.mediaCode;

// 编码器
// https://github.com/Denislyl/AndroidMediaCodec/blob/master/AvcEncoder.java

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class AvcEncoder {

    private final static String TAG = "Encoder";
    private MediaCodec mediaCodec;
    private int mWidth, mHeight;
    byte[] mInfo = null;
    boolean RecordEncDataFlag = false;
    FileOutputStream fos = null;
    private byte[] yvu420 = null;

    public AvcEncoder(int width, int height, int framerate, int bitrate) {
        mWidth = width;
        mHeight = height;
        yvu420 = new byte[width * height * 3 / 2];
        getSupportColorFormat();
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/acv");
        } catch (IOException e) {e.printStackTrace();}
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30);

        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        if (RecordEncDataFlag) {
            try {
                fos = new FileOutputStream(new File("/sdcrad/app_camera_enc.ha64"));
            } catch (FileNotFoundException e) {e.printStackTrace();}
        }
    }

    public void close() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {e.printStackTrace();}

        try {
            fos.close();
        } catch (IOException e) {e.printStackTrace();}

    }

    public int offerEncoder(byte[] input, byte[] output) {
        Log.d(".........Codec", "encoder in");
        int pos = 0;
        yvu420 = input;
        try {
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outBuffers = mediaCodec.getInputBuffers();
            int inputIndex = mediaCodec.dequeueInputBuffer(-1);
            if (inputIndex > 0) {
                ByteBuffer inputBuffer = inputBuffers[inputIndex];
                inputBuffer.clear();
                inputBuffer.put(yvu420);
                mediaCodec.queueInputBuffer(inputIndex, 0, input.length, 0, 0);
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            while (outputIndex >= 0) {
                ByteBuffer outputBuffer = outBuffers[outputIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                if (mInfo != null) {
                    System.arraycopy(outData, 0, output, 0, outData.length);
                    pos += outData.length;
                    Log.d(TAG, "offerEncoder: ");
                } else {
                    ByteBuffer spBuffer = ByteBuffer.wrap(outData);
                    if (bufferInfo.flags == 2) {
                        mInfo = new byte[outData.length];
                        System.arraycopy(outData, 0, mInfo, 0, outData.length);
                        System.arraycopy(outData, 0, output, pos, outData.length);
                        pos += outData.length;
                    } else {
                        Log.d(TAG, "offerEncoder: error");
                        return -1;
                    }
                    Log.d(TAG, "mInfo: " + Arrays.toString(mInfo));
                }
                mediaCodec.releaseOutputBuffer(outputIndex, false);
                outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
            if (bufferInfo.flags == 1) {
                Log.d(TAG, "key frame");
                System.arraycopy(output, 0, yvu420, 0, pos);
                System.arraycopy(mInfo, 0, output, 0, mInfo.length);
                pos += mInfo.length;
            }
        } catch (Exception e) {e.printStackTrace();}
        return pos;
    }



    private int getSupportColorFormat() {
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo codecInfo = null;
        for (int i = 0; i < numCodecs && codecInfo == null; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) continue;
            String[] type = info.getSupportedTypes();
            boolean found = false;
            for (int j = 0; j < type.length & !found; j++) {
                if (type[i].equals("video/avc")) found = true;
            }
            if (!found) continue;
            codecInfo = info;
        }
        Log.e("AvcEncoder", "Found " + codecInfo.getName() + " supporting " + "video/avc");
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");
        Log.e("AvcEncoder",
                "length-" + capabilities.colorFormats.length + "==" + Arrays.toString(capabilities.colorFormats));
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            switch (capabilities.colorFormats[i]) {
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible:
                    Log.e("AvcEncoder", "supported color format::" + capabilities.colorFormats[i]);
                    break;//return capabilities.colorFormats[i];
                default:
                    Log.e("AvcEncoder", "other color format " + capabilities.colorFormats[i]);
                    break;
            }
        }
        return 0;
    }

}
