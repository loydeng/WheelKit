//
// Created by loyde on 2023/2/5.
//
#include "PlayerJNI.h"

extern "C"
JNIEXPORT jlong JNICALL
Java_com_loy_kit_jni_RxPlayer_nativeNewPlayer(JNIEnv *env, jclass clazz, jobject callback) {
    auto player = new PlayerWrapper(callback);
    return reinterpret_cast<jlong>(player);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_jni_RxPlayer_nativeReleasePlayer(JNIEnv *env, jclass clazz, jlong ptr) {
    auto  player = reinterpret_cast<PlayerWrapper *>(ptr);
    delete player;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_jni_RxPlayer_nativePlay(JNIEnv *env, jclass clazz, jlong ptr, jstring url) {
    auto  player = reinterpret_cast<PlayerWrapper *>(ptr);
    const char *path = env->GetStringUTFChars(url, nullptr);
    player->Play(path);
    env->ReleaseStringUTFChars(url, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_jni_RxPlayer_nativeStop(JNIEnv *env, jclass clazz, jlong ptr) {
    auto  player = reinterpret_cast<PlayerWrapper *>(ptr);
    player->Stop();
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_loy_kit_jni_RxPlayer_nativeIsPlaying(JNIEnv *env, jclass clazz, jlong ptr) {
    auto player = reinterpret_cast<PlayerWrapper *>(ptr);
    return player->IsPlaying();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_jni_RxPlayer_nativeRecord(JNIEnv *env, jobject thiz) {
    /*if (!recorder) {
        auto *sink = new SLFileSink("/storage/emulated/0/Music/Record.pcm");
        recorder = new SLRecorder(sink);
        recorder->Prepare();
    }
    if (recorder->IsRecording()) {
        recorder->Pause();
    } else {
        recorder->Record();
    }*/
}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_jni_RxPlayer_nativeStopRecord(JNIEnv *env, jobject thiz) {
    /*if (recorder) {
        recorder->Stop();
        delete recorder;
        recorder = nullptr;
    }*/
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_loy_kit_jni_RxPlayer_nativeIsRecording(JNIEnv *env, jobject thiz) {
    /*if (recorder) {
        return recorder->IsRecording();
    }*/
    return false;
}