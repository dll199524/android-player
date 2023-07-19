package com.example.myplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;

import com.example.myplayer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;
    private final static String TAG = "MainActivity";
    CameraManager cm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        cm = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        binding.previewTexture.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "onSurfaceTextureAvailable ");
                setupCamera(width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });
    }



    private void setupCamera(int width, int height) {

        try {
            String[] cameraIdList = cm.getCameraIdList();
            for (String cameraId : cameraIdList) {
                CameraCharacteristics cc = cm.getCameraCharacteristics(cameraId);
                if (cc.get(CameraCharacteristics.LENS_FACING) != CameraMetadata.LENS_FACING_BACK)
                    continue;
                StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] outputSize = map.getOutputSizes(SurfaceTexture.class);

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



}