//
// Created by loyde on 2023/2/2.
//

#ifndef WHEELKIT_UTILWRAPPER_HPP
#define WHEELKIT_UTILWRAPPER_HPP

#include <string>
#include <jni.h>

namespace util_wrapper {
    class Common {
    public:
        // 是否是小端字节序列
        static bool isLittleEndian() {
            unsigned short word = 0x0102;
            return *(char *) &word == 2;
        }

        // 获取当前时间, 自1970年至今的总计毫秒
        static long long currentMilliTime() {
            struct timeval tm;
            gettimeofday(&tm, NULL);
            long long milli_second = tm.tv_sec * 1000 + tm.tv_usec / 1000;
            return milli_second;
        }

    private:
        Common() {}
    };

    class JNI {
    public:
        const static int CURRENT_JNI_VERSION = JNI_VERSION_1_6;

        static JNI *InitVM(JavaVM *vm) { // JNI_OnLoad 调用线程为主线程
            static JNI singleton(vm);// 在 C++ 11 之后，被 static 修饰的变量可以保证是线程安全的
            return &singleton;
        }

        static int GetEnv(JNIEnv **env) {
            jint ret = GetInstance()->javaVM->GetEnv((void **) env, CURRENT_JNI_VERSION);
            if (ret == JNI_EDETACHED) {
                GetInstance()->javaVM->AttachCurrentThread(env, nullptr);
                return JNI_TRUE;
            }
            return JNI_FALSE;
        };

        static void FreeEnv(JNIEnv **env) {
            jint ret = GetInstance()->javaVM->GetEnv((void **) env, CURRENT_JNI_VERSION);
            if (ret == JNI_OK) {
                bool isMainThread = pthread_equal(GetInstance()->mainThread, pthread_self());
                if (!isMainThread) { // 主线程由系统管理, 不能detach
                    GetInstance()->javaVM->DetachCurrentThread();
                }
            }
            *env = nullptr;
        }

        // dynamic register jni method
        static int
        RegisterDynamicMethod(const char *class_name, JNINativeMethod *methods, int methodSize) {
            JNIEnv *env = nullptr;
            GetEnv(&env);

            jclass jclazz = env->FindClass(class_name);
            if (jclazz == NULL) {
                return JNI_ERR;
            }
            if (env->RegisterNatives(jclazz, methods, methodSize) < 0) {
                return JNI_ERR;
            }
            FreeEnv(&env);
            return JNI_OK;
        }


    private:
        static JNI *GetInstance() {
            return InitVM(nullptr);
        }

        JavaVM *javaVM;
        pthread_t mainThread{-1};

        JNI(JavaVM *vm) {
            javaVM = vm;
            mainThread = pthread_self();
        }
    };

    class Bit {
    public:
        Bit() = delete;

        static int GetBitValueInByte(uint8_t byte, int mask) {
            return byte & mask;
        };

        static int GetByteValueInInt(int byte, int mask) {
            return byte & mask;
        };
        const static int MASK_BYTE_BIT_1 = 0x80;
        const static int MASK_BYTE_BIT_2 = 0x40;
        const static int MASK_BYTE_BIT_3 = 0x20;
        const static int MASK_BYTE_BIT_4 = 0x10;
        const static int MASK_BYTE_BIT_5 = 0x08;
        const static int MASK_BYTE_BIT_6 = 0x04;
        const static int MASK_BYTE_BIT_7 = 0x02;
        const static int MASK_BYTE_BIT_8 = 0x01;

        const static int MASK_BYTE_PRE_2 = 0xC0;
        const static int MASK_BYTE_PRE_3 = 0xE0;
        const static int MASK_BYTE_PRE_4 = 0xF0;
        const static int MASK_BYTE_PRE_5 = 0xF8;
        const static int MASK_BYTE_PRE_6 = 0xFC;
        const static int MASK_BYTE_PRE_7 = 0xFE;

        const static int MASK_BYTE_LAST_2 = 0x03;
        const static int MASK_BYTE_LAST_3 = 0x07;
        const static int MASK_BYTE_LAST_4 = 0x0F;
        const static int MASK_BYTE_LAST_5 = 0x1F;
        const static int MASK_BYTE_LAST_6 = 0x3F;
        const static int MASK_BYTE_LAST_7 = 0x7F;

        const static int MASK_INT_BYTE_1 = 0xff000000;
        const static int MASK_INT_BYTE_2 = 0xff0000;
        const static int MASK_INT_BYTE_3 = 0xff00;
        const static int MASK_INT_BYTE_4 = 0xff;

        const static int MASK_INT_PRE_BYTE_2 = 0xffff0000;
        const static int MASK_INT_PRE_BYTE_3 = 0xffffff00;

        const static int MASK_INT_LAST_BYTE_2 = 0xffff;
        const static int MASK_INT_LAST_BYTE_3 = 0xffffff;

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

#define RGBA(r, g, b, a) (( (a) & 0xff) << 24) | (( (b) & 0xff) << 16) | (( (g) & 0xff) << 8) | ( (r) & 0xff)

#define INIT_DETACHED_THREAD_ATTR(attr) \
pthread_attr_init(&attr);   \
pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED); \

// 私有函数, 只能当前定义文件访问, 外部不可访问
#define PRIVATE_FUNC __attribute__ ((visibility ("hidden")))

// 默认函数访问权限, 等同于JNI的函数修饰宏 JNIEXPORT
#define PUBLIC_FUNC __attribute__ ((visibility ("default")))

#endif //WHEELKIT_UTILWRAPPER_HPP
