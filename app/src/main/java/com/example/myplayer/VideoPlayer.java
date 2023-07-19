package com.example.myplayer;

public class VideoPlayer {

    static {
        System.loadLibrary("native-lib");
    }

    public native int decodeVideo(String inputPath, String outputPath);
    public native int decodeAudio(String videoPath, String pcmPath);
    public native int playAudioOpenSLES(String pcmPath);
}
