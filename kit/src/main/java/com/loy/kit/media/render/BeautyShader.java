package com.loy.kit.media.render;

import com.loy.kit.R;

import java.util.List;

/**
 * @author Loy
 * @time 2021/12/31 16:11
 * @des
 */
public class BeautyShader extends RectShader {
    public static final String BEAUTY_FRAGMENT = ShaderHelper.readShaderFromRawResource(R.raw.beauty);

    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String LEVEL_NAME = "opacity";

    private int beautyLevelIndex;
    private int widthIndex;
    private int heightIndex;

    private static final float sBlurScale = 0.5f;

    private static float sBeautyLevelValue = 0.0f;

    private float beautyLevel = 0.0f;

    private int frameWidth;

    private int frameHeight;

    // [0 , 1]
    public static void setBeautyLevelValue(float value) {
        float v = 0.0f;
        if (value < 0.0f) {
            v = 0.0f;
        } else if (value > 1.0f) {
            v = 1.0f;
        } else {
            v = value;
        }
        sBeautyLevelValue = v;
    }

    public static boolean isBeautifyOpen() {
        return sBeautyLevelValue > 0.01f;
    }

    public BeautyShader() {
        super(BEAUTY_FRAGMENT);
    }

    @Override
    protected void findAndInitField() {
        super.findAndInitField();

        this.beautyLevelIndex = getUniformIndex(LEVEL_NAME);
        this.widthIndex = getUniformIndex(WIDTH);
        this.heightIndex = getUniformIndex(HEIGHT);


        setFloat(beautyLevelIndex, beautyLevel);
        setInt(widthIndex, (int) (frameWidth * sBlurScale));
        setInt(heightIndex, (int) (frameHeight * sBlurScale));
    }

    @Override
    protected String[] getTextureVarNames() {
        return new String[]{DEFAULT_TEXTURE_KEY};
    }

    public void updateData(final int width, final int height) {
        if (frameWidth != width) {
            frameWidth = width;
            setInt(widthIndex, (int) (frameWidth * sBlurScale));
        }
        if (frameHeight != height) {
            frameHeight = height;
            setInt(heightIndex, (int) (frameHeight * sBlurScale));
        }
        if (beautyLevel != sBeautyLevelValue) {
            beautyLevel = sBeautyLevelValue;
            setFloat(beautyLevelIndex, beautyLevel);
        }
    }
}

