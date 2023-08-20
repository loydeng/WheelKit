//
// Created by loyde on 2023/2/1.
//

#include "DecoderWrapper.h"

DecoderWrapper::~DecoderWrapper() {
    TRACE()
    if (packet) {
        av_packet_free(&packet);
    }
    if (frame) {
        av_frame_free(&frame);
    }
    delete demuxer;
    delete pcmQueue;
    pthread_attr_destroy(&attr);
    if (swrContext) {
        swr_free(&swrContext);
    }
    if (swsContext) {
        sws_freeContext(swsContext);
        swsContext = nullptr;
    }
    if (produceListener) {
        delete produceListener;
    }
}

DecoderWrapper::DecoderWrapper(DemuxerWrapper *demuxer, ProduceListener *listener,
                               ChannelLayout channelLayout,
                               SampleFormat sampleFormat) {
    this->demuxer = demuxer;
    this->produceListener = listener;
    this->pcmQueue = new DataQueue<PCMBuffer>(produceListener);
    this->packet = av_packet_alloc();
    this->frame = av_frame_alloc();
    INIT_DETACHED_THREAD_ATTR(attr)
    this->channelLayout = channelLayout;
    this->sampleFormat = sampleFormat;
}

void *decode(void *param) {
    auto *decoder = static_cast<DecoderWrapper *>(param);
    int ret = -1;
    DemuxerWrapper *&demuxer = decoder->GetDemuxer();
    int audioIndex = demuxer->GetAudioIndex();
    int videoIndex = demuxer->GetVideoIndex();
    AVCodecContext *audioCodecCtx = demuxer->GetAudioCodecCtx();
    AVPacket *&packet = decoder->GetPacket();
    AVFrame *&frame = decoder->GetFrame();
    SwrContext *&swrCtx = decoder->GetSwrCtx();
    bool is_eof = false;
    while (decoder->IsStart()) {
        ret = decoder->GetDemuxer()->ReadData(packet);
        if (ret != 0) {
            is_eof = true;
        }
        if (packet->stream_index == audioIndex) {
            if (is_eof) {
                // 刷新最后的缓存数据, 后续不可再使用该解码器解码, 若再发生帧数据会导致如下错误返回:
                // AVERROR_EOF -> the decoder has been flushed, and no new packets can be sent to it. need to recreate AVCodecCtx
                ret = avcodec_send_packet(audioCodecCtx, nullptr);
                ASSERT_COND_OR_BREAK(ret == 0)
            } else {
                ret = avcodec_send_packet(audioCodecCtx, packet);
                av_packet_unref(packet);// packet 中 data,在 avcodec_send_packet 中引用则转移所有权, 拷贝则释放.
                if (ret != 0) {
                    // EAGAIN -> data not enough to decode
                    char *err = av_err2str(ret);
                    LOCATE_E(err)
                    continue;
                }
            }
            while (true) {
                ret = avcodec_receive_frame(audioCodecCtx, frame);

                if (ret != 0) {
                    break;
                }

                if (frame->channel_layout > 0 && frame->channels == 0) {
                    frame->channels = av_get_channel_layout_nb_channels(frame->channel_layout);
                } else if (frame->channels > 0 && frame->channel_layout == 0) {
                    frame->channel_layout = av_get_default_channel_layout(frame->channels);
                }

                if (!swrCtx) {
                    swrCtx = swr_alloc_set_opts(nullptr,
                                                decoder->GetChannelLayout(),
                                                decoder->GetSampleFormat(),
                                                frame->sample_rate,
                                                (int64_t) frame->channel_layout,
                                                (AVSampleFormat) frame->format,
                                                frame->sample_rate,
                                                0, nullptr);
                    decoder->GetSwrCtx() = swrCtx;
                    ASSERT_COND_OR_CONTINUE(swrCtx)
                    ret = swr_init(swrCtx);
                    ASSERT_COND_OR_CONTINUE(ret >= 0)
                }

                PCMBuffer *&pcmBuffer = decoder->GetPCMQueue()->Produce();
                if (decoder->IsStop()) {
                    pcmBuffer->GetSize() = 0;
                    decoder->GetPCMQueue()->ProduceDone();
                    avcodec_flush_buffers(audioCodecCtx);
                    demuxer->ResetPosition();
                    break;
                } else {
                    int sample_nb = swr_convert(swrCtx,
                                                &pcmBuffer->GetData(), frame->nb_samples,
                                                (const uint8_t **) frame->data, frame->nb_samples);
                    int channels = av_get_channel_layout_nb_channels(decoder->GetChannelLayout());
                    int sample_size = av_get_bytes_per_sample(decoder->GetSampleFormat());
                    int data_size = sample_nb * channels * sample_size;
                    pcmBuffer->GetSize() = data_size;
                    decoder->CalcDB(pcmBuffer->GetData(), data_size);
                    decoder->GetPCMQueue()->ProduceDone();
                }
            }
        } else if (packet->stream_index == videoIndex) {

        }
    }
    if (is_eof) {
        PCMBuffer *&pcmBuffer = decoder->GetPCMQueue()->Produce();
        pcmBuffer->GetSize() = -1;
        decoder->GetPCMQueue()->ProduceDone();
        demuxer->ResetPosition();
        demuxer->ReCreateCodecCtx();
    }
    return nullptr;
}

