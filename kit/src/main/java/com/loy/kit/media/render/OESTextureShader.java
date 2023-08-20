package com.loy.kit.media.render;

import android.graphics.Matrix;

import java.util.List;

/**
 * @author loy
 * @tiem 2023/8/1 16:39
 * @des
 */
public class OESTextureShader extends RectShader{

    public OESTextureShader() {
        super(FRAGMENT_SHADER_OES);
    }

    public OESTextureShader(Matrix transformMatrix) {
        super(transformMatrix, FRAGMENT_SHADER_OES);
    }

    @Override
    protected String[] getTextureVarNames() {
        return new String[]{DEFAULT_TEXTURE_KEY};
    }
}
