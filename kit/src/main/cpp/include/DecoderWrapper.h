//
// Created by loyde on 2023/2/1.
//

#ifndef WHEELKIT_DECODERWRAPPER_H
#define WHEELKIT_DECODERWRAPPER_H

#include "DemuxerWrapper.h"
#include <vector>

extern "C" {
#include "libswresample/swresample.h"
#include "libswscale/swscale.h"
};

class PCMBuffer {
public:
    // 48000hz, 2 channels, 16 bit. 100 ms 音频数据
    const static int DEFAULT_CAPACITY = 4800 * 2 * 2;

    PCMBuffer(int capacity);

    ~PCMBuffer();

    uint8_t *&GetData();

    int &GetSize();

private:
    uint8_t *data{nullptr};
    int size{0};
    int capacity{0};
};
template <class T>
class DataQueue {
public:
    DataQueue(int num = 10, int capacity = PCMBuffer::DEFAULT_CAPACITY){
        size = num;
        for (int i = 0; i < num; i++) {
            T *buffer = new T(capacity);
            dataQueue.emplace_back(buffer);
        }
        int ret = -1;
        ret = pthread_mutex_init(&mutex, nullptr);
        CHECK_COND(ret == 0);
        ret = pthread_cond_init(&cond, nullptr);
        CHECK_COND(ret == 0);
    }

    ~DataQueue(){
        while (!dataQueue.empty()) {
            dataQueue.pop_back();
        }

        pthread_mutex_destroy(&mutex);
        pthread_cond_destroy(&cond);
    }

    T*& Produce(){
        pthread_mutex_lock(&mutex);
        while (Next(header) == tail) {
            pthread_cond_wait(&cond, &mutex);
        }
        pthread_mutex_unlock(&mutex);
        return dataQueue[Next(header)];
    }

    void ProduceDone(){
        header = Next(header);
        pthread_cond_signal(&cond);
    }

    T *&Consume(){
        pthread_mutex_lock(&mutex);
        while (tail == header) {
            pthread_cond_wait(&cond, &mutex);
        }
        pthread_mutex_unlock(&mutex);
        return dataQueue[Next(tail)];
    }

    void ConsumeDone(){
        tail = Next(tail);
        pthread_cond_signal(&cond);
    }
    int Next(int index){
        return (index + 1) % size;
    }
private:
    vector<T *> dataQueue;
    int size;
    int header{0};
    int tail{0};
    pthread_mutex_t mutex;
    pthread_cond_t cond;
};

class DecoderWrapper {
public:
    enum State{
        STOP,
        START,
        PAUSE,
    };
    enum ChannelLayout{
        STEREO, // 双声道
        SINGLE,  // 单声道
    };
    enum SampleFormat{
        U8,   // 交错格式
        U8P,  // 平面格式
        S16,  // 交错格式
        S16P, // 平面格式
        FLT,  // 浮点类型交错格式
        FLTP, // 浮点类型平面格式
    };
    DecoderWrapper(DemuxerWrapper *demuxer, ChannelLayout channelLayout=STEREO, SampleFormat sampleFormat=S16);

    ~DecoderWrapper();

    bool StartDecode();

    bool PauseDecode();

    bool StopDecode();

    DemuxerWrapper *&GetDemuxer();

    AVPacket *&GetPacket();

    AVFrame *&GetFrame();

    SwrContext *&GetSwrCtx();

    bool IsStart();

    bool IsPause();

    bool IsStop();

    State &GetState();

    DataQueue<PCMBuffer> *&GetPCMQueue();

    int64_t GetChannelLayout();

    AVSampleFormat GetSampleFormat();
private:
    DataQueue<PCMBuffer> *pcmQueue{nullptr};
    DemuxerWrapper *demuxer{nullptr};
    AVPacket *packet{nullptr};
    AVFrame *frame{nullptr};
    SwrContext *swrContext{nullptr};
    pthread_t thread;
    pthread_attr_t attr;
    State state{STOP};
    ChannelLayout channelLayout;
    SampleFormat sampleFormat;
};


#endif //WHEELKIT_DECODERWRAPPER_H
