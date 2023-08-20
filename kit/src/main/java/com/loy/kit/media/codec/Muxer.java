package com.loy.kit.media.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author loyde
 * @tiem 2023/2/21 19:55
 * @des
 */ // 封装
public class Muxer {
    enum FileFormat {// 容器内部支持的编码视频格式有限制,
        MP4(MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4), // 音频 AAC LC/HE V1/HE V2/ELD; 视频 h263/ h264 avc BP
        GPP_3(MediaMuxer.OutputFormat.MUXER_OUTPUT_3GPP),
        OGG(MediaMuxer.OutputFormat.MUXER_OUTPUT_OGG),// 音频
        ;
        private final int format;

        FileFormat(int format) {
            this.format = format;
        }

        public int getFormat() {
            return format;
        }
    }

    private final MediaMuxer mMuxer;

    public Muxer(String path, FileFormat fileFormat) throws IOException {
        mMuxer = new MediaMuxer(path, fileFormat.format);
        //mMuxer.setLocation(1,1);
        //mMuxer.setOrientationHint(0);
    }

    public int addTrack(MediaFormat format) {
        return mMuxer.addTrack(format);
    }

    public void start() {
        mMuxer.start();
    }

    //trackIndex is from addTrack, buffer and info is from either MediaCodec or MediaExtractor
    public void write(int trackIndex, ByteBuffer buffer, MediaCodec.BufferInfo info) {
        mMuxer.writeSampleData(trackIndex, buffer, info);
    }

    public void release() {
        mMuxer.stop();
        mMuxer.release();
    }
}
