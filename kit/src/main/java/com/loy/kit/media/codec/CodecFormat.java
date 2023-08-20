package com.loy.kit.media.codec;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

/**
 * @author loyde
 * @tiem 2023/2/21 19:37
 * @des 保存编解码相关的常用常量
 */
public interface CodecFormat {
    String VIDEO_FORMAT_PREFIX = "video/";
    String AUDIO_FORMAT_PREFIX = "audio/";

    interface Mime {
        interface Video {
            String H264 = MediaFormat.MIMETYPE_VIDEO_AVC;
            String H265 = MediaFormat.MIMETYPE_VIDEO_HEVC;
            String VP8 = MediaFormat.MIMETYPE_VIDEO_VP8;
            String VP9 = MediaFormat.MIMETYPE_VIDEO_VP9;
            String RAW = MediaFormat.MIMETYPE_VIDEO_RAW;
        }

        interface Audio {
            String AAC = MediaFormat.MIMETYPE_AUDIO_AAC;
            String OPUS = MediaFormat.MIMETYPE_AUDIO_OPUS;
            String RAW = MediaFormat.MIMETYPE_AUDIO_RAW;
        }

        interface Text {
        }
    }

    interface FormatKey {
        String MIME = MediaFormat.KEY_MIME;
        String DURATION = MediaFormat.KEY_DURATION;
        String BITRATE = MediaFormat.KEY_BIT_RATE;
        String BITRATE_MODE = MediaFormat.KEY_BITRATE_MODE;
        String MAX_INPUT_SIZE = MediaFormat.KEY_MAX_INPUT_SIZE;
        String PROFILE = MediaFormat.KEY_PROFILE;
        String LEVEL = "level"; //MediaFormat.KEY_LEVEL;

        interface Video {
            String WIDTH = MediaFormat.KEY_WIDTH;
            String HEIGHT = MediaFormat.KEY_HEIGHT;
            String FRAME_RATE = MediaFormat.KEY_FRAME_RATE;
            String I_FRAME_INTERVAL = MediaFormat.KEY_I_FRAME_INTERVAL;
            String COLOR_FORMAT = MediaFormat.KEY_COLOR_FORMAT;
        }

        interface Audio {
            String SAMPLE_RATE = MediaFormat.KEY_SAMPLE_RATE;
            String CHANNELS = MediaFormat.KEY_CHANNEL_COUNT;
            //String DEPTH = MediaFormat.KEY_PCM_ENCODING;
        }
    }

    // 调整码率的控流模式,
    enum BitrateMode {
        CQ(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ),         // 恒定质量;
        VBR(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR),       // 动态码率;
        CBR(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR),       // 恒定码率;
        CBR_FD(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR_FD), // 以丢帧方式恒定码率;
        ;
        private int value;

        BitrateMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    interface ColorFormat {
        int All = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;  //api 21  compat format
        // ----------------------------------------------------------------
        // |   use showSupportColorFormat see hardware support
        // |
        //\|/
        int I420 = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
        int NV12 = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        int NV21 = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar;


        int RGB = MediaCodecInfo.CodecCapabilities.COLOR_Format24bitRGB888;
        //int RGBA = MediaCodecInfo.CodecCapabilities.COLOR_FormatRGBAFlexible;
        int ARGB = MediaCodecInfo.CodecCapabilities.COLOR_Format32bitARGB8888;
        int BGR = MediaCodecInfo.CodecCapabilities.COLOR_Format24bitBGR888;
        int BGRA = MediaCodecInfo.CodecCapabilities.COLOR_Format32bitBGRA8888;
        //int ABGR = MediaCodecInfo.CodecCapabilities.COLOR_Format32bitABGR8888;
    }

    interface H264 {
        int PROFILE_CONSTRAINED_BASELINE = MediaCodecInfo.CodecProfileLevel.AVCProfileConstrainedBaseline;
        int PROFILE_CONSTRAINED_HIGH = MediaCodecInfo.CodecProfileLevel.AVCProfileConstrainedHigh;

        int LEVEL_3_1 = MediaCodecInfo.CodecProfileLevel.AVCLevel31;
    }

}
