//
// Created by loyde on 2023/2/2.
//

#ifndef WHEELKIT_UTILWRAPPER_HPP
#define WHEELKIT_UTILWRAPPER_HPP
#include <string>
#include <jni.h>
namespace util_wrapper {
    class Common{
    public:
        // 是否是小端字节序列
        static bool isLittleEndian(){
            unsigned short word = 0x0102;
            return *(char *) &word == 2;
        }
        // 获取当前时间, 自1970年至今的总计毫秒
        static long long currentMilliTime(){
            struct timeval tm;
            gettimeofday(&tm, NULL);
            long long milli_second = tm.tv_sec * 1000 + tm.tv_usec / 1000;
            return milli_second;
        }
    private:
        Common(){}
    };

    class JNI{
    public:
        const static int CURRENT_JNI_VERSION = JNI_VERSION_1_6;
        static JNI* InitVM(JavaVM* vm){
            static JNI singleton(vm);// 在 C++ 11 之后，被 static 修饰的变量可以保证是线程安全的
            return &singleton;
        }

        static void GetEnv(JNIEnv** env){
            bool isMainThread = pthread_equal(GetInstance()->mainThread, pthread_self());
            if (isMainThread) {
                GetInstance()->javaVM->GetEnv((void **)env, CURRENT_JNI_VERSION);
            } else {
                GetInstance()->javaVM->AttachCurrentThread(env, nullptr);
            }
        };

        static void FreeEnv(JNIEnv** env){
            bool isMainThread = pthread_equal(GetInstance()->mainThread, pthread_self());
            if (!isMainThread) {
                GetInstance()->javaVM->DetachCurrentThread();
            }
            *env = nullptr;
        }

        // dynamic register jni method
        static int RegisterDynamicMethod(const char*class_name, JNINativeMethod* methods, int methodSize){
            JNIEnv *env = nullptr;
            GetEnv(&env);

            jclass jclazz = env->FindClass(class_name);
            if(jclazz == NULL){
                return JNI_ERR;
            }
            if(env->RegisterNatives(jclazz, methods, methodSize) < 0){
                return JNI_ERR;
            }
            FreeEnv(&env);
            return JNI_OK;
        }


    private:
        static JNI* GetInstance(){
            return InitVM(nullptr);
        }
        JavaVM *javaVM;
        pthread_t mainThread{-1};
        JNI(JavaVM *vm){
            javaVM = vm;
            mainThread = pthread_self();
        }
    };

};

// 字符串格式化
#define STRING_FORMAT(format, ...) \
({                                 \
    size_t size = 1 + snprintf(nullptr, 0, format, ##__VA_ARGS__); \
    char bytes[size];              \
    snprintf(bytes, size, format, ##__VA_ARGS__);                  \
    bytes;  \
})

#define NAME_FROM_PATH(path) \
({                           \
    string ret;              \
    if(typeid(path) == typeid(char*) || \
    typeid(path) == typeid(const char*) || \
    typeid(path) == typeid(string)){    \
        ret = string(path);  \
    }                        \
    if(!ret.empty()){        \
        int index = ret.rfind('/');     \
        if(index != -1){     \
            ret = ret.substr(index+1);  \
        }                    \
    }                        \
    ret;                     \
})


#define INIT_DETACHED_THREAD_ATTR(attr) \
pthread_attr_init(&attr);   \
pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED); \


#endif //WHEELKIT_UTILWRAPPER_HPP
