package com.example.myplayer.audio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.core.app.ActivityCompat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


//音频数据采集
public class MyAudioRecord implements IAudioRecord {

    private static final String TAG = "MyAudioRecord";

    private static final int INPUT = MediaRecorder.AudioSource.MIC;
    private static final int SAMPLE_RATE_IN_SIZE = 16_000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE_IN_BYTES = 2048;

    private int mAudioSource;
    private int mSampleRateInHz;
    private int mChannelConfig;
    private int mAudioFormat;
    private int mBufferSizeInBytes;
    private byte[] mBuffer;
    @Status
    private int mStatus;

    private AudioRecord mAudioRecord;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private boolean isStopRecording = false;
    private Context mContext;

    @IntDef({Status.NO_READY, Status.READY, Status.RECORDING, Status.PAUSE, Status.STOP, Status.DESTROY})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
    public @interface Status {
        int NO_READY = 0;
        int READY = 1;
        int RECORDING = 2;
        int PAUSE = 3;
        int STOP = 4;
        int DESTROY = 5;
    }

    private static final int MSG_START = 1;
    private static final int MSG_RESUME = 2;
    private static final int MSG_PAUSE = 3;
    private static final int MSG_STOP = 4;
    private static final int MSG_DESTROY = 5;

    public MyAudioRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat,
                         int bufferSizeInBytes, byte[] buffer) {
        this.mAudioSource = audioSource;
        this.mSampleRateInHz = sampleRateInHz;
        this.mChannelConfig = channelConfig;
        this.mAudioFormat = audioFormat;
        this.mBufferSizeInBytes = bufferSizeInBytes;
        this.mBuffer = buffer;
        this.mBufferSizeInBytes = Math.max(bufferSizeInBytes, AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat));
        mStatus = Status.NO_READY;

        createAudioRecord(audioSource, sampleRateInHz, channelConfig,
                audioFormat, bufferSizeInBytes);
        createThread();
    }

    private void createAudioRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat,
                                   int bufferSizeInBytes) {
        if (mStatus != Status.NO_READY) {
            Log.d(TAG, "createAudioRecord: status is no ready");
        }

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat,
                bufferSizeInBytes);
        mStatus = Status.READY;
    }

    private void createThread() {
        mHandlerThread = new HandlerThread("audio-thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), (Handler.Callback) msg -> {
            switch (msg.what) {
                case MSG_START:
                    onStart();
                    break;
                case MSG_RESUME:
                    onResume();
                    break;
                case MSG_PAUSE:
                    onPause();
                    break;
                case MSG_STOP:
                    onStop();
                    break;
                case MSG_DESTROY:
                    onDestroy();
                    break;
            }
            return true;
        });
    }


    @Override
    public void start() {
        isStopRecording = false;
        mHandler.sendEmptyMessage(MSG_START);
    }

    private void onStart() {
        if (mStatus != Status.STOP && mStatus != Status.READY) return;
        mStatus = Status.RECORDING;
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED)
            Log.d(TAG, "onStart: state uninitialized");
        mAudioRecord.startRecording();
        if (stateListener != null) {
            stateListener.onStart();
        }
        doRecording();
    }

    private void doRecording() {
        try {onReading();}
        catch (Exception e) {handleError(AudioRecord.ERROR, e);}
    }

    private void onReading() {
        int ret;
        while (mAudioRecord != null && !isStopRecording &&
                mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING && mStatus == Status.RECORDING) {
            ret = mAudioRecord.read(mBuffer, 0, mBufferSizeInBytes);
            if (ret >= 0) {
                handleSuccess(mBuffer);
                continue;
            }
            handleErrorRead(ret);
        }
        Log.i(TAG, "stop reading");
    }



    private void handleSuccess(byte[] mBuffer) {
        if (recordCallback != null) recordCallback.success(mBuffer);
    }

    private void handleErrorRead(int ret) {
        switch (ret) {
            case AudioRecord.ERROR_INVALID_OPERATION:
                handleError(ret, new Exception("the object isn't properly initialized"));
                break;
            case AudioRecord.ERROR_BAD_VALUE:
                handleError(ret, new Exception("the parameters don't resolve to valid data and indexes"));
                break;
            case AudioRecord.ERROR_DEAD_OBJECT:
                handleError(ret, new Exception("the object is not valid anymore and" +
                        "needs to be recreated. The dead object error code is not returned if some data was" +
                        "successfully transferred. In this case, the error is returned at the next read"));
                break;
            case AudioRecord.ERROR:
                handleError(ret, new Exception("unknown error"));
                break;
        }
    }

    private void handleError(int code, Exception e) {
        if (recordCallback != null) recordCallback.error(code, e);
    }


    @Override
    public void resume() {
        isStopRecording = false;
        mHandler.sendEmptyMessage(MSG_RESUME);
    }

    private void onResume() {
        if (mStatus == Status.PAUSE) return;
        mStatus = Status.RECORDING;
        mAudioRecord.startRecording();
        Log.d(TAG, "onResume");
        doRecording();
    }

    @Override
    public void pause() {
        isStopRecording = true;
        mHandler.sendEmptyMessage(MSG_PAUSE);
    }

    private void onPause() {
        if (mStatus != Status.RECORDING) return;
        mStatus = Status.PAUSE;
        mAudioRecord.stop();
        Log.d(TAG, "pause");
    }

    @Override
    public void stop() {
        isStopRecording = true;
        mHandler.sendEmptyMessage(MSG_STOP);
    }

    private void onStop() {
        if (mStatus != Status.RECORDING && mStatus != Status.PAUSE) return;
        mStatus = Status.STOP;
        mAudioRecord.stop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void destroy() {
        isStopRecording = true;
        mHandler.sendEmptyMessage(MSG_DESTROY);
    }

    private void onDestroy() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
        mStatus = Status.DESTROY;
        Log.d(TAG, "onDestroy");
    }


    private OnStateListener stateListener;
    public interface OnStateListener {
        void onStart();
        void onResume();
        void onPause();
        void onStop();
    }

    public void setStateListener(OnStateListener stateListener) {
        this.stateListener = stateListener;
    }

    private RecordCallback recordCallback;
    public interface RecordCallback {
        void success(byte[] buffer);
        void error(int code, Exception e);
    }

    public void setRecordCallback(RecordCallback recordCallback) {
        this.recordCallback = recordCallback;
    }

    public static final class Builder {
        private int mAudioSource;
        private int mSampleRateInHz;
        private int mChannelConfig;
        private int mAudioFormat;
        private int mBufferSizeInBytes;
        private byte[] mBuffer;

        public Builder() {
            this(INPUT, SAMPLE_RATE_IN_SIZE, CHANNEL_CONFIG, AUDIO_FORMAT,
                    BUFFER_SIZE_IN_BYTES, new byte[BUFFER_SIZE_IN_BYTES]);
        }

        public Builder(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat,
                       int bufferSizeInBytes, byte[] buffer) {
            mAudioSource = audioSource;
            mSampleRateInHz = sampleRateInHz;
            mChannelConfig = channelConfig;
            mAudioFormat = audioFormat;
            mBufferSizeInBytes = bufferSizeInBytes;
            mBuffer = buffer;
        }

        public Builder setAudioSource(int audioSource) {
            this.mAudioSource = audioSource;
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

        public MyAudioRecord build() {return new MyAudioRecord(mAudioSource, mSampleRateInHz,
                mChannelConfig, mAudioFormat, mBufferSizeInBytes, mBuffer);}
    }
}
