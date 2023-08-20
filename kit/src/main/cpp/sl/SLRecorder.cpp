//
// Created by loyde on 2023/2/5.
//

#include "SLRecorder.h"

void record_callback(SLAndroidSimpleBufferQueueItf caller, void *pContext) {
    auto *recorder = static_cast<SLRecorder *>(pContext);
    recorder->GetSink()->PutBuffer(recorder->GetBuffer(), recorder->GetBuffSize());
    if (recorder->IsRecording()) {
        SLAndroidSimpleBufferQueueItf &bufferQueueItf = recorder->GetBufferQueueItf();
        SLresult result = (*bufferQueueItf)->Enqueue(bufferQueueItf, recorder->GetBuffer(),
                                                     recorder->GetBuffSize());
        ASSERT_OK_RETURN(result)
    }
}

void SLRecorder::Prepare() {
    SLBase::Prepare();

    SLresult result;

    // configure audio source
    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE,
                                      SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT,
                                      nullptr};
    SLDataSource audioSrc = {&loc_dev, nullptr};

    // configure audio sink
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM,
            2,
            SL_SAMPLINGRATE_44_1,
            SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
            SL_BYTEORDER_LITTLEENDIAN};
    SLDataSink dataSink = {&loc_bq, &format_pcm};

    // create audio recorder
    // (requires the RECORD_AUDIO permission)
    const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};
    result = (*engineItf)->CreateAudioRecorder(engineItf, &recorderObj, &audioSrc, &dataSink, 1, id,
                                               req);
    ASSERT_OK_RETURN(result)

    // realize the audio recorder
    INIT_OBJ_RETURN(recorderObj)

    // get the Record interface
    result = (*recorderObj)->GetInterface(recorderObj, SL_IID_RECORD, &recordItf);
    ASSERT_OK_RETURN(result)

    // get the buffer circleQueue interface
    result = (*recorderObj)->GetInterface(recorderObj, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                          &bufferQueueItf);
    ASSERT_OK_RETURN(result)

    // register callback on the buffer circleQueue
    result = (*bufferQueueItf)->RegisterCallback(bufferQueueItf, record_callback, this);
    ASSERT_OK_RETURN(result)
}

SLRecorder::SLRecorder(SLSink *sink, int size) {
    this->sink = sink;
    buffSize = size;
    buff = new uint8_t[size];
    recordStat = SL_RECORDSTATE_STOPPED;
}

SLRecorder::~SLRecorder() {
    TRACE()
    delete[] buff;
    delete sink;
    DELETE_OBJ(recorderObj)
}

void SLRecorder::Record() {
    TRACE()
    // in case already recording
    if (recordStat == SL_RECORDSTATE_RECORDING) {
        LOCATE_W("SLRecorder is already recording !")
        return;
    }
    SLresult result;
    if (recordStat == SL_RECORDSTATE_STOPPED) {
        // clear buffer circleQueue
        result = (*bufferQueueItf)->Clear(bufferQueueItf);
        ASSERT_OK_RETURN(result)

        // enqueue an empty buffer to be filled by the recorder
        // (for streaming recording, we would enqueue at least 2 empty buffers to
        // start things off)
        result = (*bufferQueueItf)->Enqueue(bufferQueueItf, buff, buffSize);
        // the most likely other result is SL_RESULT_BUFFER_INSUFFICIENT,
        // which for this code example would indicate a programming error
        ASSERT_OK_RETURN(result)

        bool is_open = sink->Open();
        ASSERT_COND_OR_RETURN(is_open)
    }
    // start recording
    result = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_RECORDING);
    if (result == SL_RESULT_SUCCESS) {
        recordStat = SL_RECORDSTATE_RECORDING;
    } else {
        FORMAT_E("unable to Record, error code: %d", result)
    }
}

void SLRecorder::Pause() {
    TRACE()
    if (recordStat == SL_RECORDSTATE_RECORDING) {
        SLresult result = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_PAUSED);
        if (result == SL_RESULT_SUCCESS) {
            recordStat = SL_RECORDSTATE_PAUSED;
        }
    }
}

void SLRecorder::Stop() {
    TRACE()
    if (recordStat != SL_RECORDSTATE_STOPPED) {
        SLresult result = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_STOPPED);
        if (result == SL_RESULT_SUCCESS) {
            recordStat = SL_RECORDSTATE_STOPPED;
            sink->Close();
        }
    }
}

int &SLRecorder::GetBuffSize() {
    return buffSize;
}

uint8_t *&SLRecorder::GetBuffer() {
    return buff;
}

SLSink *&SLRecorder::GetSink() {
    return sink;
}

bool SLRecorder::IsRecording() const {
    return recordStat == SL_RECORDSTATE_RECORDING;
}

SLAndroidSimpleBufferQueueItf &SLRecorder::GetBufferQueueItf() {
    return bufferQueueItf;
}

SLFileSink::SLFileSink(const char *outputPath) {
    path = new char[strlen(outputPath)];
    strcpy(path, outputPath);
}

SLFileSink::~SLFileSink() {
    TRACE()
    delete[] path;
    if (file) {
        fclose(file);
    }
}

int SLFileSink::PutBuffer(uint8_t *&buffer, int &capacity) {
    size_t size = fwrite(buffer, 1, capacity, file);
    if (size != capacity) {
        FORMAT_E("PutBuffer error %d to file : %s", (int) size, path)
    }
    return (int) size;
}

bool SLFileSink::Open() {
    if (file) {
        if (fclose(file)) {
            LOCATE_E("close sink file error")
        }
    }
    file = fopen(path, "w");
    return file != nullptr;
}

bool SLFileSink::Close() {
    if (file) {
        if (fclose(file)) {
            LOCATE_E("close sink file error")
            return false;
        } else {
            file = nullptr;
        }
    }
    return true;
}


