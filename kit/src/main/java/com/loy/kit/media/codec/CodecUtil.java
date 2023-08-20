package com.loy.kit.media.codec;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

/**
 * @author loyde
 * @des
 * @time 2022/4/14 21:45
 */
public class CodecUtil {

    public static MediaCodecInfo findFromCodecList(String mime, boolean isEncoder) {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        MediaCodecInfo[] codecInfos = mediaCodecList.getCodecInfos();
        for (MediaCodecInfo codecInfo : codecInfos) {
            boolean filter = codecInfo.isEncoder();
            if (isEncoder == filter) {
                //MediaCodecInfo.CodecCapabilities capabilitiesForType = codecInfo.getCapabilitiesForType(mime);
                //capabilitiesForType.isFormatSupported()
                //codecInfo.isHardwareAccelerated();
                //codecInfo.isSoftwareOnly();
                String[] supportedTypes = codecInfo.getSupportedTypes();
                for (String supportedType : supportedTypes) {
                    if (supportedType.equals(mime)) {
                        return codecInfo;
                    }
                }
            }
        }
        return null;
    }

    public static String getCodecName(MediaFormat format, boolean isEncoder) {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        return isEncoder ? mediaCodecList.findEncoderForFormat(format) : mediaCodecList.findDecoderForFormat(format);
    }

    public static int[] showSupportColorFormat(String mime, boolean isEncoder) {
        MediaCodecInfo info = findFromCodecList(mime, isEncoder);
        if (info != null) {
            MediaCodecInfo.CodecCapabilities cap = info.getCapabilitiesForType(mime);
            int[] colorFormats = new int[cap.colorFormats.length];
            System.arraycopy(cap.colorFormats, 0, colorFormats, 0, colorFormats.length);
            return colorFormats;
        }else {
            return new int[0];
        }
    }
}
