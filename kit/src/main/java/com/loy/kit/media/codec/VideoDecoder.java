package com.loy.kit.media.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import com.loy.kit.media.codec.CodecFormat.FormatKey;
import com.loy.kit.media.codec.CodecFormat.ColorFormat;
import java.io.IOException;

/**
 * @author loyde
 * @tiem 2023/2/21 19:53
 * @des 视频解码器
 */
public class VideoDecoder extends VideoCodec {
    private Surface mOutputSurface;

    public static class Builder {
        private Type type;
        private int width;
        private int height;
        private int frameRate;
        private int colorFormat;
        private Surface outputSurface;

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

        public Builder setFrameRate(int frameRate) {
            this.frameRate = frameRate;
            return this;
        }

        public Builder setColorFormat(int colorFormat) {
            this.colorFormat = colorFormat;
            return this;
        }

        public Builder setOutputSurface(Surface outputSurface) {
            this.outputSurface = outputSurface;
            return this;
        }

        public VideoDecoder build() throws IOException {
            if (type == null || width <= 0 || height <= 0) {
                throw new IllegalArgumentException("value must nonnull or > 0 exclude input surface and colorFormat default");
            }
            return new VideoDecoder(this);
        }
    }

    private VideoDecoder(Builder builder) throws IOException {
        mFormat = MediaFormat.createVideoFormat(builder.type.getMime(), builder.width, builder.height);
        mFormat.setInteger(FormatKey.Video.FRAME_RATE, builder.frameRate);
        mFormat.setInteger(FormatKey.Video.COLOR_FORMAT, (builder.colorFormat == 0 ? ColorFormat.All : builder.colorFormat));
        //String name = findCodec(mFormat, false);
        //mCodec = MediaCodec.createByCodecName(name);
        mCodec = MediaCodec.createDecoderByType(builder.type.getMime());
        mOutputSurface = builder.outputSurface;
    }

    @Override
    public void configure() {
        mCodec.configure(mFormat, mOutputSurface, null, 0);
    }

}
