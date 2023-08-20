package com.loy.kit.media.capture.camrea.bean;

import android.util.Size;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author loy
 * @tiem 2023/7/24 18:56
 * @des
 */
public class Resolution {

    static final ArrayList<Size> COMMON_RESOLUTIONS = new ArrayList<>(Arrays.asList(
            // 0, Unknown resolution
            new Size(160, 120), // 1, QQVGA
            new Size(240, 160), // 2, HQVGA
            new Size(320, 240), // 3, QVGA
            new Size(400, 240), // 4, WQVGA
            new Size(480, 320), // 5, HVGA
            new Size(640, 360), // 6, nHD
            new Size(640, 480), // 7, VGA
            new Size(768, 480), // 8, WVGA
            new Size(854, 480), // 9, FWVGA
            new Size(800, 600), // 10, SVGA
            new Size(960, 540), // 11, qHD
            new Size(960, 640), // 12, DVGA
            new Size(1024, 576), // 13, WSVGA
            new Size(1024, 600), // 14, WVSGA
            new Size(1280, 720), // 15, HD
            new Size(1280, 1024), // 16, SXGA
            new Size(1920, 1080), // 17, Full HD
            new Size(1920, 1440), // 18, Full HD 4:3
            new Size(2560, 1440), // 19, QHD
            new Size(3840, 2160) // 20, UHD
    ));

    private int width;
    private int height;

    public Resolution(Profile profile) {
        this.width = profile.getWidth();
        this.height = profile.getHeight();
    }

    public Resolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public Resolution setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public Resolution setHeight(int height) {
        this.height = height;
        return this;
    }
}
