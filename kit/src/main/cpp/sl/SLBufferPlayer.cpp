//
// Created by loyde on 2023/2/5.
//
#include "SLPlayer.h"

SLBufferPlayer::SLBufferPlayer(Event *event) {
    this->event = event;
    playCallback = [](SLAndroidSimpleBufferQueueItf caller, void *pContext) -> void {
        auto *player = static_cast<SLBufferPlayer *>(pContext);

        PCMBuffer *&buffer = player->GetSource()->GetBuffer();
        int &size = buffer->GetSize();
        long long int duration = player->GetSource()->GetDuration();
        Event *pEvent = player->GetEvent();
        if (size > 0) {
            (*caller)->Enqueue(caller, buffer->GetData(), size);
            player->GetSource()->ReleaseBuffer();
            double add = size * 1000.0 / player->GetSource()->GetBitRate();
            double now = player->GetCurrentTime() + add;
            if ((int) now / 1000 - (int) player->GetCurrentTime() / 1000 > 0) {
                pEvent->OnProgress((long long) now, duration);
            }
            player->GetCurrentTime() = now;
        } else {
            player->GetSource()->ReleaseBuffer();
            if (pEvent) {
                pEvent->OnProgress(duration, duration);
            }
            player->SLPlayer::Stop();
            bool isFinish = (size == -1);
            pEvent->OnStop(isFinish);
            // clear buffer circleQueue
            SLresult result = (*caller)->Clear(caller);
            ASSERT_OK_RETURN(result)

            player->NextPlay();
        }
    };
}

SLBufferPlayer::~SLBufferPlayer() {
    TRACE()
    if (source) {
        delete source;
    }
}

void SLBufferPlayer::Prepare(DecoderSource *src) {
    if (!source) {
        SLPlayer::Prepare();
    }
    this->source = src;

    if (playerObj) {
        DELETE_OBJ(playerObj)
        playerObj = nullptr;
    }


    SLDataFormat_PCM format = GetFormatFromSource(src);

    SLDataLocator_AndroidSimpleBufferQueue locator = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataSource dataSource = {&locator, &format};

    const SLInterfaceID ids[4] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME, SL_IID_MUTESOLO,
                                  SL_IID_PLAYBACKRATE};
    const SLboolean req[4] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    SLresult result = (*engineItf)->CreateAudioPlayer(engineItf, &playerObj, &dataSource, &dataSink,
                                                      sizeof(req) / sizeof(SLboolean),
                                                      ids, req);
    ASSERT_OK_RETURN(result)

    // realize the player
    INIT_OBJ_RETURN(playerObj)

    // get the buffer circleQueue interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_BUFFERQUEUE, &bufferQueueItf);
    ASSERT_OK_RETURN(result)

    // register callback on the buffer circleQueue
    result = (*bufferQueueItf)->RegisterCallback(bufferQueueItf, playCallback, this);
    ASSERT_OK_RETURN(result)

    // get the Play interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_PLAY, &playItf);
    ASSERT_OK_RETURN(result)

    // 基于SL自身API获取不到duration, 进度也不够精准
    //SetCallbackEvent();

    // Mute/Solo is not supported for sources that are known to be mono,
    // as this is get the Mute/Solo interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_MUTESOLO, &muteSoloItf);
    ASSERT_OK_RETURN(result)

    // get the volume interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_VOLUME, &volumeItf);
    ASSERT_OK_RETURN(result);

    // set the player's state to playing
    result = (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING);
    ASSERT_OK_RETURN(result);
}

bool SLBufferPlayer::Play() {
    TRACE()
    if (state == SL_PLAYSTATE_STOPPED) {
        ASSERT_COND_OR_RETURN(source->Open(), false)
        playCallback(bufferQueueItf, this);
        ASSERT_COND_OR_RETURN(SetState(SL_PLAYSTATE_PLAYING), false);
        event->OnStart();
        return true;
    }
    return false;
}

bool SLBufferPlayer::Stop() {
    TRACE()
    if (state == SL_PLAYSTATE_PAUSED) {
        SetMute(true);
        (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING);
    }
    source->Close();
    currentTime = 0;
    return true;
}

inline bool SLBufferPlayer::CanReuse(SLSource *newSource) {
    return source->GetChannels() == newSource->GetChannels() &&
           source->GetSLSampleFormat() == newSource->GetSLSampleFormat() &&
           source->GetSLChannelLayout() == newSource->GetSLChannelLayout();
}

DecoderSource *&SLBufferPlayer::GetSource() {
    return source;
}

void SLBufferPlayer::SetNextSource(DecoderSource *next) {
    if (this->nextSource) {
        FORMAT_E("previous not release")
    }
    this->nextSource = next;
}

void SLBufferPlayer::NextPlay() {
    if (nextSource) {
        if (source) {
            delete source;
            if (CanReuse(nextSource)) {
                source = nextSource;
            } else{
                Prepare(nextSource);
            }
        } else {
            Prepare(nextSource);
        }
        nextSource = nullptr;
        Play();
    }
}








