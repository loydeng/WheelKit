//
// Created by loyde on 2023/2/5.
//

#ifndef WHEELKIT_SLSOURCE_H
#define WHEELKIT_SLSOURCE_H
#include "SLBase.h"
#include "DecoderWrapper.h"

class SLSource{
public:
    virtual ~SLSource(){};
    virtual bool Open(){
        return false;
    };
    virtual bool Close(){
        return false;
    };

    virtual PCMBuffer*& GetBuffer() = 0;

    virtual void ReleaseBuffer() = 0;

    // sample rate in millisecond
    SLuint32 GetSLSampleRate() const {
        // 44100 * 1000 = SL_SAMPLINGRATE_44_1;
        return (SLuint32)(sampleRate * 1000);
    }

    SLuint16 GetSLSampleFormat() const {
        switch (sampleFormat) {
            case AV_SAMPLE_FMT_U8:
                return SL_PCMSAMPLEFORMAT_FIXED_8;
            case AV_SAMPLE_FMT_S16:
                return SL_PCMSAMPLEFORMAT_FIXED_16;
            case AV_SAMPLE_FMT_S32:
                return SL_PCMSAMPLEFORMAT_FIXED_32;
        }
        return SL_PCMSAMPLEFORMAT_FIXED_16;
    }

    SLuint32 GetChannels() const {
        return (SLuint32)channels;
    }

    SLuint32 GetSLChannelLayout() const {
        // AV_CH_FRONT_LEFT      至 AV_CH_TOP_BACK_RIGHT  和
        // SL_SPEAKER_FRONT_LEFT 至 SL_SPEAKER_TOP_BACK_RIGHT 一致

        // AV_CH_LAYOUT_MONO | AV_CH_LAYOUT_STEREO | AV_CH_LAYOUT_2_1 | AV_CH_LAYOUT_2_2 等一致, 如:
        // AV_CH_LAYOUT_MONO ->   SL_SPEAKER_FRONT_CENTER,
        // AV_CH_LAYOUT_STEREO -> SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
        return (SLuint32)channel_layout;
    }

    int GetBitRate() const {
        return bitRate;
    }

    long long GetDuration(){
        return duration;
    }
protected:
    long long duration{0};
    int sampleRate{0};
    AVSampleFormat sampleFormat{AV_SAMPLE_FMT_NONE};
    int channels{0};
    uint64_t channel_layout{0};
    int bitRate;
};

class DecoderSource: public SLSource {
public:
    DecoderSource(const char *url, ProduceListener* listener = nullptr);

    ~DecoderSource();

    bool Open() override;

    PCMBuffer *&GetBuffer() override;

    void ReleaseBuffer() override;

    bool Close() override;

private:
    DecoderWrapper* decoder{nullptr};
};


#endif //WHEELKIT_SLSOURCE_H
