package com.loy.kit.media.render;

import android.graphics.Matrix;
import android.opengl.GLES20;

import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author loy
 * @tiem 2023/8/1 20:03
 * @des
 */
public class YUVShader extends RectShader {

    public YUVShader() {
        super(FRAGMENT_SHADER_YUV);
    }

    public YUVShader(Matrix transformMatrix) {
        super(transformMatrix, FRAGMENT_SHADER_YUV);
    }

    @Override
    protected String[] getTextureVarNames() {
        return new String[]{"y_tex", "u_tex", "v_tex"};
    }
}
