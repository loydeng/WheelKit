//
// Created by loyde on 2023/1/24.
//

#ifndef WHEELKIT_SLWRAPPER_H
#define WHEELKIT_SLWRAPPER_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <jni.h>
#include "DecoderWrapper.h"

// 阻塞方式实现 SL Object 初始化
#define INIT_OBJ_RETURN(obj, ...) \
({                                \
SLresult ret = (*obj)->Realize(obj, SL_BOOLEAN_FALSE); \
int err=(ret != SL_RESULT_SUCCESS);                 \
if(err){                          \
    FORMAT_E(SL_MSG_TEMPLATE, __func__, ret, __FILE_NAME, __LINE__) \
    DELETE_OBJ(obj);  \
    obj = NULL;       \
    return __VA_ARGS__;           \
};                                \
});
#define DELETE_OBJ(obj) (*obj)->Destroy(obj);

#define SL_MSG_TEMPLATE "%s get error return %d at (%s:%d)"

#define CHECK_FAILURE(ret) \
({                                  \
    int err=(ret != SL_RESULT_SUCCESS); \
    if(err){                        \
        FORMAT_E(SL_MSG_TEMPLATE, __func__, ret, __FILE_NAME, __LINE__) \
    }                               \
    err;             \
})

#define ASSERT_OK_RETURN(result, ...) if(CHECK_FAILURE(result)){ \
    (void)result;                                                \
    return __VA_ARGS__;                                          \
}

class SLBase {
public:
    SLBase();

    virtual ~SLBase();

    virtual void Init();

protected:
    SLObjectItf engineObj{nullptr};
    SLEngineItf engineItf{nullptr};
    SLObjectItf mixObj{nullptr};
    SLEnvironmentalReverbItf environmentalReverbItf{nullptr};
};

class SLPlayer : public SLBase {
public:
    SLPlayer(jobject javaObj);

    virtual ~SLPlayer();

    void Init() override;

    bool IsPlaying() const;

    virtual bool Play() = 0;

    virtual bool Pause();

    virtual bool Stop();

    void OnFinish();

    void OnProgress(long long currentPos, long long duration);

    int Mute(int channel, bool mute);

    int Solo(int channel, bool solo);

    int GetChannel(uint8_t *num);

    int GetMaxVolume();

    int GetVolume();

    bool SetVolume(int milli_bel);

    bool SetMute(bool mute);

    int EnableStereoPosition(bool enable);

    int SetStereoPosition(int per_mille);

    double & GetCurrentTime();

protected:
    void SetCallbackEvent();
    bool SetState(SLuint32 state);
    JNIEnv *env;
    jobject callback{nullptr};
    int playState;
    double currentTime{0};
    SLObjectItf playerObj = nullptr;
    SLPlayItf playItf = nullptr;
    SLMuteSoloItf muteSoloItf = nullptr;
    SLVolumeItf volumeItf = nullptr;
    SLDataLocator_OutputMix sinkLocator;
    SLDataSink dataSink;
};

/**
 * 用于播放音频文件, 如: mp3, wav, ogg, flac, aac(AAC LC)
 * 支持格式参考: https://developer.android.com/ndk/guides/audio/opensl/opensl-for-android?hl=zh-cn#mime-data-format
 */
class SLFilePlayer : public SLPlayer {
public:
    SLFilePlayer(char *path, jobject javaObj);

    ~SLFilePlayer();

    // set the whole file looping state for the URI audio player
    bool SetLoopPlay(bool loop);

    bool Seek(int millisecond);
public:
    void Init() override;
    bool Play() override;
    int GetCurrentPosition();
private:
    char *path{nullptr};
    SLSeekItf seekItf{nullptr};

    void SetDuration();
};

class SLSource{
public:
    virtual ~SLSource(){};
    virtual bool Open(){
        return false;
    };
    virtual bool Close(){
        return false;
    };
    virtual int GetBuffer(uint8_t* &buffer, int& capacity) = 0;

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
        // AV_CH_LAYOUT_MONO | AV_CH_LAYOUT_STEREO | AV_CH_LAYOUT_2_1 | AV_CH_LAYOUT_2_2 等基本一致
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

/**
 * pcm 输入文件
 */
class SLFileSource: public SLSource {
public:
    SLFileSource(const char * inputPath, int sampleRate, AVSampleFormat sampleFormat, int channels);

    ~SLFileSource();

    bool Open() override;

    int GetBuffer(uint8_t* &buffer, int& capacity) override;

    bool Close() override;

private:
    char *path{nullptr};
    FILE *file{nullptr};
};

class DecoderSource: public SLSource {
public:
    DecoderSource(const char *url);

    ~DecoderSource();

    bool Open() override;

    int GetBuffer(uint8_t* &buffer, int& capacity) override;

    bool Close() override;

private:
    DecoderWrapper* decoder{nullptr};
};

/**
 * 用于播放音频原始数据, PCM格式
 */
class SLBufferPlayer : public SLPlayer {
public:
    const static int DEFAULT_BUFF_SIZE = 2 * 4800 * 2;

    SLBufferPlayer(SLSource *source, jobject javaObj, int size = DEFAULT_BUFF_SIZE);

    ~SLBufferPlayer();

    SLSource*& GetSource();

    void Init() override;
    bool Play() override;
    bool Stop() override;

private:
    SLSource *source{nullptr};
    uint8_t *buff{nullptr};
    int buffSize{0};

public:

    SLAndroidSimpleBufferQueueItf &GetBuffQueueItf();

    int &GetBuffSize();

    uint8_t *&GetBuff();

private:
    SLAndroidSimpleBufferQueueItf bufferQueueItf{nullptr};
    SLEffectSendItf effectSendItf{nullptr};
};

class SLSink{
public:
    virtual ~SLSink(){};
    virtual bool Open() = 0;
    virtual bool Close() = 0;
    virtual int PutBuffer(uint8_t*& buffer, int& capacity) = 0;
};

/**
 * pcm 输出文件
 */
class SLFileSink: public SLSink{
public:
    SLFileSink(const char * outputPath);

    ~SLFileSink();

    bool Open() override;

    int PutBuffer(uint8_t *&buffer, int &capacity) override;

    bool Close() override;

private:
    char *path = nullptr;
    FILE *file = nullptr;
};

/**
 * SL 只能录制输出音频原始数据 PCM
 */
class SLRecorder : public SLBase {
public:
    const static int DEFAULT_BUFF_SIZE = 2 * 4800 * 2;

    SLRecorder(SLSink *sink, int size = DEFAULT_BUFF_SIZE);

    ~SLRecorder();
    // create audio recorder: recorder is not in fast path
    //    like to avoid excessive re-sampling while playing back from Hello &
    //    Android clip
    void Init() override;
    // set the recording state for the audio recorder
    void Record();

    // Pause Record, can resume
    void Pause();

    void Stop();

    bool IsRecording();

    int &GetBuffSize();

    uint8_t *&GetBuffer();

    SLSink*& GetSink();

    SLAndroidSimpleBufferQueueItf &GetBufferQueueItf();
private:
    SLObjectItf recorderObj{nullptr};
    SLRecordItf recordItf{nullptr};
    SLAndroidSimpleBufferQueueItf bufferQueueItf{nullptr};
    uint8_t *buff{nullptr};
    int buffSize{0};
    int recordStat{0};
    SLSink *sink{nullptr};
};

#endif //WHEELKIT_SLWRAPPER_H
