//
// Created by loyde on 2023/1/24.
//
#include "include/SLWrapper.h"
#include <sys/stat.h>

SLBase::SLBase() {}

SLBase::~SLBase() {
    TRACE()
    DELETE_OBJ(mixObj)
    DELETE_OBJ(engineObj)
}

void SLBase::Init() {

    slCreateEngine(&engineObj, 0, nullptr, 0, nullptr, nullptr);

    INIT_OBJ_RETURN(engineObj)

    SLresult result = (*engineObj)->GetInterface(engineObj, SL_IID_ENGINE, &engineItf);
    ASSERT_OK_RETURN(result)
    (*engineItf)->CreateOutputMix(engineItf, &mixObj, 0, NULL, NULL);
    INIT_OBJ_RETURN(mixObj)

    /*
    // create output mix, with environmental reverb specified as a non-required interface
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    (*engineItf)->CreateOutputMix(engineItf, &mixObj, 1, ids, req);
    INIT_OBJ_RETURN(mixObj)

    // get the environmental reverb interface, this could fail if the environmental reverb effect is not available,
    // either because the feature is not present, excessive CPU load,
    // or the required MODIFY_AUDIO_SETTINGS permission was not requested and granted
    // or we are in fast audio, reverb is not supported.
    result = (*mixObj)->GetInterface(mixObj, SL_IID_ENVIRONMENTALREVERB, &environmentalReverbItf);
    if (CHECK_FAILURE(result)) {
        E("the environmental reverb effect is not available");
    } else {
        const SLEnvironmentalReverbSettings settings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
        result = (*environmentalReverbItf)->SetEnvironmentalReverbProperties(environmentalReverbItf, &settings);
        ASSERT_OK_RETURN(result)
    }*/
}

SLPlayer::SLPlayer(jobject javaObj) {
    util_wrapper::JNI::GetEnv(&env);
    this->callback = env->NewGlobalRef(javaObj);
    playState = SL_PLAYSTATE_STOPPED;
}

SLPlayer::~SLPlayer() {
    TRACE()
    DELETE_OBJ(playerObj)
    env->DeleteGlobalRef(callback);
    util_wrapper::JNI::FreeEnv(&env);
}

long long cTime = 0;
void event_callback (SLPlayItf caller, void *pContext, SLuint32 event){
    SLPlayer *player = static_cast<SLPlayer *>(pContext);

    switch (event) {
        case SL_PLAYEVENT_HEADATEND:
            if (cTime == 0) {
                FORMAT_E("start to play...")
            } else {
                FORMAT_E("finish to play, duration: %lld", cTime)
                cTime = 0;
            }
            break;
        case SL_PLAYEVENT_HEADATNEWPOS:
            cTime+=1000;
            FORMAT_E("progress in %lld", cTime)
            break;
        case SL_PLAYEVENT_HEADATMARKER:
            LOCATE_E("Play status: SL_PLAYEVENT_HEADATMARKER")
            break;
        case SL_PLAYEVENT_HEADMOVING:
            LOCATE_E("Play status: SL_PLAYEVENT_HEADMOVING")
            break;
        case SL_PLAYEVENT_HEADSTALLED: // 停止
            LOCATE_E("Play status: SL_PLAYEVENT_HEADSTALLED")
            break;
    }
}


void SLPlayer::SetCallbackEvent() {
    if (playItf) {
        // On Android 2.3, I get an SL_PLAYEVENT_HEADATEND event when it reaches the end of the file (whether the player is set to loop or not).
        // On Android 4.0, I only get it when the file is not set to loop. For now I suggest a workaround such as monitoring the Play position.
        (*playItf)->RegisterCallback(playItf, event_callback, this);
        //(*playItf)->SetMarkerPosition(playItf, 60000);
        // UpdatePeriod default is 1000 ms;
        // (*playItf)->SetPositionUpdatePeriod(playItf, 1000);
        (*playItf)->SetCallbackEventsMask(playItf, SL_PLAYEVENT_HEADATEND|SL_PLAYEVENT_HEADATMARKER|
                                                   SL_PLAYEVENT_HEADATNEWPOS|SL_PLAYEVENT_HEADSTALLED|SL_PLAYEVENT_HEADMOVING);
    }
}

