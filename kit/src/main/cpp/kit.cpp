#include "include/kit.h"

extern "C" {
#include "libavcodec/jni.h"
}

extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved){
    TRACE()

    JNIEnv* env = nullptr;
    if(vm->GetEnv((void**)&env, util_wrapper::JNI::CURRENT_JNI_VERSION) != JNI_OK){
        return JNI_EVERSION;
    }

    util_wrapper::JNI::InitVM(vm);

    av_jni_set_java_vm(vm, NULL);

    return util_wrapper::JNI::CURRENT_JNI_VERSION;
}


SLPlayer *player = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_jni_NativeLib_nativePlay(JNIEnv *env, jobject thiz) {
    if (!player) {
        SLSource *pQueue = new SLFileSource("/storage/emulated/0/Music/decode.pcm",44100,AV_SAMPLE_FMT_S16,2);
        //SLSource *pQueue = new DecoderSource("/storage/emulated/0/Music/bgm.mp3");
        player = new SLBufferPlayer(pQueue,  thiz);
        //player = new SLFilePlayer("/storage/emulated/0/Music/Record.mp3", thiz);
        player->Init();
    }
    if (player->IsPlaying()) {
        player->Pause();
    } else {
        player->Play();
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_jni_NativeLib_nativeStop(JNIEnv *env, jobject thiz) {
    if (player) {
        player->Stop();
        delete player;
        player = nullptr;
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_loy_kit_jni_NativeLib_nativeIsPlaying(JNIEnv *env, jobject thiz) {
    if (player) {
        return player->IsPlaying();
    }
    return false;
}

SLRecorder *recorder = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_jni_NativeLib_nativeRecord(JNIEnv *env, jobject thiz) {
    if (!recorder) {
        auto *sink = new SLFileSink("/storage/emulated/0/Music/Record.pcm");
        recorder = new SLRecorder(sink);
        recorder->Init();
    }
    if (recorder->IsRecording()) {
        recorder->Pause();
    } else {
        recorder->Record();
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_jni_NativeLib_nativeStopRecord(JNIEnv *env, jobject thiz) {
    if (recorder) {
        recorder->Stop();
        delete recorder;
        recorder = nullptr;
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_loy_kit_jni_NativeLib_nativeIsRecording(JNIEnv *env, jobject thiz) {
    if (recorder) {
        return recorder->IsRecording();
    }
    return false;
}