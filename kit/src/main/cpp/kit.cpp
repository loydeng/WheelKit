#include "LogWrapper.h"
#include <jni.h>

extern "C" {
#include "ffmpeg/libavcodec/jni.h"
}

extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved){
    TRACE()

    JNIEnv* env = nullptr;
    if(vm->GetEnv((void**)&env, util_wrapper::JNI::CURRENT_JNI_VERSION) != JNI_OK){
        return JNI_EVERSION;
    }
    util_wrapper::JNI::InitVM(vm);

    av_jni_set_java_vm(vm, nullptr);

    return util_wrapper::JNI::CURRENT_JNI_VERSION;
}

extern "C" JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved){
    TRACE()
}