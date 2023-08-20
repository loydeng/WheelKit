package com.loy.kit.media.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.util.List;

/**
 * @author Loy
 * @time 2021/12/28 11:36
 * @des
 */
public class WatermarkShader extends RectShader {

    private TextureObject mTextureObject;

    public WatermarkShader(float[] vertexCoordinate, Bitmap bitmap) {
        super(vertexCoordinate, null, FRAGMENT_SHADER_2D);
        int texId = ShaderHelper.decodeBitmapTo2DTexture(bitmap);
        mTextureObject = new TextureObject(ShaderHelper.TextureType.Sample2D, texId, ShaderHelper.TextureUnit.TEXTURE0);
    }

    public void changeBitmap(Bitmap bitmap) {
        ShaderHelper.loadBitmapToTexture(mTextureObject.getTextureId(), bitmap);
    }

    @Override
    protected String[] getTextureVarNames() {
        return new String[]{DEFAULT_TEXTURE_KEY};
    }



    public void updatePosition(float x, float y) {
        float[] vp = this.vertexCoordinate;
        float w = Math.abs(vp[0] - vp[3]) / 2;
        float h = Math.abs(vp[4] - vp[7]) / 2;
        updatePositionAndSize(x, y, w, h);
    }

    // 以左上为缩放锚点
    public void updateSize(float w, float h) {
        float[] vp = this.vertexCoordinate;
        float x = vp[3];
        float y = vp[4];
        updatePositionAndSize(x, y, w, h);
    }

    // 更新水印的位置和形状
    // (x,y) 表示水印左上角的坐标, 取值[-1, 1]
    // (w,h) 表示水印大小占父容器的比例, 取值[0, 1]
    public void updatePositionAndSize(float x, float y, float w, float h) {
        float l = clamp(x, -1, 1);
        float t = clamp(y, -1, 1);
        float width = clamp(w, 0, 1);
        float height = clamp(h, 0, 1);
        float r = clamp(l + width * 2, -1, 1);
        float b = clamp(t - height * 2, -1, 1);
        updateCoordinate(l, t, r, b);
    }

    private float clamp(float value, float min, float max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        } else {
            return value;
        }
    }

    private void updateCoordinate(float l, float t, float r, float b) {
        float[] vp = this.vertexCoordinate;

        vp[0] = l;
        vp[1] = b;

        vp[3] = r;
        vp[4] = b;

        vp[6] = l;
        vp[7] = t;

        vp[9] = r;
        vp[10] = t;

        setAction(() -> {
            GLES20.glEnableVertexAttribArray(vertex);
            GLES20.glVertexAttribPointer(vertex, 3, GLES20.GL_FLOAT, false, 3 * 4, ShaderHelper.getCoordinateFloatBuffer(vertexCoordinate));
        });
    }
}
