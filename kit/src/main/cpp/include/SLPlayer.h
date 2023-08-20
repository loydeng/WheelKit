//
// Created by loyde on 2023/2/5.
//

#ifndef WHEELKIT_SLPLAYER_H
#define WHEELKIT_SLPLAYER_H

#include "SLSource.h"

class SLPlayer : public SLBase {
public:
    class Event {
    private:
        jobject callback;
    public:
        Event(const jobject callback) : callback(callback) {}

        virtual void OnLoad(bool isLoading){
            JNIEnv *pEnv = nullptr;
            jint  ret = util_wrapper::JNI::GetEnv(&pEnv);
            jclass clazz = pEnv->GetObjectClass(callback);
            jmethodID mId = pEnv->GetMethodID(clazz, "onLoading", "(Z)V");
            pEnv->CallVoidMethod(callback, mId, isLoading);
            if (ret) {
                util_wrapper::JNI::FreeEnv(&pEnv);
            }
        };

        virtual void OnStart() {};

        virtual void OnPause() {};

        virtual void OnStop(bool isFinish) {
            JNIEnv *pEnv = nullptr;
            jint  ret = util_wrapper::JNI::GetEnv(&pEnv);
            jclass clazz = pEnv->GetObjectClass(callback);
            jmethodID mId = pEnv->GetMethodID(clazz, "onPlayFinish", "(Z)V");
            pEnv->CallVoidMethod(callback, mId, isFinish);
            if (ret) {
                util_wrapper::JNI::FreeEnv(&pEnv);
            }
        };

        virtual void OnProgress(long currentPos, long duration){
            JNIEnv *pEnv = nullptr;
            jint  ret = util_wrapper::JNI::GetEnv(&pEnv);
            jclass clazz = pEnv->GetObjectClass(callback);
            jmethodID mId = pEnv->GetMethodID(clazz, "onUpdateProgress", "(JJ)V");
            pEnv->CallVoidMethod(callback, mId, currentPos, duration);
            if (ret) {
                util_wrapper::JNI::FreeEnv(&pEnv);
            }
        };
    };

    SLPlayer();

    virtual ~SLPlayer();

    void Prepare() override;

    bool IsPlaying() ;

    SLuint32 GetState() const;

    virtual bool Play() = 0;

    virtual bool PauseOrResume();

    virtual bool Stop();

    void SetLoadState(bool isLoading);

    int Mute(int channel, bool mute);

    int Solo(int channel, bool solo);

    int GetChannel(uint8_t *num);

    short GetMaxVolume();

    int GetVolume();

    bool SetVolume(short milli_bel);

    bool SetMute(bool mute);

    int EnableStereoPosition(bool enable);

    int SetStereoPosition(short per_mille);

    double &GetCurrentTime();

protected:
    void SetCallbackEvent();

    bool SetState(SLuint32 state);

public:
    Event *GetEvent() const;

protected:
    Event *event{nullptr};
    SLuint32 state{SL_PLAYSTATE_STOPPED};
    double currentTime{0};
    SLObjectItf playerObj{nullptr};
    SLPlayItf playItf{nullptr};
    SLMuteSoloItf muteSoloItf{nullptr};
    SLVolumeItf volumeItf{nullptr};
    SLDataLocator_OutputMix sinkLocator{};
    SLDataSink dataSink{};
};

/**
 * 用于播放音频文件, 如: mp3, wav, ogg, flac, aac(AAC LC)
 * 支持格式参考: https://developer.android.com/ndk/guides/audio/opensl/opensl-for-android?hl=zh-cn#mime-data-format
 */
class SLFilePlayer : public SLPlayer {
public:
    SLFilePlayer(Event *event);

    ~SLFilePlayer();

    void Prepare(const char *path);

    // set the whole file looping state for the URI audio player
    bool SetLoopPlay(bool loop);

    bool Seek(int millisecond);

public:
    bool Play() override;

    int GetCurrentPosition();

private:
    SLSeekItf seekItf{nullptr};

    void SetDuration();

};

/**
 * 用于播放音频原始数据, PCM格式
 */
class SLBufferPlayer : public SLPlayer {
public:
    SLBufferPlayer(Event *event = nullptr);

    ~SLBufferPlayer();

    void Prepare(DecoderSource *src);

    bool Play() override;

    bool Stop() override;

    DecoderSource *&GetSource();

    void SetNextSource(DecoderSource *next);

    void NextPlay();
private:
    inline bool CanReuse(SLSource *newSource);


    static SLDataFormat_PCM GetFormatFromSource(SLSource *source) {
        SLDataFormat_PCM format{
                SL_DATAFORMAT_PCM,
                source->GetChannels(),
                source->GetSLSampleRate(),
                source->GetSLSampleFormat(),
                source->GetSLSampleFormat(),
                source->GetSLChannelLayout(),
                SL_BYTEORDER_LITTLEENDIAN
        };
        return format;
    };

    void (*playCallback)(SLAndroidSimpleBufferQueueItf, void *){nullptr};
    DecoderSource *source{nullptr};
    DecoderSource *nextSource{nullptr};
    SLAndroidSimpleBufferQueueItf bufferQueueItf{nullptr};
};

#endif //WHEELKIT_SLPLAYER_H
