//
// Created by loyde on 2023/2/5.
//
#include "SLPlayer.h"
#include <sys/stat.h>

SLFilePlayer::SLFilePlayer(Event* event)  {
    this->event = event;
}

SLFilePlayer::~SLFilePlayer() {
    TRACE()
}

void SLFilePlayer::Prepare(const char *path) {
    SLPlayer::Prepare();

    SLresult result;

    // configure audio source
    // (requires the INTERNET permission depending on the uri parameter)
    SLDataLocator_URI loc_uri = {SL_DATALOCATOR_URI, (SLchar *) path};
    SLDataFormat_MIME format_mime = {SL_DATAFORMAT_MIME, nullptr, SL_CONTAINERTYPE_UNSPECIFIED};
    SLDataSource dataSource = {&loc_uri, &format_mime};

    // create audio player
    const SLInterfaceID ids[3] = {SL_IID_SEEK, SL_IID_MUTESOLO, SL_IID_VOLUME};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*engineItf)->CreateAudioPlayer(engineItf, &playerObj, &dataSource, &dataSink, 3, ids,
                                             req);
    // note that an invalid URI is not detected here, but during prepare/prefetch on Android, or possibly during Realize on other platforms
    ASSERT_OK_RETURN(result)

    // realize the player
    INIT_OBJ_RETURN(playerObj)

    // get the Play interface
    result = (*playerObj)->GetInterface(playerObj, SL_IID_PLAY, &playItf);
    ASSERT_OK_RETURN(result)

    SetCallbackEvent();

    // get the Seek interface
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
    if (state == SL_PLAYSTATE_STOPPED || state == SL_PLAYSTATE_PAUSED) {
        return SetState(SL_PLAYSTATE_PLAYING);
    }
    return false;
}

int SLFilePlayer::GetCurrentPosition() {
    TRACE()
    if (playItf) {
        SLmillisecond ms;
        SLresult result = (*playItf)->GetPosition(playItf, &ms);
        if (result == SL_RESULT_SUCCESS) {
            return (int) ms;
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


