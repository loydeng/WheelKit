package com.loy.kit.media.render;

import android.graphics.Matrix;

/**
 * @author Loy
 * @time 2021/12/28 10:28
 * @des
 */
public class Sample2DTextureShader extends RectShader {

    public Sample2DTextureShader() {
        super(FRAGMENT_SHADER_2D);
    }

    public Sample2DTextureShader(Matrix transformMatrix) {
        super(transformMatrix, FRAGMENT_SHADER_2D);
    }

    @Override
    protected String[] getTextureVarNames() {
        return new String[]{DEFAULT_TEXTURE_KEY};
    }
}
