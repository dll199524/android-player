package com.example.myplayer.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import com.example.myplayer.R;
import com.example.myplayer.databinding.ActivityAudioRecordBinding;

//https://blog.csdn.net/m0_38089373/category_10242606.html
public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityAudioRecordBinding binding;
    private MyAudioRecord mAudioRecord;
    private MyAudioTrack mAudioTrack;
    private MediaPlayer mMediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAudioRecord = new MyAudioRecord.Builder().build();
        mAudioRecord.setRecordCallback(new MyAudioRecord.RecordCallback() {
            @Override
            public void success(byte[] buffer) {

            }

            @Override
            public void error(int code, Exception e) {

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                break;
            default:
                break;
        }
    }
}