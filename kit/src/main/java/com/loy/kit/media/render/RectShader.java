package com.loy.kit.media.render;

import android.graphics.Matrix;
import android.opengl.GLES20;

import com.loy.kit.R;

import java.util.Arrays;

/**
 * @author loy
 * @tiem 2023/8/1 16:24
 * @des
 */
public abstract class RectShader extends AbstractShader {

    // 图片2D 片元着色器代码, 可实现图片渲染
    protected static final String FRAGMENT_SHADER_2D = ShaderHelper.readShaderFromRawResource(R.raw.default_fragment_2d);

    // OES 片元着色器代码, 可实现android相机输出纹理的渲染
    protected static final String FRAGMENT_SHADER_OES = ShaderHelper.readShaderFromRawResource(R.raw.default_fragment_oes);

    protected static final String FRAGMENT_SHADER_YUV = ShaderHelper.readShaderFromRawResource(R.raw.default_fragment_yuv);

    // 对应着色器中的变量名
    // 顶点坐标
    protected static final String VERTEX_COORDINATE_KEY = "vc";

    // 纹理坐标
    protected static final String FRAGMENT_COORDINATE_KEY = "fc";

    // 纹理位置, 传输变换后纹理坐标给片元着色器, 方便进行纹理采样
    protected static final String FRAGMENT_POSITION_NAME = "fp";

    // 纹理位置变换矩阵, 默认单位矩阵
    protected static final String FRAGMENT_MATRIX_KEY = "fm";

    // 纹理采样器属性变量键值
    protected static final String DEFAULT_TEXTURE_KEY = "tx";

    /**
     * 默认顶点坐标, 全域映射, 正对 GL_TRIANGLE_STRIP 其中错序一位.
     * (-1,1,0)     /\ y     (1,1,0)
     * |
     * -----------------------> x
     * |
     * (-1,-1,0)   |         (1,-1,0)
     */
    private static final float[] DEFAULT_VERTEX_COORDINATE = {
            -1.0f, -1.0f, 0, // Bottom left.
            1.0f, -1.0f, 0, // Bottom right.
            -1.0f, 1.0f, 0, // Top left.
            1.0f, 1.0f, 0, // Top right.
    };

    public static float[] getDefaultVertexCoordinate() {
        return Arrays.copyOf(DEFAULT_VERTEX_COORDINATE, DEFAULT_VERTEX_COORDINATE.length);
    }

    /**
     * 默认片元坐标, 全域映射
     * ------------------------->
     * | (0,0)                 (1,0)
     * |
     * |
     * |
     * |
     * \|/(0,1)                 (1,1)
     */
    private static final float[] DEFAULT_FRAGMENT_COORDINATE = {
            0.0f, 0.0f, // Bottom left.
            1.0f, 0.0f, // Bottom right.
            0.0f, 1.0f, // Top left.
            1.0f, 1.0f, // Top right.
    };

    protected static float[] getDefaultFragmentCoordinate() {
        return Arrays.copyOf(DEFAULT_FRAGMENT_COORDINATE, DEFAULT_FRAGMENT_COORDINATE.length);
    }

    // 默认顶点着色器代码
    protected static final String DEFAULT_VERTEX_SHADER = ShaderHelper.readShaderFromRawResource(R.raw.default_vertex_2d);

    public RectShader(String fragmentCode) {
        this(null, fragmentCode);
    }

    public RectShader(Matrix transformMatrix, String fragmentCode) {
        super(getDefaultVertexCoordinate(), getDefaultFragmentCoordinate(), transformMatrix, DEFAULT_VERTEX_SHADER, fragmentCode);
    }

    public RectShader(float[] vertexCoordinate, Matrix transformMatrix, String fragmentCode) {
        super(vertexCoordinate, getDefaultFragmentCoordinate(), transformMatrix, DEFAULT_VERTEX_SHADER, fragmentCode);
    }

    @Override
    protected void findAndInitField() {
        // 获取显卡程序中的变量句柄
        // 变量索引在 GLSL 程序的生命周期内（链接之后和销毁之前），都是固定的，只需要获取一次
        this.vertex = getAttributeIndex(VERTEX_COORDINATE_KEY);
        this.fragment = getAttributeIndex(FRAGMENT_COORDINATE_KEY);
        this.matrix = getUniformIndex(FRAGMENT_MATRIX_KEY);

        // 基于变量句柄, 将本程序的数据绑定至显卡内存. 静态数据秩序绑定一次,不必每次绘制时都绑定
        // 注意,顶点属性（Attribute）变量,可以不用激活程序(调用 glUseProgram 之后)进行赋值。而Uniform属性变量赋值则一定要激活程序,否则无法正确传递至着色器器程序中.
        GLES20.glEnableVertexAttribArray(vertex);
        GLES20.glVertexAttribPointer(vertex, 3, GLES20.GL_FLOAT, false, 3 * 4, ShaderHelper.getCoordinateFloatBuffer(vertexCoordinate));

        GLES20.glEnableVertexAttribArray(fragment);
        GLES20.glVertexAttribPointer(fragment, 2, GLES20.GL_FLOAT, false, 2 * 4, ShaderHelper.getCoordinateFloatBuffer(fragmentCoordinate));
    }
}
