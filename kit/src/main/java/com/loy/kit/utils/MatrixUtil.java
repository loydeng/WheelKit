package com.loy.kit.utils;

import android.graphics.Matrix;
import android.graphics.Point;

/**
 * @author loy
 * @tiem 2023/7/30 15:55
 * @des
 */
public class MatrixUtil {

    // 将 3D变换矩阵 转换为 2D变换矩阵.即不需要Z轴元素,所以就没要第三行第三列元素(索引2), 详情看查看矩阵在坐标平移,缩放,旋转时的影响数值.
    // 注意变换矩阵是按列主序排列的.
    public static android.graphics.Matrix convertMatrixToAndroidGraphicsMatrix(float[] matrix4x4) {
        float[] values = {
                matrix4x4[0 * 4 + 0], matrix4x4[1 * 4 + 0], matrix4x4[3 * 4 + 0],
                matrix4x4[0 * 4 + 1], matrix4x4[1 * 4 + 1], matrix4x4[3 * 4 + 1],
                matrix4x4[0 * 4 + 3], matrix4x4[1 * 4 + 3], matrix4x4[3 * 4 + 3],
        };
        android.graphics.Matrix matrix = new android.graphics.Matrix();

        matrix.setValues(values);
        return matrix;
    }


    public static float[] convertMatrixFromAndroidGraphicsMatrix(android.graphics.Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);

        // The android.graphics.Matrix looks like this:
        // [x1 y1 w1]
        // [x2 y2 w2]
        // [x3 y3 w3]
        // We want to contruct a matrix that looks like this:
        // [x1 y1  0 w1]
        // [x2 y2  0 w2]
        // [ 0  0  1  0]
        // [x3 y3  0 w3]
        // Since it is stored in column-major order, it looks like this:
        // [x1 x2 0 x3
        //  y1 y2 0 y3
        //   0  0 1  0
        //  w1 w2 0 w3]
        float[] matrix4x4 = {
                values[0 * 3 + 0], values[1 * 3 + 0], 0, values[2 * 3 + 0],
                values[0 * 3 + 1], values[1 * 3 + 1], 0, values[2 * 3 + 1],
                0, 0, 1, 0,
                values[0 * 3 + 2], values[1 * 3 + 2], 0, values[2 * 3 + 2],
        };
        return matrix4x4;
    }

    private static int distance(float x0, float y0, float x1, float y1) {
        return (int) Math.round(Math.hypot(x1 - x0, y1 - y0));
    }

    public static Point calculateTransformedRenderSize(int frameWidth, int frameHeight, Matrix renderMatrix) {
        int renderWidth;
        int renderHeight;
        if (renderMatrix == null) {
            renderWidth = frameWidth;
            renderHeight = frameHeight;
        } else {
            // 三个纹理坐标( 左下, 右下, 左上), 可以得出纹理宽高.
            final float[] srcPoints = new float[]{0f, 0f, 1f, 0f, 0f, 1f};
            final float[] dstPoints = new float[6];

            // 将变换矩阵应用到归一化的顶点坐标中.
            renderMatrix.mapPoints(dstPoints, srcPoints);

            // 按纹理大小乘以归一化系数,得出实际坐标大小
            for (int i = 0; i < 3; ++i) {
                dstPoints[i * 2 + 0] *= frameWidth;
                dstPoints[i * 2 + 1] *= frameHeight;
            }

            // 勾股定理得出渲染宽高.
            renderWidth = distance(dstPoints[0], dstPoints[1], dstPoints[2], dstPoints[3]);
            renderHeight = distance(dstPoints[0], dstPoints[1], dstPoints[4], dstPoints[5]);
        }
        return new Point(renderWidth, renderHeight);
    }
}
