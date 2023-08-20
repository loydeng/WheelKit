//
// Created by loyde on 2023/2/5.
//

#ifndef WHEELKIT_SLBASE_H
#define WHEELKIT_SLBASE_H
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <jni.h>
#include "LogWrapper.h"

// 阻塞方式实现 SL Object 初始化
#define INIT_OBJ_RETURN(obj, ...) \
({                                \
SLresult ret = (*obj)->Realize(obj, SL_BOOLEAN_FALSE); \
int err=(ret != SL_RESULT_SUCCESS);                 \
if(err){                          \
    FORMAT_E(SL_MSG_TEMPLATE, __func__, ret, __FILE_NAME, __LINE__) \
    DELETE_OBJ(obj);  \
    obj = NULL;       \
    return __VA_ARGS__;           \
};                                \
});
#define DELETE_OBJ(obj) (*obj)->Destroy(obj);

#define SL_MSG_TEMPLATE "%s get error return %d at (%s:%d)"

#define CHECK_FAILURE(ret) \
({                                  \
    int err=(ret != SL_RESULT_SUCCESS); \
    if(err){                        \
        FORMAT_E(SL_MSG_TEMPLATE, __func__, ret, __FILE_NAME, __LINE__) \
    }                               \
    err;             \
})

#define ASSERT_OK_RETURN(result, ...) if(CHECK_FAILURE(result)){ \
    (void)result;                                                \
    return __VA_ARGS__;                                          \
}


class SLBase {
public:
    SLBase() = default;

    virtual ~SLBase();

    virtual void Prepare();
protected:
    SLObjectItf engineObj{nullptr};
    SLEngineItf engineItf{nullptr};
    SLObjectItf mixObj{nullptr};
    SLEnvironmentalReverbItf environmentalReverbItf{nullptr};
};


#endif //WHEELKIT_SLBASE_H
