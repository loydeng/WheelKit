package com.loy.kit.media.render;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.opengl.GLES20;

import com.loy.kit.utils.MatrixUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author loy
 * @tiem 2023/8/1 12:03
 * @des
 */
public abstract class AbstractShader {
    public static final String TAG = "AbstractShader";

    protected final int NONE = GLES20.GL_NONE;

    // 顶点维度, xyz, (2d z=0)
    public static final int VERTEX_DIMENSION = 3;

    // 顶点坐标
    protected float[] vertexCoordinate;

    // 顶点个数
    protected int vertexNum;

    // 纹理坐标
    protected float[] fragmentCoordinate;

    // 顶点着色器代码
    protected String vertexCode;

    // 片元着色器代码
    protected String fragmentCode;

    // 程序索引
    protected int program;

    // 顶点坐标索引
    protected int vertex;

    // 片元坐标索引
    protected int fragment;

    // 顶点坐标矩阵索引
    protected int matrix;

    private final LinkedList<Runnable> changeActions;

    protected android.graphics.Matrix transformMatrix;

    protected boolean isInit;

    protected int[] textureLocations;

    public AbstractShader(float[] vertexCoordinate, float[] fragmentCoordinate, String vertexCode, String fragmentCode) {
        this(vertexCoordinate, fragmentCoordinate, null, vertexCode, fragmentCode);
    }

    public AbstractShader(float[] vertexCoordinate, float[] fragmentCoordinate, Matrix transformMatrix, String vertexCode, String fragmentCode) {
        this.isInit = false;
        this.vertexCoordinate = vertexCoordinate;
        this.vertexNum = vertexCoordinate.length / VERTEX_DIMENSION;
        this.fragmentCoordinate = fragmentCoordinate;
        this.transformMatrix = transformMatrix;
        this.vertexCode = vertexCode;
        this.fragmentCode = fragmentCode;
        this.changeActions = new LinkedList<>();
    }

    protected int getAttributeIndex(String name) {
        return ShaderHelper.getAttribLocation(this.program, name);
    }

    protected int getUniformIndex(String name) {
        return ShaderHelper.getUniformLocation(this.program, name);
    }

    public void init() {
        if (!isInit) {
            isInit = true;

            // 创建显卡程序
            createProgram();

            // 获取变量索引并初始化
            findAndInitField();

            String[] textureVarNames = getTextureVarNames();

            if (textureVarNames == null || textureVarNames.length == 0) {
                throw new IllegalArgumentException("AbstractShader getTextureVarNames return args is null or number is zero");
            }

            textureLocations = new int[textureVarNames.length];
            for (int i = 0; i < textureVarNames.length; i++) {
                textureLocations[i] = getUniformIndex(textureVarNames[i]);
            }
        }
    }

    private void createProgram() {
        // 加载编译着色器
        int vertexShader = ShaderHelper.loadShader(ShaderHelper.ShaderType.VERTEX, vertexCode);
        int fragmentShader = ShaderHelper.loadShader(ShaderHelper.ShaderType.FRAGMENT, fragmentCode);

        // 创建显卡程序
        this.program = ShaderHelper.createProgram(vertexShader, fragmentShader);

        // 程序链接完成,可释放着色器
        ShaderHelper.releaseShader(vertexShader);
        ShaderHelper.releaseShader(fragmentShader);
    }

    protected abstract void findAndInitField();

    protected abstract String[] getTextureVarNames();

    private void onFieldDateChange() {
        while (!changeActions.isEmpty()) {
            changeActions.removeFirst().run();
        }
    }

    protected void setAction(Runnable runnable) {
        changeActions.add(runnable);
    }

    protected void setInt(int index, int value) {
        setAction(() -> GLES20.glUniform1i(index, value));
    }

    protected void setFloat(int index, float value) {
        setAction(() -> GLES20.glUniform1f(index, value));
    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {
        setAction(() -> GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue)));
    }

    protected void setPoint(final int location, final PointF point) {
        setAction(() -> {
            float[] vec2 = new float[2];
            vec2[0] = point.x;
            vec2[1] = point.y;
            GLES20.glUniform2fv(location, 1, vec2, 0);
        });
    }

    protected void setFloatVec3(final int location, final float[] arrayValue) {
        setAction(() -> GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue)));
    }

    protected void setFloatVec4(final int location, final float[] arrayValue) {
        setAction(() -> GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue)));
    }

    protected void setFloatArray(final int location, final float[] arrayValue) {
        setAction(() -> GLES20.glUniform1fv(location, arrayValue.length, FloatBuffer.wrap(arrayValue)));
    }

    protected void setUniformMatrix3f(final int location, final float[] matrix) {
        setAction(() -> GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0));
    }

    protected void setUniformMatrix4f(final int location, final float[] matrix) {
        setAction(() -> GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0));
    }

    // 绘制纹理
    public void draw(int viewportX, int viewportY, int viewportWidth, int viewportHeight, VideoFrame frame) {

        ShaderHelper.useProgram(program);

        float[] finalGLMatrix = MatrixUtil.convertMatrixFromAndroidGraphicsMatrix(frame.getTransformMatrix());

        GLES20.glUniformMatrix4fv(this.matrix, 1, false, finalGLMatrix, 0);

        onFieldDateChange();

        List<TextureObject> textureObjects = frame.getTextureObjects();

        for (int i = 0; i < textureObjects.size(); i++) {
            textureObjects.get(i).active(textureLocations[i]);
        }

        GLES20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexNum);

        //GLES20.glDisableVertexAttribArray(vertex);
        //GLES20.glDisableVertexAttribArray(fragment);

        for (int i = 0; i < textureObjects.size(); i++) {
            textureObjects.get(i).inactive();
        }

        ShaderHelper.useProgram(0);
    }

    protected void release() {
        if (program > 0) {
            ShaderHelper.releaseProgram(program);
            program = -1;
        }
    }
}
