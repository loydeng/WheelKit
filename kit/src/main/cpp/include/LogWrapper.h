//
// Created by loyde on 2023/1/24.
//

#ifndef WHEELKIT_LOGWRAPPER_H
#define WHEELKIT_LOGWRAPPER_H
#include "UtilWrapper.hpp"
#include <android/log.h>
#include <string>

using namespace std;

#define TAG "loy"

#define OPEN_TRACE 1

#define LOG_D(tag, content) __android_log_write(ANDROID_LOG_DEBUG,tag,content);
#define LOG_I(tag, content) __android_log_write(ANDROID_LOG_INFO,tag,content);
#define LOG_W(tag, content) __android_log_write(ANDROID_LOG_WARN,tag,content);
#define LOG_E(tag, content) __android_log_write(ANDROID_LOG_ERROR,tag,content);

#define D(content) LOG_D(TAG, content)
#define I(content) LOG_I(TAG, content)
#define W(content) LOG_W(TAG, content)
#define E(content) LOG_E(TAG, content)

#define TAG_FORMAT_D(tag, format, ...) __android_log_print(ANDROID_LOG_DEBUG,tag, format, ##__VA_ARGS__);
#define TAG_FORMAT_I(tag, format, ...) __android_log_print(ANDROID_LOG_INFO, tag, format, ##__VA_ARGS__);
#define TAG_FORMAT_W(tag, format, ...) __android_log_print(ANDROID_LOG_WARN, tag, format, ##__VA_ARGS__);
#define TAG_FORMAT_E(tag, format, ...) __android_log_print(ANDROID_LOG_ERROR,tag, format, ##__VA_ARGS__);

#define FORMAT_D(format, ...) TAG_FORMAT_D(TAG, format, ##__VA_ARGS__)
#define FORMAT_I(format, ...) TAG_FORMAT_I(TAG, format, ##__VA_ARGS__)
#define FORMAT_W(format, ...) TAG_FORMAT_W(TAG, format, ##__VA_ARGS__)
#define FORMAT_E(format, ...) TAG_FORMAT_E(TAG, format, ##__VA_ARGS__)

#define MSG_LOCATION_TEMPLATE "%s <locate in %s::%s at line %d>"
#define TAG_LOCATE_D(tag, content) __android_log_print(ANDROID_LOG_DEBUG,tag, MSG_LOCATION_TEMPLATE, content, __FILE_NAME, __func__, __LINE__);
#define TAG_LOCATE_I(tag, content) __android_log_print(ANDROID_LOG_INFO, tag, MSG_LOCATION_TEMPLATE, content, __FILE_NAME, __func__, __LINE__);
#define TAG_LOCATE_W(tag, content) __android_log_print(ANDROID_LOG_WARN, tag, MSG_LOCATION_TEMPLATE, content, __FILE_NAME, __func__, __LINE__);
#define TAG_LOCATE_E(tag, content) __android_log_print(ANDROID_LOG_ERROR,tag, MSG_LOCATION_TEMPLATE, content, __FILE_NAME, __func__, __LINE__);

#define LOCATE_D(content) TAG_LOCATE_D(TAG, content)
#define LOCATE_I(content) TAG_LOCATE_I(TAG, content)
#define LOCATE_W(content) TAG_LOCATE_W(TAG, content)
#define LOCATE_E(content) TAG_LOCATE_E(TAG, content)

// 函数返回值检查
#define __FILE_NAME \
({                    \
 string name=__FILE__; \
 int index=name.rfind('/'); \
 if(index!=-1){       \
   name=name.substr(index+1); \
 }                    \
 name.c_str();        \
})

#if OPEN_TRACE
#define TRACE_MSG_TEMPLATE "<Trace Info> function: %s is called at (%s:%d)"
#define TRACE() __android_log_print(ANDROID_LOG_WARN, TAG, TRACE_MSG_TEMPLATE, __func__, __FILE_NAME, __LINE__);
#else
#define TRACE()
#endif

#define COND_MSG_TEMPLATE "%s condition is not %s at (%s:%d)"

#define CHECK_COND(cond) \
({                       \
bool err=!(cond);        \
if(err) {                \
    __android_log_print(ANDROID_LOG_ERROR, TAG, COND_MSG_TEMPLATE, __func__, #cond, __FILE_NAME, __LINE__); \
}                        \
err;                     \
})

#define ASSERT_COND_OR_RETURN(cond, ...) if(CHECK_COND(cond)){ \
  return __VA_ARGS__;                            \
}

#define ASSERT_ZERO_OR_RETURN(cond, ...) ASSERT_COND_OR_RETURN(cond == 0, ##__VA_ARGS__)

#define ASSERT_NONNULL_OR_RETURN(cond, ...) ASSERT_COND_OR_RETURN(cond != nullptr, ##__VA_ARGS__)

#define ASSERT_COND_OR_CONTINUE(cond, ...) if(CHECK_COND(cond)){ \
  __VA_ARGS__;                                                    \
  continue;                                               \
}

#define ASSERT_COND_OR_BREAK(cond, ...) if(CHECK_COND(cond)){ \
  __VA_ARGS__;                                                 \
  break;                                               \
}

#endif //WHEELKIT_LOGWRAPPER_H
