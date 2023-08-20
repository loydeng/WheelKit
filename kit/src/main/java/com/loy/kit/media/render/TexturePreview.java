package com.loy.kit.media.render;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Size;
import android.view.TextureView;
import android.graphics.Matrix;

import androidx.annotation.NonNull;

import com.loy.kit.log.SdkLog;
import com.loy.kit.media.capture.camrea.bean.Profile;
import com.loy.kit.utils.ThreadUtil;

/**
 * @author loy
 * @tiem 2023/2/27 13:30
 * @des
 */
public class TexturePreview extends TextureView {
    private static final String TAG = "CameraPreview";

    public TexturePreview(@NonNull Context context, TextureView.SurfaceTextureListener listener) {
        super(context);

        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                listener.onSurfaceTextureAvailable(surface, width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                listener.onSurfaceTextureSizeChanged(surface,width,height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return listener.onSurfaceTextureDestroyed(surface);
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                listener.onSurfaceTextureUpdated(surface);
            }
        });
    }
}
