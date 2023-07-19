#include <jni.h>
#include <string>
#include "libswscale/swscale.h"


extern "C" { // ffmpeg是纯c写的，必须采用c的编译方式，否则奔溃
#include <libavformat/avformat.h>
#include "libavutil/imgutils.h"
#include <libavutil/time.h>
#include <android/log.h>
}
#define FFMPEG_LOG_TAG	"FFmpeg_Native"
#define LOGV(...)__android_log_print(ANDROID_LOG_VERBOSE, FFMPEG_LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG ,FFMPEG_LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  ,FFMPEG_LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  ,FFMPEG_LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR ,FFMPEG_LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_myplayer_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_myplayer_VideoPlayer_decodeVideo(JNIEnv *env, jobject thiz, jstring input_path,
                                                  jstring output_path) {
    AVFormatContext *avFormatContext = avformat_alloc_context();
    const char *url = env->GetStringUTFChars(input_path, 0);
    //1. 打开媒体文件
    int result = avformat_open_input(&avFormatContext, url, NULL, NULL);
    if (result != 0) {
        LOGD("open input error url = %s, result = %d", &url, &result);
        return -1;
    }
    //2.读取媒体文件信息，给avFormatContext赋值
    if (avformat_find_stream_info(avFormatContext, NULL) < 0) {
        LOGD("find stream error");
        return -1;
    }
    //3. 匹配到视频流的index
    int videoIndex = -1;
    for (int i = 0; i < avFormatContext->nb_streams; i++) {
        AVMediaType codeType = avFormatContext->streams[i]->codecpar->codec_type;
        if (codeType == AVMEDIA_TYPE_AUDIO) {
            videoIndex = i;
            break;
        }
    }
    if (videoIndex == -1) {
        LOGD("cannot find a audio stream");
        return -1;
    }
    AVCodecParameters *avCodecParameters = avFormatContext->streams[videoIndex]->codecpar;
    //4. 根据视频流信息的codec_id找到对应的解码器
    AVCodec *avCodec = avcodec_find_decoder(avCodecParameters->codec_id);
    if (avCodec == NULL) {
        LOGD("cannot find Codec");
        return -1;
    }
    AVCodecContext *avCodecContext = avFormatContext->streams[videoIndex]->codec;
    //5.使用给定的AVCodec初始化AVCodecContext
    int openResult = avcodec_open2(avCodecContext, avCodec, NULL);
    if (openResult < 0) {
        LOGD("avcodec open2 result %d", &openResult);
        return -1;
    }
    const char *outPath = env->GetStringUTFChars(output_path, 0);
    //6. 初始化输出文件、解码AVPacket和AVFrame结构体
    FILE *outFile = fopen(outPath, "wb+");
    if (outFile == NULL) {
        LOGD("fopen out file error");
        return -1;
    }
    auto *packet = (AVPacket *) av_malloc(sizeof (AVPacket));
    AVFrame *pFrame = av_frame_alloc();
    AVFrame *pFrameYUV = av_frame_alloc();
    uint8_t *out_buffer = (unsigned char *) av_malloc(av_image_get_buffer_size(AV_PIX_FMT_YUV420P,
                                                                               avCodecContext->width, avCodecContext->height, 1));
    av_image_fill_arrays(pFrameYUV->data, pFrameYUV->linesize, out_buffer,
                         AV_PIX_FMT_YUV420P, avCodecContext->width,avCodecContext->height, 1);
    // 由于解码出来的帧格式不一定是YUV420P的,在渲染之前需要进行格式转换
    struct SwsContext *img_convert_ctx = sws_getContext(avCodecContext->width, avCodecContext->height,
            avCodecContext->pix_fmt, avCodecContext->width, avCodecContext->height,
            AV_PIX_FMT_YUV420P, SWS_BICUBIC, NULL, NULL, NULL);
    //7. 开始一帧一帧读取
    int readPacketCount = -1;
    int frame_cnt = 0;
    clock_t startTime = clock();
    while (readPacketCount = av_read_frame(avFormatContext, packet) >= 0)
    {
        if (packet->stream_index == videoIndex)
        {
            //8. send packet
            int sendPacket = avcodec_send_packet(avCodecContext, packet);
            if (sendPacket != 0)
            {
                LOGD("avcodec send packet error %d", sendPacket);
                continue;
            }
            //9. receive frame
            int receive_frame = avcodec_receive_frame(avCodecContext, pFrame);
            if (receive_frame != 0)
            {
                LOGD("avcodec receive frame error %d", receive_frame);
                continue;
            }
            //10. 格式转换
            sws_scale(img_convert_ctx, (const uint8_t *const *) pFrame->data, pFrame->linesize,
                      0, avCodecContext->height, pFrameYUV->data, pFrameYUV->linesize);
            //11. 分别写入YUV数据
            int y_size = avCodecParameters->width * avCodecParameters->height;
            fwrite(pFrameYUV->data[0], 1, y_size, outFile);
            fwrite(pFrameYUV->data[1], 1, y_size / 4, outFile);
            fwrite(pFrameYUV->data[2], 1, y_size / 4, outFile);
            char pictypeStr[10] = {0};
            switch (pFrame->pict_type)
            {
                case AV_PICTURE_TYPE_I:
                {
                    sprintf(pictypeStr, "I");
                    break;
                }
                case AV_PICTURE_TYPE_P:
                {
                    sprintf(pictypeStr, "P");
                    break;
                }
                case AV_PICTURE_TYPE_B:
                {
                    sprintf(pictypeStr, "B");
                    break;
                }
            }
            LOGI("frame index %5d type %s", frame_cnt, pictypeStr);
            frame_cnt++;
        }
        av_packet_unref(packet);
    }
    LOGI("frame count is %d", frame_cnt);
    clock_t endTime = clock();
    LOGI("decode video use time %ld", (endTime - startTime));

    sws_freeContext(img_convert_ctx);
    fclose(outFile);
    av_frame_free(&pFrame);
    av_frame_free(&pFrameYUV);
    avcodec_close(avCodecContext);
    avformat_close_input(&avFormatContext);
    return 0;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_myplayer_VideoPlayer_decodeAudio(JNIEnv *env, jobject thiz, jstring video_path,
                                                  jstring pcm_path) {
    // TODO: implement decodeAudio()
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_myplayer_VideoPlayer_playAudioOpenSLES(JNIEnv *env, jobject thiz,
                                                        jstring pcm_path) {
    // TODO: implement playAudioOpenSLES()
}