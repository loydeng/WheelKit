package com.loy.kit.media.render;

import android.graphics.Bitmap;

import java.util.Arrays;

/**
 * @author Loy
 * @time 2022/1/5 10:50
 * @des
 */
public class Watermark {

    /*enum WatermarkCoordinate {

        // 水印左上位置坐标, 顺序 右上 -> 左上 -> 右下 -> 左下
        // 默认大小 0.4 * 0.2, 即父控件的 20% * 10% 的大小
        LEFT_TOP(new float[]{
                -0.5f, 0.9f, 0,
                -0.9f, 0.9f, 0,
                -0.5f, 0.7f, 0,
                -0.9f, 0.7f, 0,
        }),

        // 水印右上位置坐标
        RIGHT_TOP(new float[]{
                0.9f, 0.9f, 0,
                0.5f, 0.9f, 0,
                0.9f, 0.7f, 0,
                0.5f, 0.7f, 0,
        }),

        // 水印左下位置坐标
        LEFT_BOTTOM(new float[]{
                -0.5f, -0.7f, 0,
                -0.9f, -0.7f, 0,
                -0.5f, -0.9f, 0,
                -0.9f, -0.9f, 0,
        }),

        // 水印右下位置坐标
        RIGHT_BOTTOM(new float[]{
                0.9f, -0.7f, 0,
                0.5f, -0.7f, 0,
                0.9f, -0.9f, 0,
                0.5f, -0.9f, 0,
        }),
        ;

        private final float[] coordinate;

        private WatermarkCoordinate(float[] coordinate) {
            this.coordinate = coordinate;
        }

        public float[] getCoordinate() {
            return coordinate;
        }
    }*/

    public enum Position {
        LEFT_TOP(0.05f, 0.85f, 0.2f, 0.1f),
        RIGHT_TOP(0.75f, 0.85f, 0.2f, 0.1f),
        LEFT_BOTTOM(0.05f, 0.05f, 0.2f, 0.1f),
        RIGHT_BOTTOM(0.75f, 0.05f, 0.2f, 0.1f);

        private float x;
        private float y;
        private float w;
        private float h;

        Position(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getW() {
            return w;
        }

        public float getH() {
            return h;
        }
    }

    private final static float[] DEFAULT = new float[]{
            0, 0, 0,
            0, 0, 0,
            0, 0, 0,
            0, 0, 0,
    };

    private final WatermarkShader shader;

    public Watermark(Position position, Bitmap bitmap) {
        this(position.getX(), position.getY(), position.getW(), position.getH(), bitmap);
    }

    // 更新水印的位置和形状
    // (x,y) 表示水印左上角的坐标, 取值[-1, 1]
    // (w,h) 表示水印大小占父容器的比例, 取值[0, 1]
    public Watermark(float x, float y, float w, float h, Bitmap bitmap) {
        shader = new WatermarkShader(Arrays.copyOf(DEFAULT, DEFAULT.length), bitmap);
        float[] covertParam = covertToVertexCoordinate(x, y, w, h);
        shader.updatePositionAndSize(covertParam[0], covertParam[1], covertParam[2], covertParam[3]);
    }

    public WatermarkShader getShader() {
        return shader;
    }

    // x: [0, 1] -> [-1, 1]; y: [0,1] -> [1,-1]
    // (w, h): [0, 1] -> [0, 1]
    public float[] covertToVertexCoordinate(float x, float y, float w, float h) {
        float[] param = new float[4];
        param[0] = x * 2 - 1;
        param[1] = 1 - y * 2;
        param[2] = w;
        param[3] = h;
        return param;
    }
}