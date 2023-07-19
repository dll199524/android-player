package com.example.myplayer.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myplayer.R;
import com.example.myplayer.databinding.ActivityAudioRecordBinding;

//https://blog.csdn.net/m0_38089373/category_10242606.html
public class AudioRecordActivity extends AppCompatActivity {

    private ActivityAudioRecordBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }


}