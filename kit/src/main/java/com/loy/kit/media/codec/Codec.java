package com.loy.kit.media.codec;

import android.media.MediaCodec;

import com.loy.kit.media.codec.CodecFormat.Mime;

import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * @author loyde
 * @tiem 2023/2/21 19:44
 * @des
 */ // 编解码器
public abstract class Codec {
    protected static final int FLAG_ENCODE = MediaCodec.CONFIGURE_FLAG_ENCODE;

    public enum Type {
        H264(Mime.Video.H264),
        H265(Mime.Video.H265),
        VP8(Mime.Video.VP8),
        VP9(Mime.Video.VP9),
        AAC(Mime.Audio.AAC),
        OPUS(Mime.Audio.OPUS),
        ;

        private final String mime;

        Type(String mime) {
            this.mime = mime;
        }

        public String getMime() {
            return mime;
        }
    }

    protected Type type;
    protected MediaCodec mCodec;
    protected MediaFormat mFormat;
    protected CodecBuffer mInputBuffer;
    protected CodecBuffer mOutputBuffer;

    public Codec() {
        mInputBuffer = new CodecBuffer();
        mOutputBuffer = new CodecBuffer();
    }

    public abstract void configure();

    public void start() {
        mCodec.start();
    }

    public void stop() {
        mCodec.stop();
    }

    public void reset() {
        mCodec.reset();
    }

    public void release() {
        mCodec.release();
    }

    // 获取有效输入缓冲区
    public int hasFillDataSize() {
        // 获取有效输入缓冲区的索引
        int index = mCodec.dequeueInputBuffer(0);
        if (index >= 0) {
            ByteBuffer inputBuffer = mCodec.getInputBuffer(index);
            inputBuffer.clear();
            mInputBuffer.put(index, inputBuffer);
            return inputBuffer.limit();
        }
        return 0;
    }

    public CodecBuffer getInputBuffer() {
        return mInputBuffer;
    }

    // 同步方式向编码器发数据
    public void fillData() {
        // pts
        //long microSecond = System.nanoTime() / 1000;
        // 将数据发回给编码器
        mCodec.queueInputBuffer(mInputBuffer.getIndex(), mInputBuffer.getOffset(), mInputBuffer.getSize(),
                mInputBuffer.getPresentationTimeUs(), mInputBuffer.getFlags());
        mInputBuffer.release();
    }

    public byte[] getOutputData() {
        // 获取输出缓冲区状态
        int index = mCodec.dequeueOutputBuffer(mOutputBuffer.getBufferInfo(), 10_1000);
        if (index >= 0) {
            ByteBuffer outputBuffer = mCodec.getOutputBuffer(index);
            mOutputBuffer.put(index, outputBuffer);
            if (mOutputBuffer.isConfigFrame()) {
                mOutputBuffer.saveConfigBytes();
            } else if (mOutputBuffer.isKeyFrame()) {

            } else if (mOutputBuffer.isEOS()) {
                return null;
            }else {
                return mOutputBuffer.getBytes();
            }
        } else {
            // index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED       // 编码器输出格式改变, 首次设置就会回调一次, 中间不会再修改.
            // 这里可以获取编码器格式
            // MediaFormat outputFormat = mCodec.getOutputFormat();
            // 然后可以开启复用器, 将数据写入文件.
            // int trackIndex = mediaMuxer.addTrack(outputFormat);
            // mediaMuxer.start();

            // index == MediaCodec.INFO_TRY_AGAIN_LATER             // 暂无数据, 一会再读
            // index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED      // 表示输出缓冲已变化, 有新数据可取.
        }
        return new byte[0];
    }

    public void releaseData() {
        mCodec.releaseOutputBuffer(mOutputBuffer.getIndex(), mOutputBuffer.isRender());
        mOutputBuffer.release();
    }

    // 异步方式
    // onInputBufferAvailable  回调时可喂数据,
    // onOutputBufferAvailable 回调时可拿数据
    public void onBufferAvailable(MediaCodec.Callback callback) {
        mCodec.setCallback(callback);
    }
}
