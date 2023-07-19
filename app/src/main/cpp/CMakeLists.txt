cmake_minimum_required(VERSION 3.18.1)

set(FFMPEG ${CMAKE_SOURCE_DIR}/ffmpeg)
set(RTMP ${CMAKE_SOURCE_DIR}/remp)
include_directories(${FFMPEG}/include)
set(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} -L${FFMPEG}/libs/${CMAKE_ANDROID_ARCH_ABI}") # ffmpeg库指定
set(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} -L${RTMP}/libs/${CMAKE_ANDROID_ARCH_ABI}") # rtmp库指定

file(GLOB src_files *.cpp)

add_library( # Sets the name of the library.
        native-lib
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        ${src_files})

target_link_libraries( # Specifies the target library.
        native-lib # 总库libnative-lib.so

        # 忽略顺序的方式，导入
        -Wl,--start-group
        avcodec avfilter avformat avutil swresample swscale
        -Wl,--end-group

        log # 日志库，打印日志用的
        z # libz.so库，是FFmpeg需要用ndk的z库，FFMpeg需要额外支持  libz.so
        rtmp # rtmp 后面会专门介绍 rtmp交叉编译+FFmpeg结合编译
        android # android 后面会专门介绍，目前先要明白的是 ANativeWindow 用来渲染画面的 == Surface对象
        OpenSLES # OpenSLES 后面会专门介绍，目前先要明白的是 OpenSLES 用来播放声音的-特别是在native层播放（最快的）
        )