bool SLPlayer::SetState(SLuint32 state) {
    SLresult result = (*playItf)->SetPlayState(playItf, state);
    if (result == SL_RESULT_SUCCESS) {
        playState = (int)state;
    }
    return result == SL_RESULT_SUCCESS;
}

bool SLPlayer::IsPlaying() const {
    return playState == SL_PLAYSTATE_PLAYING;
}

void SLPlayer::Init() {
    SLBase::Init();

    // configure audio sink
    sinkLocator = {SL_DATALOCATOR_OUTPUTMIX, mixObj};
    dataSink = {&sinkLocator, NULL};
}

bool SLPlayer::Pause() {
    TRACE()
    if (playState == SL_PLAYSTATE_PLAYING) {
        return SetState(SL_PLAYSTATE_PAUSED);
    }
    return false;
}

bool SLPlayer::Stop() {
    if (playState == SL_PLAYSTATE_PLAYING || playState == SL_PLAYSTATE_PAUSED) {
        return SetState(SL_PLAYSTATE_STOPPED);
    }
    return false;
}

void SLPlayer::OnFinish() {
    JNIEnv *env = nullptr;

    util_wrapper::JNI::GetEnv(&env);

    jclass clazz = env->GetObjectClass(callback);
    jmethodID mId = env->GetMethodID(clazz, "onPlayFinish", "()V");
    env->CallVoidMethod(callback, mId);

    util_wrapper::JNI::FreeEnv(&env);
}

void SLPlayer::OnProgress(long long current, long long total) {

    JNIEnv *env = nullptr;

    util_wrapper::JNI::GetEnv(&env);

    jclass clazz = env->GetObjectClass(callback);
    jmethodID mId = env->GetMethodID(clazz, "onUpdateProgress", "(JJ)V");
    env->CallVoidMethod(callback, mId, current, total);

    util_wrapper::JNI::FreeEnv(&env);
}

int SLPlayer::Mute(int channel, bool mute) {
    if (muteSoloItf) {
        SLresult result = (*muteSoloItf)->SetChannelMute(muteSoloItf, channel, mute);
        return result == SL_RESULT_SUCCESS;
    }
    return 0;
}

int SLPlayer::Solo(int channel, bool solo) {
    if (muteSoloItf) {
        SLresult result = (*muteSoloItf)->SetChannelSolo(muteSoloItf, channel, solo);
        return result == SL_RESULT_SUCCESS;
    }
    return 0;
}

int SLPlayer::GetChannel(uint8_t *num) {
    if (muteSoloItf) {
        SLresult result = (*muteSoloItf)->GetNumChannels(muteSoloItf, num);
        return result == SL_RESULT_SUCCESS;
    }
    return 0;
}

int SLPlayer::GetMaxVolume() {
    if (volumeItf) {
        SLmillibel milli_bel;
        SLresult result = (*volumeItf)->GetMaxVolumeLevel(volumeItf, &milli_bel);
        if (result == SL_RESULT_SUCCESS){
            return milli_bel;
        }
    }
    return 0;
}

int SLPlayer::GetVolume() {
    if (volumeItf) {
        SLmillibel milli_bel;
        SLresult result = (*volumeItf)->GetVolumeLevel(volumeItf, &milli_bel);
        if (result == SL_RESULT_SUCCESS){
            return milli_bel;
        }
    }
    return 0;
}

bool SLPlayer::SetVolume(int milli_bel) {
    if (volumeItf) {
        SLresult result = (*volumeItf)->SetVolumeLevel(volumeItf, milli_bel);
        return result == SL_RESULT_SUCCESS;
    }
    return false;
}

bool SLPlayer::SetMute(bool mute) {
    if (volumeItf) {
        SLresult result = (*volumeItf)->SetMute(volumeItf, mute);
        return result == SL_RESULT_SUCCESS;
    }
    return false;
}

int SLPlayer::EnableStereoPosition(bool enable) {
    if (volumeItf) {
        SLresult result = (*volumeItf)->EnableStereoPosition(volumeItf, enable);
        return result == SL_RESULT_SUCCESS;
    }
    return 0;
}

