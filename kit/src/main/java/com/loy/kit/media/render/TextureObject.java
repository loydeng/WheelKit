package com.loy.kit.media.render;

import android.opengl.GLES20;

/**
 * @author loy
 * @tiem 2023/8/17 10:49
 * @des
 */
public class TextureObject {
    // 纹理Id
    private int textureId;
    private final ShaderHelper.TextureType type;
    private final ShaderHelper.TextureUnit unit;


    public TextureObject(ShaderHelper.TextureType type, int textureId) {
        this(type, textureId, ShaderHelper.TextureUnit.TEXTURE0);
    }

    public TextureObject(ShaderHelper.TextureType type, int textureId, ShaderHelper.TextureUnit unit) {
        this.textureId = textureId;
        this.type = type;
        this.unit = unit;
    }

    public void active(int textureLoc) {
        GLES20.glActiveTexture(unit.getValue());
        GLES20.glBindTexture(type.getValue(), textureId);
        GLES20.glUniform1i(textureLoc, unit.getUnitIndex());
    }

    public void inactive() {
        GLES20.glActiveTexture(unit.getValue());
        GLES20.glBindTexture(type.getValue(), 0);
    }

    public TextureObject setTextureId(int textureId) {
        this.textureId = textureId;
        return this;
    }

    public int getTextureId() {
        return textureId;
    }

    public ShaderHelper.TextureType getType() {
        return type;
    }

    public void release() {
        if (textureId != GLES20.GL_NONE) {
            ShaderHelper.destroyTexture(textureId);
            textureId = GLES20.GL_NONE;
        }
    }
}
