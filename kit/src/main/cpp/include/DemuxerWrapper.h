//
// Created by loyde on 2023/1/30.
//

#ifndef WHEELKIT_DEMUXERWRAPPER_H
#define WHEELKIT_DEMUXERWRAPPER_H
#include "LogWrapper.h"

//#define __STDC_CONSTANT_MACROS
extern "C" {
#include "ffmpeg/libavformat/avformat.h"
#include "ffmpeg/libavcodec/avcodec.h"
}

#define STREAM_DURATION(stream) stream->duration * av_q2d(stream->time_base)
#define FRAME_CURRENT_TIME(frame, stream) frame->pts * av_q2d(stream->time_base)

class DemuxerWrapper {
public:
    DemuxerWrapper(const char *url);

    ~DemuxerWrapper();

    bool Init();

    long long GetDuration();

    int ReadData(AVPacket *&packet);

    bool ResetPosition();

    bool ReCreateCodecCtx();

    bool SeekTo(long long milliSecond, int streamIndex=-1);

    AVFormatContext *GetInputFmtCtx() const;

    int GetAudioIndex() const;

    int GetVideoIndex() const;

    AVStream *GetAudioStream() const;

    AVStream *GetVideoStream() const;

    AVCodecParameters *GetAudioCodecPar() const;

    AVCodecParameters *GetVideoCodecPar() const;

    const AVCodec *GetAudioCodec() const;

    const AVCodec *GetVideoCodec() const;

    AVCodecContext *GetAudioCodecCtx() const;

    AVCodecContext *GetVideoCodecCtx() const;

private:
    static AVCodecContext* CreateAVCodecCtx(const AVCodec* &codec, AVCodecParameters* &parameters);
    char *url{nullptr};
    AVFormatContext *inputFmtCtx{nullptr};
    int audioIndex{-1};
    int videoIndex{-1};
    AVStream* audioStream{nullptr};
    AVStream* videoStream{nullptr};
    AVCodecParameters *audioCodecPar{nullptr};
    AVCodecParameters *videoCodecPar{nullptr};
    const AVCodec *audioCodec{nullptr};
    const AVCodec *videoCodec{nullptr};
    AVCodecContext *audioCodecCtx{nullptr};
    AVCodecContext *videoCodecCtx{nullptr};
    long long duration{0};
    pthread_mutex_t mutex{};
};


#endif //WHEELKIT_DEMUXERWRAPPER_H