int SLPlayer::SetStereoPosition(int per_mille) {
    if (volumeItf) {
        SLresult result = (*volumeItf)->SetStereoPosition(volumeItf, per_mille);
        return result == SL_RESULT_SUCCESS;
    }
    return 0;
}

double & SLPlayer::GetCurrentTime() {
    return currentTime;
}

SLFilePlayer::SLFilePlayer(char *path, jobject javaObj): SLPlayer(javaObj) {
    this->path = new char[strlen(path)];
    strcpy(this->path, path);
}

SLFilePlayer::~SLFilePlayer() {
    TRACE()
    delete[] path;
}

void SLFilePlayer::Init() {
    SLPlayer::Init();

    SLresult result;

    // configure audio source
    // (requires the INTERNET permission depending on the uri parameter)
    SLDataLocator_URI loc_uri = {SL_DATALOCATOR_URI, reinterpret_cast<SLchar *>(path)};
    SLDataFormat_MIME format_mime = {SL_DATAFORMAT_MIME, NULL, SL_CONTAINERTYPE_UNSPECIFIED};
    SLDataSource dataSource = {&loc_uri, &format_mime};

    // create audio player
    const SLInterfaceID ids[3] = {SL_IID_SEEK, SL_IID_MUTESOLO, SL_IID_VOLUME};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*engineItf)->CreateAudioPlayer(engineItf, &playerObj, &dataSource, &dataSink, 3, ids, req);
    // note that an invalid URI is not detected here, but during prepare/prefetch on Android, or possibly during Realize on other platforms
    ASSERT_OK_RETURN(result)

    // realize the player
    INIT_OBJ_RETURN(playerObj)

    // get the Play interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_PLAY, &playItf);
    ASSERT_OK_RETURN(result)

    SetCallbackEvent();

    // get the seek interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_SEEK, &seekItf);
    ASSERT_OK_RETURN(result)

    // get the Mute/Solo interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_MUTESOLO, &muteSoloItf);
    ASSERT_OK_RETURN(result);

    // get the volume interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_VOLUME, &volumeItf);
    ASSERT_OK_RETURN(result);

}

bool SLFilePlayer::Play() {
    TRACE()
    if (playState == SL_PLAYSTATE_STOPPED || playState == SL_PLAYSTATE_PAUSED) {
        return SetState(SL_PLAYSTATE_PLAYING);
    }
    return false;
}

int SLFilePlayer::GetCurrentPosition() {
    TRACE()
    if (playItf) {
        SLmillisecond ms;
        SLresult  result = (*playItf)->GetPosition(playItf, &ms);
        if (result == SL_RESULT_SUCCESS) {
            return (int)ms;
        }
    }
    return -1;
}

bool SLFilePlayer::SetLoopPlay(bool loop) {
    if (seekItf) {
        SLresult result = (*seekItf)->SetLoop(seekItf, (SLboolean) loop, 0, SL_TIME_UNKNOWN);
        return result == SL_RESULT_SUCCESS;
    }
    return false;
}

bool SLFilePlayer::Seek(int millisecond) {
    if (seekItf) {
        // SL_SEEKMODE_FAST or SL_SEEKMODE_ACCURATE
        SLresult result = (*seekItf)->SetPosition(seekItf, millisecond, SL_SEEKMODE_FAST);
        return result == SL_RESULT_SUCCESS;
    }
    return false;
}

void play_callback(SLAndroidSimpleBufferQueueItf caller, void *pContext) {
    SLBufferPlayer *player = static_cast<SLBufferPlayer *>(pContext);

    int size = player->GetSource()->GetBuffer(player->GetBuff(),
                                              player->GetBuffSize());
    if (size > 0) {
        (*player->GetBuffQueueItf())->Enqueue(player->GetBuffQueueItf(),
                                              player->GetBuff(), size);

        double add = size * 1000.0 / player->GetSource()->GetBitRate();
        double now = player->GetCurrentTime() + add;
        if ((int)now / 1000 - (int)player->GetCurrentTime() / 1000 > 0) {
            //FORMAT_E("currentTime:%f", now)
            player->OnProgress((long long )now, player->GetSource()->GetDuration());
        }
        player->GetCurrentTime() = now;
    } else {
        player->OnProgress(player->GetSource()->GetDuration(), player->GetSource()->GetDuration());
        player->Stop();
    }
}

