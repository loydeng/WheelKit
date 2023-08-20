//
// Created by loyde on 2023/2/4.
//

#include "PlayerWrapper.h"

PlayerWrapper::PlayerWrapper(jobject callback) {
    util_wrapper::JNI::GetEnv(&env);
    this->callback = env->NewGlobalRef(callback);
    this->event = new SLPlayer::Event(this->callback);
    player = new SLBufferPlayer(event);
}

PlayerWrapper::~PlayerWrapper() {
    delete player;
    env->DeleteGlobalRef(callback);
    util_wrapper::JNI::FreeEnv(&env);
    delete event;
}

void PlayerWrapper::Play(const char *url) {
    if (player) {
        auto *source = new DecoderSource(url, this);
        player->SetNextSource(source);
        if (player->IsPlaying()) {
            player->Stop();
        } else {
            player->NextPlay();
        }
    }
}

bool PlayerWrapper::IsPlaying() {
    if (player){
        return player->IsPlaying();
    }
    return false;
}

bool PlayerWrapper::Pause() {
    return player->PauseOrResume();
}

bool PlayerWrapper::Resume() {

    return player->PauseOrResume();

}

bool PlayerWrapper::Stop() {
    return player->Stop();
}

bool PlayerWrapper::Seek(long long int millisecond) {
    return false;
}

void PlayerWrapper::OnState(bool isSlow) {
    event->OnLoad(isSlow);
}
