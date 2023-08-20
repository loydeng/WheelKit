package com.loy.kit.media.capture.camrea.bean;

import android.util.Size;

import com.loy.kit.utils.ScreenUtil;

/**
 * @author loy
 * @tiem 2023/7/24 9:33
 * @des 常用分辨率, 基于设备屏幕分辨率, 而非电影分辨率
 */
public enum Profile {
    PROFILE_480(640, 480, 1500_000),
    PROFILE_720P(1280, 720,3420_000),
    PROFILE_1080P(1920, 1080,6240_000),
    //PROFILE_2K(2560, 1440),
    //PROFILE_4K(3840, 2160),
    ;
    private final int width;
    private final int height;
    private final int bitrate; // 30fps下码率,帧率减半时,码率减少1/3.这里是直播场景推荐码率,通信场景下码率减半

    Profile(int width, int height,int bitrate) {
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Size getOrientationViewSize() {
        return ScreenUtil.isPortrait() ? new Size(height, width) : new Size(width, height);
    }

    public int getBitrate() {
        return bitrate;
    }

    public boolean isMatchResolution(Resolution resolution) {
        return resolution != null && resolution.getWidth() == width && resolution.getHeight() == height;
    }
}
