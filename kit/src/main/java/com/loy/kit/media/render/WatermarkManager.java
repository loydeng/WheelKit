package com.loy.kit.media.render;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Loy
 * @time 2022/1/7 17:39
 * @des
 */
public class WatermarkManager {

    private final static LinkedList<WatermarkShader> sWatermarkShaders = new LinkedList<>();

    public static boolean hasWatermark() {
        return !sWatermarkShaders.isEmpty();
    }

    public static int size() {
        return sWatermarkShaders.size();
    }

    public static void addWatermark(WatermarkShader watermarkShader) {
        sWatermarkShaders.add(watermarkShader);
    }

    public static void removeWatermark(WatermarkShader watermarkShader) {
        sWatermarkShaders.remove(watermarkShader);
    }

    public static void clear() {
        for (WatermarkShader shader : sWatermarkShaders) {
            shader.release();
        }
        sWatermarkShaders.clear();
    }

    public static List<WatermarkShader> getWatermarkShader() {
        return sWatermarkShaders;
    }
}
