//
// Created by loyde on 2023/2/5.
//

#include "SLSource.h"

DecoderSource::DecoderSource(const char *url, ProduceListener *listener) {
    auto *demuxerWrapper = new DemuxerWrapper(url);
    demuxerWrapper->Init();
    this->duration = demuxerWrapper->GetDuration();
    decoder = new DecoderWrapper(demuxerWrapper, listener);
    // 未修改采用率, 保持和输入一致. 不会导致变速
    this->sampleRate = demuxerWrapper->GetAudioCodecCtx()->sample_rate;
    this->channel_layout = decoder->GetChannelLayout();
    this->channels = av_get_channel_layout_nb_channels(channel_layout);
    this->sampleFormat = decoder->GetSampleFormat();
    this->bitRate = av_get_bytes_per_sample(sampleFormat) * channels * sampleRate;
}

DecoderSource::~DecoderSource() {
    TRACE()
    delete decoder;
}

PCMBuffer *&DecoderSource::GetBuffer() {
    return decoder->GetPCMQueue()->Consume();
}

void DecoderSource::ReleaseBuffer() {
    decoder->GetPCMQueue()->ConsumeDone();
}

bool DecoderSource::Open() {
    return decoder->StartDecode();
}

bool DecoderSource::Close() {
    return decoder->StopDecode();
}