SLBufferPlayer::SLBufferPlayer(SLSource *source, jobject javaObj, int size): SLPlayer(javaObj) {
    this->source = source;
    this->buffSize = size;
    this->buff = new uint8_t[buffSize];
}

SLBufferPlayer::~SLBufferPlayer() {
    TRACE()
    delete[] buff;
    delete source;
}

void SLBufferPlayer::Init() {
    SLPlayer::Init();

    SLresult result;

    SLDataLocator_AndroidSimpleBufferQueue locator = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            2};

    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM,
            source->GetChannels(),
            source->GetSLSampleRate(),
            source->GetSLSampleFormat(),
            source->GetSLSampleFormat(),
            source->GetSLChannelLayout(),
            SL_BYTEORDER_LITTLEENDIAN
    };

    SLDataSource dataSource = {&locator, &format_pcm};


    // fast audio does not support when SL_IID_EFFECTSEND is required, skip it for fast audio case
    const SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME, SL_IID_EFFECTSEND,
            /*SL_IID_MUTESOLO,*/};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE,
            /*SL_BOOLEAN_TRUE,*/};

    result = (*engineItf)->CreateAudioPlayer(engineItf, &playerObj, &dataSource, &dataSink,
                                             (format_pcm.samplesPerSec > SL_SAMPLINGRATE_8 ? 2 : 3),
                                             ids, req);
    ASSERT_OK_RETURN(result)

    // realize the player
    INIT_OBJ_RETURN(playerObj)

    // get the buffer queue interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_BUFFERQUEUE, &bufferQueueItf);
    ASSERT_OK_RETURN(result)

    // register callback on the buffer queue
    result = (*bufferQueueItf)->RegisterCallback(bufferQueueItf, play_callback, this);
    ASSERT_OK_RETURN(result)

    // get the Play interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_PLAY, &playItf);
    ASSERT_OK_RETURN(result)

    // 基于SL自身API获取不到duration, 进度也不够精准
    //SetCallbackEvent();

    // get the effect send interface
    effectSendItf = NULL;
    if (format_pcm.samplesPerSec == SL_SAMPLINGRATE_8) {
        result = (*playerObj)->GetInterface(playerObj, SL_IID_EFFECTSEND, &effectSendItf);
        ASSERT_OK_RETURN(result)
    }

    // Mute/Solo is not supported for sources that are known to be mono,
    // asthis is get the Mute/Solo interface
    // result = (*playerObj)->GetInterface(playerObj, SL_IID_MUTESOLO, &muteSoloItf);
    // ASSERT_OK_RETURN(result)

    // get the volume interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_VOLUME, &volumeItf);
    ASSERT_OK_RETURN(result);

    // set the player's state to playing
    result = (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING);
    ASSERT_OK_RETURN(result);
}

bool SLBufferPlayer::Play() {
    TRACE()
    if (playState == SL_PLAYSTATE_PLAYING) {
        return false;
    }
    if (playState == SL_PLAYSTATE_STOPPED) {
        ASSERT_COND_OR_RETURN(source->Open(), false)
    }
    ASSERT_COND_OR_RETURN(SetState(SL_PLAYSTATE_PLAYING), false);
    play_callback(bufferQueueItf, this);
    return true;
}

int &SLBufferPlayer::GetBuffSize() {
    return buffSize;
}

uint8_t *&SLBufferPlayer::GetBuff() {
    return buff;
}

SLAndroidSimpleBufferQueueItf &SLBufferPlayer::GetBuffQueueItf() {
    return bufferQueueItf;
}

SLSource *&SLBufferPlayer::GetSource() {
    return source;
}

bool SLBufferPlayer::Stop() {
    TRACE()
    if (SLPlayer::Stop()){
        currentTime = 0;
        source->Close();
        OnFinish();
        return true;
    }
    return false;
}