bool DecoderWrapper::StartDecode() {
    GetPCMQueue()->Reset();
    state = START;
    bool ret = (pthread_create(&thread, &attr, decode, this) == 0);
    CHECK_COND(ret);
    return ret;
}

bool DecoderWrapper::PauseDecode() {
    state = PAUSE;
    return true;
}

bool DecoderWrapper::StopDecode() {
    TRACE()
    state = STOP;
    return true;
}

DemuxerWrapper *&DecoderWrapper::GetDemuxer() {
    return demuxer;
}

AVPacket *&DecoderWrapper::GetPacket() {
    return packet;
}

AVFrame *&DecoderWrapper::GetFrame() {
    return frame;
}

SwrContext *&DecoderWrapper::GetSwrCtx() {
    return swrContext;
}

bool DecoderWrapper::IsStart() {
    return state == START;
}

bool DecoderWrapper::IsPause() {
    return state == PAUSE;
}

bool DecoderWrapper::IsStop() {
    return state == STOP;
}

DataQueue<PCMBuffer> *&DecoderWrapper::GetPCMQueue() {
    return pcmQueue;
}

int64_t DecoderWrapper::GetChannelLayout() {
    switch (channelLayout) {
        case STEREO:
            return AV_CH_LAYOUT_STEREO;
        case SINGLE:
            return AV_CH_LAYOUT_MONO;
    }
}

AVSampleFormat DecoderWrapper::GetSampleFormat() {
    switch (sampleFormat) {
        case U8:
            return AV_SAMPLE_FMT_U8;
        case U8P:
            return AV_SAMPLE_FMT_U8P;
        case S16:
            return AV_SAMPLE_FMT_S16;
        case S16P:
            return AV_SAMPLE_FMT_S16P;
        case FLT:
            return AV_SAMPLE_FMT_FLT;
        case FLTP:
            return AV_SAMPLE_FMT_FLTP;
    }
}

int DecoderWrapper::CalcDB(uint8_t const *pcm, int len) {
    int db = 0;
    short int value = 0;
    double sum = 0;
    for (int i = 0; i < len; i += 2) {
        memcpy(&value, pcm + i, 2);
        sum += abs(value);
    }
    double average = sum / (len / 2);
    if (average > 0) {
        // 分贝计算公式: db = 20 Log10(Prms/Pref); Prms 当前振幅, Pref 最大振幅 // pcm 的值已是 Prms/Pref
        db = (int) (20 * log10(average));
    }
    return db;
}

PCMBuffer::PCMBuffer(int capacity) {
    data = new uint8_t[capacity];
    this->capacity = capacity;
}

PCMBuffer::~PCMBuffer() {
    delete[] data;
}

uint8_t *&PCMBuffer::GetData() {
    return data;
}

int &PCMBuffer::GetSize() {
    return size;
}
