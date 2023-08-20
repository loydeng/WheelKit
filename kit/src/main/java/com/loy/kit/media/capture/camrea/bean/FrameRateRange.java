package com.loy.kit.media.capture.camrea.bean;

/**
 * @author loy
 * @tiem 2023/2/26 14:37
 * @des
 */
public class FrameRateRange {
    private final int min;
    private final int max;
    private final int factor;

    public FrameRateRange(int min, int max,int factor) {
        this.min = min;
        this.max = max;
        this.factor = factor;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getFactor() {
        return factor;
    }
}
