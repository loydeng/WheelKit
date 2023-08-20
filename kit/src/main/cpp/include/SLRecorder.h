//
// Created by loyde on 2023/2/5.
//

#ifndef WHEELKIT_SLRECORDER_H
#define WHEELKIT_SLRECORDER_H
#include "SLBase.h"

class SLSink{
public:
    virtual ~SLSink(){};
    virtual bool Open() = 0;
    virtual bool Close() = 0;
    virtual int PutBuffer(uint8_t*& buffer, int& capacity) = 0;
};

/**
 * pcm 输出文件
 */
class SLFileSink: public SLSink{
public:
    SLFileSink(const char * outputPath);

    ~SLFileSink();

    bool Open() override;

    int PutBuffer(uint8_t *&buffer, int &capacity) override;

    bool Close() override;

private:
    char *path = nullptr;
    FILE *file = nullptr;
};

/**
 * SL 只能录制输出音频原始数据 PCM
 */
class SLRecorder : public SLBase {
public:
    const static int DEFAULT_BUFF_SIZE = 2 * 4800 * 2;

    SLRecorder(SLSink *sink, int size = DEFAULT_BUFF_SIZE);

    ~SLRecorder();
    // create audio recorder: recorder is not in fast path
    //    like to avoid excessive re-sampling while playing back from Hello &
    //    Android clip
    void Prepare();
    // set the recording state for the audio recorder
    void Record();

    // PauseOrResume Record, can Resume
    void Pause();

    void Stop();

    bool IsRecording() const;

    int &GetBuffSize();

    uint8_t *&GetBuffer();

    SLSink*& GetSink();

    SLAndroidSimpleBufferQueueItf &GetBufferQueueItf();
private:
    SLObjectItf recorderObj{nullptr};
    SLRecordItf recordItf{nullptr};
    SLAndroidSimpleBufferQueueItf bufferQueueItf{nullptr};
    uint8_t *buff{nullptr};
    int buffSize{0};
    int recordStat{0};
    SLSink *sink{nullptr};
};


#endif //WHEELKIT_SLRECORDER_H
