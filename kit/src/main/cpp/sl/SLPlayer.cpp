//
// Created by loyde on 2023/2/5.
//

#include "SLPlayer.h"

SLPlayer::SLPlayer() {
    state = SL_PLAYSTATE_STOPPED;
}

SLPlayer::~SLPlayer() {
    TRACE()
    DELETE_OBJ(playerObj)
    if (event) {
        delete event;
    }
}

long long cTime = 0;

void event_callback(SLPlayItf caller, void *pContext, SLuint32 event) {
    auto *player = static_cast<SLPlayer *>(pContext);
    switch (event) {
        case SL_PLAYEVENT_HEADATEND:
            if (cTime == 0) {
                FORMAT_E("start to Play...")
            } else {
                FORMAT_E("finish to Play, duration: %lld", cTime)
                cTime = 0;
            }
            break;
        case SL_PLAYEVENT_HEADATNEWPOS:
            cTime += 1000;
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
        default:
            LOCATE_E("Play status: unknown");
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
        (*playItf)->SetCallbackEventsMask(playItf,
                                          SL_PLAYEVENT_HEADATEND | SL_PLAYEVENT_HEADATMARKER |
                                          SL_PLAYEVENT_HEADATNEWPOS | SL_PLAYEVENT_HEADSTALLED |
                                          SL_PLAYEVENT_HEADMOVING);
    }
}

bool SLPlayer::SetState(SLuint32 state) {
    SLresult result = (*playItf)->SetPlayState(playItf, state);
    if (result == SL_RESULT_SUCCESS) {
        this->state = state;
    }
    return result == SL_RESULT_SUCCESS;
}

bool SLPlayer::IsPlaying()  {
    return state == SL_PLAYSTATE_PLAYING;
}

void SLPlayer::Prepare() {
    SLBase::Prepare();
    // configure audio sink
    sinkLocator = {SL_DATALOCATOR_OUTPUTMIX, mixObj};
    dataSink = {&sinkLocator, nullptr};
}

bool SLPlayer::PauseOrResume() {
    TRACE()
    bool ret = false;
    if (state == SL_PLAYSTATE_PLAYING) {
        ret = SetState(SL_PLAYSTATE_PAUSED);
        event->OnPause();
    } else if (state == SL_PLAYSTATE_PAUSED) {
        ret = SetState(SL_PLAYSTATE_PLAYING);
        event->OnStart();
    }
    return ret;
}

bool SLPlayer::Stop() {
    TRACE()
    bool ret = false;
    if (state == SL_PLAYSTATE_PAUSED || state == SL_PLAYSTATE_PLAYING) {
        auto preStat = state;
        ret = SetState(SL_PLAYSTATE_STOPPED);
        if (preStat == SL_PLAYSTATE_PAUSED) {
            SetMute(false);
        }
    }
    return ret;
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

short SLPlayer::GetMaxVolume() {
    if (volumeItf) {
        SLmillibel milli_bel;
        SLresult result = (*volumeItf)->GetMaxVolumeLevel(volumeItf, &milli_bel);
        if (result == SL_RESULT_SUCCESS) {
            return milli_bel;
        }
    }
    return 0;
}

int SLPlayer::GetVolume() {
    if (volumeItf) {
        SLmillibel milli_bel;
        SLresult result = (*volumeItf)->GetVolumeLevel(volumeItf, &milli_bel);
        if (result == SL_RESULT_SUCCESS) {
            return milli_bel;
        }
    }
    return 0;
}

bool SLPlayer::SetVolume(short milli_bel) {
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

int SLPlayer::SetStereoPosition(short per_mille) {
    if (volumeItf) {
        SLresult result = (*volumeItf)->SetStereoPosition(volumeItf, per_mille);
        return result == SL_RESULT_SUCCESS;
    }
    return 0;
}

double &SLPlayer::GetCurrentTime() {
    return currentTime;
}

SLuint32 SLPlayer::GetState() const {
    return state;
}

SLPlayer::Event *SLPlayer::GetEvent() const {
    return event;
}

