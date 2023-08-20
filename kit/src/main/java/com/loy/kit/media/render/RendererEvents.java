package com.loy.kit.media.render;

/**
 * @author loy
 * @tiem 2023/7/29 17:37
 * @des
 */
public interface RendererEvents {
    public void onFirstFrameRendered();

    public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation);
}
