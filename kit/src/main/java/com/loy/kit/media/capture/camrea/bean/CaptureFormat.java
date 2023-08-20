package com.loy.kit.media.capture.camrea.bean;

import android.graphics.ImageFormat;
import android.util.Size;

import com.loy.kit.media.capture.camrea.bean.FrameRateRange;

import java.util.List;

/**
 * @author loy
 * @tiem 2023/2/26 14:45
 * @des
 */
public class CaptureFormat {
    private final Profile mProfile;
    private final FrameRateRange mFrameRateRange;

    public CaptureFormat(Profile profile, FrameRateRange frameRateRange) {
        mProfile = profile;
        mFrameRateRange = frameRateRange;
    }

    public Profile getProfile() {
        return mProfile;
    }

    public FrameRateRange getFrameRateRange() {
        return mFrameRateRange;
    }

    // TODO: add YUV420_888,JPEG
    private static final int imageFormat = ImageFormat.NV21;

    public static int getImageFormat() {
        return imageFormat;
    }

    public static int frameSize(int width, int height, int imageFormat) {
        if (imageFormat != ImageFormat.NV21) {
            throw new UnsupportedOperationException("Don't know how to calculate " + "the frame size of non-NV21 image formats.");
        }
        return (width * height * ImageFormat.getBitsPerPixel(imageFormat)) / 8;
    }

}
