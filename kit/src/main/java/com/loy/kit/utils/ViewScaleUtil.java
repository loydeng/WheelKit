package com.loy.kit.utils;

import android.util.Size;

/**
 * @author loy
 * @tiem 2023/2/26 14:38
 * @des
 */
public class ViewScaleUtil {

    public enum ScaleType {
        FIT,      // 保持比例适配， 会有黑边
        FILL,     // 保持比例填充， 会有截断
        STRETCH,  // 不保持比例进行拉伸， 会有变形
        FILL_WIDTH, // 保持比例以宽度适配，较宽时则高度方向上有黑边， 较窄时则高度方向有截断. 相机预览推荐使用
        FILL_HEIGHT // 保持比例以高度适配，较高时则宽度方向上有黑边， 较窄时则宽度方向有截断， 视频播放器横屏使用。
    }

    private static class Holder{
        private static final ViewScaleUtil INSTANCE = new ViewScaleUtil();
    }

    public static ViewScaleUtil getInstance() {
        return Holder.INSTANCE;
    }

    private ViewScaleUtil() {}

    public Size getPresentSize(
            ScaleType type,                 // 缩放模式
            int viewWidth, int viewHeight,  // 视图容器大小
            int frameWidth, int frameHeight // 帧(视图内容)大小
            ) {
        int realWidth = 0;
        int realHeight = 0;
        if (viewWidth != 0 && viewHeight != 0 && frameWidth != 0 && frameHeight != 0) {
            float viewRatio = (float) viewWidth / viewHeight;
            float frameRatio = (float) frameWidth / frameHeight;
            if (type == ScaleType.STRETCH) {  // 基于视图容器比例填充
                if (frameRatio > viewRatio) { // 较胖， 宽适配， 则高度方向有拉伸
                    realWidth = viewWidth;
                    realHeight = (int) (realWidth / viewRatio);
                } else {
                    realHeight = viewHeight;
                    realWidth = (int) (realHeight * viewRatio);
                }
            } else if (type == ScaleType.FILL) { // 基于图像比例填充
                if (frameRatio > viewRatio) {    // 较胖， 宽超出边界截断
                    realHeight = viewHeight;
                    realWidth = (int) (viewHeight * frameRatio);
                } else {
                    realWidth = viewWidth;
                    realHeight = (int) (realWidth / frameRatio);
                }
            } else if (type == ScaleType.FIT) {
                if (frameRatio > viewRatio) { // 较胖， 宽适配， 则高度方向有黑边
                    realWidth = viewWidth;
                    realHeight = (int) (realWidth / frameRatio);
                } else {
                    realHeight = viewHeight;
                    realWidth = (int) (realHeight * frameRatio);
                }
            } else if (type == ScaleType.FILL_WIDTH) {
                realWidth = viewWidth;
                realHeight = (int) (realWidth / frameRatio);
            } else if (type == ScaleType.FILL_HEIGHT) {
                realHeight = viewHeight;
                realWidth = (int) (realHeight * frameRatio);
            }
        }
        return new Size(realWidth, realHeight);
    }
}
