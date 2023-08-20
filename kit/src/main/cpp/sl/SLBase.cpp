//
// Created by loyde on 2023/2/5.
//

#include "SLBase.h"
SLBase::~SLBase() {
    TRACE()
    DELETE_OBJ(mixObj)
    DELETE_OBJ(engineObj)
}

void SLBase::Prepare() {

    slCreateEngine(&engineObj, 0, nullptr, 0, nullptr, nullptr);

    INIT_OBJ_RETURN(engineObj)

    SLresult result = (*engineObj)->GetInterface(engineObj, SL_IID_ENGINE, &engineItf);
    ASSERT_OK_RETURN(result)
    (*engineItf)->CreateOutputMix(engineItf, &mixObj, 0, nullptr, nullptr);
    INIT_OBJ_RETURN(mixObj)

    /*
    // create output mix, with environmental reverb specified as a non-required interface
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    (*engineItf)->CreateOutputMix(engineItf, &mixObj, 1, ids, req);
    INIT_OBJ_RETURN(mixObj)

    // get the environmental reverb interface, this could fail if the environmental reverb effect is not available,
    // either because the feature is not present, excessive CPU load,
    // or the required MODIFY_AUDIO_SETTINGS permission was not requested and granted
    // or we are in fast audio, reverb is not supported.
    result = (*mixObj)->GetInterface(mixObj, SL_IID_ENVIRONMENTALREVERB, &environmentalReverbItf);
    if (CHECK_FAILURE(result)) {
        E("the environmental reverb effect is not available");
    } else {
        const SLEnvironmentalReverbSettings settings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
        result = (*environmentalReverbItf)->SetEnvironmentalReverbProperties(environmentalReverbItf, &settings);
        ASSERT_OK_RETURN(result)
    }*/
}