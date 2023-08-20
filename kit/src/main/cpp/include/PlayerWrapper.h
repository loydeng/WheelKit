//
// Created by loyde on 2023/2/4.
//

#ifndef WHEELKIT_PLAYERWRAPPER_H
#define WHEELKIT_PLAYERWRAPPER_H

#include "SLPlayer.h"

class PlayerWrapper : ProduceListener {
public:
    PlayerWrapper(jobject callback);

    ~PlayerWrapper();

    void Play(const char *url);

    bool Pause();

    bool Resume();

    bool Stop();

    bool IsPlaying();

    bool Seek(long long millisecond);

public:
    void OnState(bool isSlow) override;

private:
    JNIEnv *env{nullptr};
    jobject callback{nullptr};
    SLPlayer::Event *event{nullptr};
    SLBufferPlayer *player{nullptr};
    DataQueue<PCMBuffer> *audioLoadState{nullptr};
};


#endif //WHEELKIT_PLAYERWRAPPER_H