SLFileSource::SLFileSource(const char *inputPath, int sampleRate, AVSampleFormat sampleFormat, int channels) {
    path = new char[strlen(inputPath)];
    strcpy(path, inputPath);
    this->sampleRate = sampleRate;
    this->sampleFormat = sampleFormat;
    this->channels = channels;
    this->channel_layout = av_get_default_channel_layout(channels);
    this->bitRate = sampleRate * channels * av_get_bytes_per_sample(sampleFormat);
    struct stat fileStat{};
    stat(path, &fileStat);
    this->duration = fileStat.st_size * 1000 / bitRate; // millisecond
}

SLFileSource::~SLFileSource() {
    TRACE()
    delete[] path;
    if (file) {
        if (fclose(file)){
            LOCATE_E("close source file error")
        }
    }
}

int SLFileSource::GetBuffer(uint8_t *&buffer, int &capacity) {
    return fread(buffer, 1, capacity, file);
}

bool SLFileSource::Open() {
    if (file) {
        if (fclose(file)){
            LOCATE_E("close source file error")
        }
    }
    file = fopen(path, "r");
    return file != nullptr;
}

bool SLFileSource::Close() {
    if (file) {
        if (fclose(file)) {
            LOCATE_E("close source file error")
            return false;
        } else {
            file = nullptr;
        }
    }
    return true;
}

void record_callback(SLAndroidSimpleBufferQueueItf caller, void *pContext) {
    auto *recorder = static_cast<SLRecorder *>(pContext);
    recorder->GetSink()->PutBuffer(recorder->GetBuffer(), recorder->GetBuffSize());
    if (recorder->IsRecording()) {
        SLAndroidSimpleBufferQueueItf &bufferQueueItf = recorder->GetBufferQueueItf();
        SLresult result = (*bufferQueueItf)->Enqueue(bufferQueueItf, recorder->GetBuffer(),
                                                     recorder->GetBuffSize());
        ASSERT_OK_RETURN(result)
    }
}

void SLRecorder::Init() {
    SLBase::Init();

    SLresult result;

    // configure audio source
    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE,
                                      SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT,
                                      NULL};
    SLDataSource audioSrc = {&loc_dev, NULL};

    // configure audio sink
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM,
            2,
            SL_SAMPLINGRATE_44_1,
            SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
            SL_BYTEORDER_LITTLEENDIAN};
    SLDataSink dataSink = {&loc_bq, &format_pcm};

    // create audio recorder
    // (requires the RECORD_AUDIO permission)
    const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};
    result = (*engineItf)->CreateAudioRecorder(engineItf, &recorderObj, &audioSrc, &dataSink, 1, id,
                                               req);
    ASSERT_OK_RETURN(result)

    // realize the audio recorder
    INIT_OBJ_RETURN(recorderObj)

    // get the Record interface
    result = (*recorderObj)->GetInterface(recorderObj, SL_IID_RECORD, &recordItf);
    ASSERT_OK_RETURN(result)

    // get the buffer queue interface
    result = (*recorderObj)->GetInterface(recorderObj, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                          &bufferQueueItf);
    ASSERT_OK_RETURN(result)

    // register callback on the buffer queue
    result = (*bufferQueueItf)->RegisterCallback(bufferQueueItf, record_callback, this);
    ASSERT_OK_RETURN(result)
}

SLRecorder::SLRecorder(SLSink *sink, int size) {
    this->sink = sink;
    buffSize = size;
    buff = new uint8_t[size];
    recordStat = SL_RECORDSTATE_STOPPED;
}

SLRecorder::~SLRecorder() {
    TRACE()
    delete[] buff;
    delete sink;
    DELETE_OBJ(recorderObj)
}

