package com.loy.kit.media.codec;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * @author loyde
 * @tiem 2023/2/21 19:50
 * @des
 */
public class CodecBuffer {
    private int index;
    private ByteBuffer byteBuffer;
    private final MediaCodec.BufferInfo bufferInfo;
    private byte[] configBytes; // SPS,PPS
    private boolean isRender; // mediacodec has output surface, this should be true;

    public CodecBuffer() {
        this.bufferInfo = new MediaCodec.BufferInfo();
    }

    public void put(int index, ByteBuffer byteBuffer) {
        this.index = index;
        this.byteBuffer = byteBuffer;
    }

    public void release() {
        this.index = -1;
        this.byteBuffer = null;
    }

    public void copyInfo(MediaCodec.BufferInfo bufferInfo) {
        this.bufferInfo.set(bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
    }

    public void clear() {
        if (byteBuffer != null){
            byteBuffer.clear();
        }
    }

    //写在关键帧前面, 即 sps pps
    public boolean isConfigFrame() {
        return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0;
    }

    public boolean isKeyFrame() {
        return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
    }

    public boolean isEOS() {
        return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[0];
        if (bufferInfo.size != 0) {
            byteBuffer.position(bufferInfo.offset);
            byteBuffer.limit(bufferInfo.offset + bufferInfo.size);
            bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
        }
        return bytes;
    }

    public CodecBuffer saveConfigBytes() {
        this.configBytes = getBytes();
        return this;
    }

    public byte[] getConfigBytes() {
        return configBytes;
    }

    public void endFlag() {
        this.bufferInfo.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
    }

    public MediaCodec.BufferInfo getBufferInfo() {
        return bufferInfo;
    }

    public int getIndex() {
        return index;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public int getOffset() {
        return bufferInfo.offset;
    }

    public CodecBuffer setOffset(int offset) {
        bufferInfo.offset = offset;
        return this;
    }

    public int getSize() {
        return bufferInfo.size;
    }

    public CodecBuffer setSize(int size) {
        this.bufferInfo.size = size;
        return this;
    }

    public int getFlags() {
        return bufferInfo.flags;
    }

    public CodecBuffer setFlags(int flags) {
        this.bufferInfo.flags = flags;
        return this;
    }

    public long getPresentationTimeUs() {
        return bufferInfo.presentationTimeUs;
    }

    public CodecBuffer setPresentationTimeUs(long presentationTimeUs) {
        this.bufferInfo.presentationTimeUs = presentationTimeUs;
        return this;
    }

    public boolean isRender() {
        return isRender;
    }

    public CodecBuffer setRender(boolean render) {
        isRender = render;
        return this;
    }
}
