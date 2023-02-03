//
// Created by loyde on 2023/2/1.
//

#include "include/DecoderWrapper.h"

DecoderWrapper::~DecoderWrapper() {
    if (packet) {
        av_packet_free(&packet);
    }
    if (frame) {
        av_frame_free(&frame);
    }
    if (demuxer) {
        delete demuxer;
    }
    pthread_attr_destroy(&attr);
}

DecoderWrapper::DecoderWrapper(DemuxerWrapper *demuxer, ChannelLayout channelLayout, SampleFormat sampleFormat) {
    this->demuxer = demuxer;
    this->pcmQueue = new DataQueue<PCMBuffer>();
    this->packet = av_packet_alloc();
    this->frame = av_frame_alloc();
    INIT_DETACHED_THREAD_ATTR(attr)
    this->channelLayout = channelLayout;
    this->sampleFormat = sampleFormat;
}

void * decode(void *param){
    DecoderWrapper *decoder = static_cast<DecoderWrapper *>(param);
    int ret = -1;
    DemuxerWrapper *&demuxer = decoder->GetDemuxer();
    AVPacket* &packet = decoder->GetPacket();
    AVFrame* &frame = decoder->GetFrame();
    SwrContext *&swrCtx = decoder->GetSwrCtx();
    while (decoder->IsStart()) {
        ret = decoder->GetDemuxer()->ReadData(packet);
        if (ret != 0) {
            decoder->GetState() = DecoderWrapper::STOP;
        }
        if (packet->stream_index == demuxer->GetAudioIndex()) {
            if (decoder->IsStop()) {
                // 刷新最后的缓存数据
                ret = avcodec_send_packet(demuxer->GetAudioCodecCtx(), nullptr);
                ASSERT_COND_OR_BREAK(ret == 0)
            } else{
                ret = avcodec_send_packet(demuxer->GetAudioCodecCtx(), packet);
                av_packet_unref(packet);// packet 中 data,在 avcodec_send_packet 中引用则转移所有权, 拷贝则释放.
                if (ret != 0) {
                    // AVERROR_EOF -> the decoder has been flushed, and no new packets can be sent to it. need to recreate AVCodecCtx
                    // EAGAIN -> data not enough to decode
                    char * err = av_err2str(ret);
                    LOCATE_E(err)
                    continue;
                }
            }
            while (true) {
                ret = avcodec_receive_frame(demuxer->GetAudioCodecCtx(), frame);

                if (ret != 0) {
                    break;
                }
                if (frame->channel_layout > 0 && frame->channels == 0) {
                    frame->channels = av_get_channel_layout_nb_channels(frame->channel_layout);
                } else if (frame->channels > 0 && frame->channel_layout == 0) {
                    frame->channel_layout = av_get_default_channel_layout(frame->channels);
                }

                if (!swrCtx) {
                    swrCtx = swr_alloc_set_opts(NULL,
                                                    decoder->GetChannelLayout(),
                                                    decoder->GetSampleFormat(),
                                                    frame->sample_rate,
                                                    frame->channel_layout,
                                                    (AVSampleFormat) frame->format,
                                                    frame->sample_rate,
                                                    0, nullptr);
                    ASSERT_COND_OR_CONTINUE(swrCtx)
                    ret = swr_init(swrCtx);
                    ASSERT_COND_OR_CONTINUE(ret >= 0)
                }

                PCMBuffer*& pcmBuffer = decoder->GetPCMQueue()->Produce();
                int sample_nb = swr_convert(swrCtx,
                                            &pcmBuffer->GetData(), frame->nb_samples,
                                            (const uint8_t **) frame->data, frame->nb_samples);
                int channels = av_get_channel_layout_nb_channels(decoder->GetChannelLayout());
                int sample_size = av_get_bytes_per_sample(decoder->GetSampleFormat());
                int data_size = sample_nb * channels * sample_size;
                pcmBuffer->GetSize() = data_size;
                decoder->GetPCMQueue()->ProduceDone();
            }
        } else if (packet->stream_index == demuxer->GetVideoIndex()) {

        }
    }
    if (decoder->IsStop()) {
        PCMBuffer*& pcmBuffer = decoder->GetPCMQueue()->Produce();
        pcmBuffer->GetSize() = 0;
        decoder->GetPCMQueue()->ProduceDone();
    }
    return nullptr;
}

bool DecoderWrapper::StartDecode() {
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
    state = STOP;
    demuxer->ResetPosition();
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

DecoderWrapper::State &DecoderWrapper::GetState() {
    return state;
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
