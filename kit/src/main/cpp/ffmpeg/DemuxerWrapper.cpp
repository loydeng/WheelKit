//
// Created by loyde on 2023/1/30.
//

#include "DemuxerWrapper.h"

long long DemuxerWrapper::GetDuration() {
    if (inputFmtCtx && !duration) {
        duration = inputFmtCtx->duration * 1000 / AV_TIME_BASE;
        FORMAT_W("AVFmtCtx: duration=%lld", duration)
    }
    return duration;
}

DemuxerWrapper::DemuxerWrapper(const char *url) {
    this->url = new char[strlen(url)];
    strcpy(this->url, url);
    pthread_mutex_init(&mutex, nullptr);
}

DemuxerWrapper::~DemuxerWrapper() {
    TRACE()
    if (videoCodecCtx) {
        avcodec_free_context(&videoCodecCtx);
    }
    if (audioCodecCtx) {
        avcodec_free_context(&audioCodecCtx);
    }
    avformat_close_input(&inputFmtCtx);
    delete[] url;
    pthread_mutex_destroy(&mutex);
}

// 拓展编解码器
// char *hw_codecs[] = {"libvpx","libvpx-vp9","libopus", "libopenh264", "libx264", "libx265","libwebp","libvorbis"};
// 硬解码器 avcodec_find_decoder_by_name
// h264_mediacodec hevc_mediacodec vp8_mediacodec vp9_mediacodec mpeg2_mediacodec mpeg4_mediacodec

bool DemuxerWrapper::Init() {
    int ret = -1;
    ret = avformat_open_input(&inputFmtCtx, url, nullptr, nullptr);
    ASSERT_ZERO_OR_RETURN(ret, false)
    ret = avformat_find_stream_info(inputFmtCtx, nullptr);
    ASSERT_COND_OR_RETURN(ret >= 0, false)

    GetDuration();
    audioIndex = av_find_best_stream(inputFmtCtx, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);
    videoIndex = av_find_best_stream(inputFmtCtx, AVMEDIA_TYPE_VIDEO, -1, -1, nullptr, 0);

    if (audioIndex >= 0) {
        audioStream = inputFmtCtx->streams[audioIndex];
        audioCodecPar = audioStream->codecpar;
        audioCodec = avcodec_find_decoder(audioCodecPar->codec_id);
        ASSERT_NONNULL_OR_RETURN(audioCodec, false)
        audioCodecCtx = CreateAVCodecCtx(audioCodec, audioCodecPar);
        FORMAT_W("audio stream: duration=%f, sample_rate=%d, channels=%d, sample_format=%d",
                 STREAM_DURATION(audioStream), audioCodecPar->sample_rate, audioCodecPar->channels,
                 audioCodecPar->format)
    }
    if (videoIndex >= 0) {
        videoStream = inputFmtCtx->streams[videoIndex];
        videoCodecPar = videoStream->codecpar;
        videoCodec = avcodec_find_decoder(videoCodecPar->codec_id);
        ASSERT_NONNULL_OR_RETURN(videoCodec, false)
        videoCodecCtx = CreateAVCodecCtx(videoCodec, videoCodecPar);
        FORMAT_W("video stream duration:%f, width=%d, height=%d, frameRate=%d, bitRate=%ld",
                 STREAM_DURATION(videoStream), videoCodecCtx->width, videoCodecCtx->height,
                 videoCodecCtx->framerate.num/videoCodecCtx->framerate.den, videoCodecCtx->bit_rate)
    }
    if (audioIndex < 0 && videoIndex < 0) {
        LOCATE_E(STRING_FORMAT("not found media stream in %s", url))
        return false;
    }
    return true;
}

int DemuxerWrapper::ReadData(AVPacket *&packet) {
    if (inputFmtCtx) {
        pthread_mutex_lock(&mutex);
        int ret = av_read_frame(inputFmtCtx, packet);
        pthread_mutex_unlock(&mutex);
        return ret;
    } else {
        LOCATE_E("DemuxerWrapper not Prepare!")
        return -1;
    }
}

bool DemuxerWrapper::ResetPosition() {
    if (inputFmtCtx) {
        TRACE()
        pthread_mutex_lock(&mutex);
        int ret = av_seek_frame(inputFmtCtx, -1, 0, AVSEEK_FLAG_BACKWARD);
        pthread_mutex_unlock(&mutex);
        ASSERT_COND_OR_RETURN(ret >= 0, false)
        return true;
    }
    return false;
}

bool DemuxerWrapper::ReCreateCodecCtx() {
    if (audioCodecCtx) {
        avcodec_free_context(&audioCodecCtx);
        audioCodecCtx = CreateAVCodecCtx(audioCodec, audioCodecPar);
    }
    if (videoCodecCtx) {
        avcodec_free_context(&videoCodecCtx);
        videoCodecCtx = CreateAVCodecCtx(videoCodec, videoCodecPar);
    }
    return videoCodecCtx || videoCodecCtx;
}

bool DemuxerWrapper::SeekTo(long long milliSecond, int streamIndex) {
    if (inputFmtCtx) {
        TRACE()
        AVRational src{1, 1000};
        AVRational dst;
        if (streamIndex >= 0) {
            dst = inputFmtCtx->streams[streamIndex]->time_base;
        } else {
            dst = {1, AV_TIME_BASE};
        }
        int64_t position = av_rescale_q_rnd((int64_t) milliSecond, src, dst, AV_ROUND_NEAR_INF);
        pthread_mutex_lock(&mutex);
        int ret = avformat_seek_file(inputFmtCtx, streamIndex, INT64_MIN, position, INT64_MAX, 0);
        pthread_mutex_unlock(&mutex);
        ASSERT_COND_OR_RETURN(ret >= 0, false)
        return true;
    }
    return false;
}

AVCodecContext *
DemuxerWrapper::CreateAVCodecCtx(const AVCodec *&codec, AVCodecParameters *&parameters) {
    AVCodecContext *codecCtx = avcodec_alloc_context3(codec);
    ASSERT_NONNULL_OR_RETURN(codecCtx, nullptr)

    int ret = avcodec_parameters_to_context(codecCtx, parameters);
    ASSERT_COND_OR_RETURN(ret >= 0, nullptr)

    // 可以设置多线程解码, 针对于软解码
    // videoCodecCtx->thread_count = 4;
    ret = avcodec_open2(codecCtx, codec, nullptr);
    ASSERT_ZERO_OR_RETURN(ret, nullptr)
    return codecCtx;
}

AVFormatContext *DemuxerWrapper::GetInputFmtCtx() const {
    return inputFmtCtx;
}

int DemuxerWrapper::GetAudioIndex() const {
    return audioIndex;
}

int DemuxerWrapper::GetVideoIndex() const {
    return videoIndex;
}

AVStream *DemuxerWrapper::GetAudioStream() const {
    return audioStream;
}

AVStream *DemuxerWrapper::GetVideoStream() const {
    return videoStream;
}

AVCodecParameters *DemuxerWrapper::GetAudioCodecPar() const {
    return audioCodecPar;
}

AVCodecParameters *DemuxerWrapper::GetVideoCodecPar() const {
    return videoCodecPar;
}

const AVCodec *DemuxerWrapper::GetAudioCodec() const {
    return audioCodec;
}

const AVCodec *DemuxerWrapper::GetVideoCodec() const {
    return videoCodec;
}

AVCodecContext *DemuxerWrapper::GetAudioCodecCtx() const {
    return audioCodecCtx;
}

AVCodecContext *DemuxerWrapper::GetVideoCodecCtx() const {
    return videoCodecCtx;
}





