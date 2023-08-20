package com.loy.kit.media.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.loy.kit.media.codec.CodecFormat.FormatKey;
import com.loy.kit.media.codec.CodecFormat.BitrateMode;

import java.io.IOException;

/**
 * @author loyde
 * @tiem 2023/2/21 19:52
 * @des 视频编码器
 */
public class VideoEncoder extends VideoCodec {

    protected Surface mInputSurface;


    public static class Builder {
        private Type type;
        private int width;
        private int height;
        private BitrateMode bitrateMode = BitrateMode.VBR;
        private int bitrate;
        private int frameRate;
        private int keyFrameInternal;

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public BitrateMode getBitrateMode() {
            return bitrateMode;
        }

        public void setBitrateMode(BitrateMode bitrateMode) {
            this.bitrateMode = bitrateMode;
        }

        public Builder setBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        public Builder setFrameRate(int frameRate) {
            this.frameRate = frameRate;
            return this;
        }

        public Builder setKeyFrameInternal(int keyFrameInternal) {
            this.keyFrameInternal = keyFrameInternal;
            return this;
        }

        public VideoEncoder build() throws IOException {
            if (type == null || width <= 0 || height <= 0 || bitrate <= 0 || frameRate <= 0 || keyFrameInternal <= 0) {
                throw new IllegalArgumentException("value must nonnull or > 0 exclude input surface");
            }
            VideoEncoder videoEncoder = new VideoEncoder(this);
            return videoEncoder;
        }
    }

    private VideoEncoder(Builder builder) throws IOException {
        mFormat = MediaFormat.createVideoFormat(builder.type.getMime(), builder.width, builder.height);
        mFormat.setInteger(FormatKey.BITRATE, builder.bitrate);
        mFormat.setInteger(FormatKey.BITRATE_MODE, builder.bitrateMode.getValue());
        mFormat.setInteger(FormatKey.Video.FRAME_RATE, builder.frameRate);
        mFormat.setInteger(FormatKey.Video.I_FRAME_INTERVAL, builder.keyFrameInternal);
        mCodec = MediaCodec.createEncoderByType(builder.type.getMime());
    }

    @Override
    public void configure() {
        mCodec.configure(mFormat, null, null, FLAG_ENCODE);
        mInputSurface = mCodec.createInputSurface();
    }

    protected Surface getInputSurface() {
        return mInputSurface;
    }

}
