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
    const static int DEFAULT_NUM = 10;
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

class YUVBuffer {
public:
    // 1920 * 1080,
    const static int DEFAULT_NUM = 3;
    const static int DEFAULT_CAPACITY = 1920 * 1080 * 3 / 2; // 3M
};

class ProduceListener {
public:
    virtual void OnState(bool isSlow) = 0;
};

template<class T>
class DataQueue {
public:
    DataQueue(ProduceListener *listener = nullptr,
              int num = T::DEFAULT_NUM, int capacity = T::DEFAULT_CAPACITY) {
        this->ctx = ctx;
        this->onProduceSlow = listener;
        size = num;
        circleQueue.reserve(num);
        for (int i = 0; i < num; i++) {
            T *buffer = new T(capacity);
            circleQueue.emplace_back(buffer);
        }
        int ret = pthread_mutex_init(&mutex, nullptr);
        CHECK_COND(ret == 0);
        ret = pthread_cond_init(&cond, nullptr);
        CHECK_COND(ret == 0);
    }

    ~DataQueue() {
        TRACE()
        std::for_each(circleQueue.begin(), circleQueue.end(), [&](const auto &item) {
            delete item;
        });
        circleQueue.clear();
        pthread_mutex_destroy(&mutex);
        pthread_cond_destroy(&cond);
    }

    T *&Produce() {
        pthread_mutex_lock(&mutex);
        while (Next(header) == tail) {
            pthread_cond_wait(&cond, &mutex);
        }

        pthread_mutex_unlock(&mutex);
        return circleQueue[Next(header)];
    }

    void ProduceDone() {
        header = Next(header);
        pthread_cond_signal(&cond);
    }

    T *&Consume() {
        pthread_mutex_lock(&mutex);
        while (tail == header) {
            if (!isProduceSLow) {
                isProduceSLow = true;
            }
            pthread_cond_wait(&cond, &mutex);
        }
        if (isProduceSLow) {
            isProduceSLow = false;
        }
        pthread_mutex_unlock(&mutex);
        return circleQueue[Next(tail)];
    }

    void ConsumeDone() {
        tail = Next(tail);
        pthread_cond_signal(&cond);
    }

    int Next(int index) {
        return (index + 1) % size;
    }

    void Reset() {
        header = 0;
        tail = 0;
    };
private:
    vector<T *> circleQueue;
    int size;
    volatile int header{0};
    volatile int tail{0};
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    bool isProduceSLow{false};
    void *ctx{nullptr};
    ProduceListener *onProduceSlow{nullptr};
};

class DecoderWrapper {
public:
    enum State {
        STOP,
        START,
        PAUSE,
    };
    enum ChannelLayout {
        STEREO, // 双声道
        SINGLE,  // 单声道
    };
    enum SampleFormat {
        U8,   // 交错格式
        U8P,  // 平面格式
        S16,  // 交错格式
        S16P, // 平面格式
        FLT,  // 浮点类型交错格式
        FLTP, // 浮点类型平面格式
    };

    DecoderWrapper(DemuxerWrapper *demuxer, ProduceListener *listener = nullptr,
                   ChannelLayout channelLayout = STEREO, SampleFormat sampleFormat = S16);

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

    DataQueue<PCMBuffer> *&GetPCMQueue();

    int64_t GetChannelLayout();

    AVSampleFormat GetSampleFormat();

    int CalcDB(uint8_t const *pcm, int len);

private:
    DataQueue<PCMBuffer> *pcmQueue{nullptr};
    DemuxerWrapper *demuxer{nullptr};
    AVPacket *packet{nullptr};
    AVFrame *frame{nullptr};
    SwrContext *swrContext{nullptr};
    SwsContext *swsContext{nullptr};
    pthread_t thread{-1};
    pthread_attr_t attr{};
    State state{STOP};
    ChannelLayout channelLayout{STEREO};
    SampleFormat sampleFormat{S16};
    ProduceListener *produceListener{nullptr};
    const static auto GetCallback() {
        static auto callback = [](void *ctx, bool isSlow) {
            auto listener = reinterpret_cast<ProduceListener *>(ctx);
            listener->OnState(isSlow);
        };
        return callback;
    };
};


#endif //WHEELKIT_DECODERWRAPPER_H