void SLRecorder::Record() {
    TRACE()
    // in case already recording
    if (recordStat == SL_RECORDSTATE_RECORDING) {
        LOCATE_W("SLRecorder is already recording !")
        return;
    }
    SLresult result;
    if (recordStat == SL_RECORDSTATE_STOPPED) {
        // clear buffer queue
        result = (*bufferQueueItf)->Clear(bufferQueueItf);
        ASSERT_OK_RETURN(result)

        // enqueue an empty buffer to be filled by the recorder
        // (for streaming recording, we would enqueue at least 2 empty buffers to
        // start things off)
        result = (*bufferQueueItf)->Enqueue(bufferQueueItf, buff, buffSize);
        // the most likely other result is SL_RESULT_BUFFER_INSUFFICIENT,
        // which for this code example would indicate a programming error
        ASSERT_OK_RETURN(result)

        bool is_open = sink->Open();
        ASSERT_COND_OR_RETURN(is_open)
    }
    // start recording
    result = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_RECORDING);
    if (result == SL_RESULT_SUCCESS) {
        recordStat = SL_RECORDSTATE_RECORDING;
    } else {
        FORMAT_E("unable to Record, error code: %d", result)
    }
}

void SLRecorder::Pause() {
    TRACE()
    if (recordStat == SL_RECORDSTATE_RECORDING) {
        SLresult result = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_PAUSED);
        if (result == SL_RESULT_SUCCESS) {
            recordStat = SL_RECORDSTATE_PAUSED;
        }
    }
}

void SLRecorder::Stop() {
    TRACE()
    if (recordStat != SL_RECORDSTATE_STOPPED) {
        SLresult result = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_STOPPED);
        if (result == SL_RESULT_SUCCESS) {
            recordStat = SL_RECORDSTATE_STOPPED;
            sink->Close();
        }
    }
}

int &SLRecorder::GetBuffSize() {
    return buffSize;
}

uint8_t *&SLRecorder::GetBuffer() {
    return buff;
}

SLSink *&SLRecorder::GetSink() {
    return sink;
}

bool SLRecorder::IsRecording() {
    return recordStat == SL_RECORDSTATE_RECORDING;
}

SLAndroidSimpleBufferQueueItf &SLRecorder::GetBufferQueueItf() {
    return bufferQueueItf;
}

SLFileSink::SLFileSink(const char *outputPath) {
    path = new char[strlen(outputPath)];
    strcpy(path, outputPath);
}

SLFileSink::~SLFileSink() {
    TRACE()
    delete[] path;
    if (file) {
        fclose(file);
    }
}

int SLFileSink::PutBuffer(uint8_t *&buffer, int &capacity) {
    size_t size = fwrite(buffer, 1, capacity, file);
    if (size != capacity) {
        FORMAT_E("PutBuffer error %d to file : %s", (int )size, path)
    }
    return (int) size;
}

bool SLFileSink::Open() {
    if (file) {
        if (fclose(file)){
            LOCATE_E("close sink file error")
        }
    }
    file = fopen(path, "w");
    return file != nullptr;
}

bool SLFileSink::Close() {
    if (file) {
        if (fclose(file)) {
            LOCATE_E("close sink file error")
            return false;
        } else {
            file = nullptr;
        }
    }
    return true;
}

DecoderSource::DecoderSource(const char *url) {
    DemuxerWrapper *demuxerWrapper = new DemuxerWrapper(url);
    demuxerWrapper->Init();
    this->duration = demuxerWrapper->GetDuration();
    decoder = new DecoderWrapper(demuxerWrapper);
    // 未修改采用率, 保持和输入一致. 不会导致变速
    this->sampleRate = demuxerWrapper->GetAudioCodecCtx()->sample_rate;
    this->channel_layout = decoder->GetChannelLayout();
    this->channels = av_get_channel_layout_nb_channels(channel_layout);
    this->sampleFormat = decoder->GetSampleFormat();
    this->bitRate = av_get_bytes_per_sample(sampleFormat) * channels * sampleRate;
}

DecoderSource::~DecoderSource() {
    delete decoder;
}

int DecoderSource::GetBuffer(uint8_t *&buffer, int &capacity) {
    PCMBuffer* &pcmBuffer = decoder->GetPCMQueue()->Consume();
    int size = pcmBuffer->GetSize();
    if (size > 0) {
        memcpy(buffer, pcmBuffer->GetData(), size);
    }
    decoder->GetPCMQueue()->ConsumeDone();
    return size;
}

bool DecoderSource::Open() {
    return decoder->StartDecode();
}

bool DecoderSource::Close() {
    return decoder->StopDecode();
}


