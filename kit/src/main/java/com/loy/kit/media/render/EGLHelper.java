package com.loy.kit.media.render;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.loy.kit.log.SdkLog;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;

/**
 * @author loy
 * @tiem 2023/7/28 9:37
 * @des
 */
public class EGLHelper {

    private static final int EGLExt_SDK_VERSION = Build.VERSION_CODES.JELLY_BEAN_MR2;
    private static final int CURRENT_SDK_VERSION = Build.VERSION.SDK_INT;

    // According to the documentation, EGL can be used from multiple threads at the same time if each
    // thread has its own EGLContext, but in practice it deadlocks on some devices when doing this.
    // Therefore, synchronize on this global lock before calling dangerous EGL functions that might
    // deadlock. See https://bugs.chromium.org/p/webrtc/issues/detail?id=5702 for more info.
    public static final Object lock = new Object();

    // These constants are taken from EGL14.EGL_OPENGL_ES2_BIT and EGL14.EGL_CONTEXT_CLIENT_VERSION.
    // https://android.googlesource.com/platform/frameworks/base/+/master/opengl/java/android/opengl/EGL14.java
    // This is similar to how GlSurfaceView does:
    // http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.1.1_r1/android/opengl/GLSurfaceView.java#760
    public static final int EGL_OPENGL_ES2_BIT = 4;
    public static final int EGL_OPENGL_ES3_BIT = 0x40;
    // Android-specific extension.
    public static final int EGL_RECORDABLE_ANDROID = 0x3142;

    // EGL 1.4 is supported from Android 17, EGLExt 18
    // use egl 1.0 refer to javax.microedition.khronos.egl
    public static boolean isEGL14Supported() {
        return CURRENT_SDK_VERSION >= EGLExt_SDK_VERSION;
    }

    // Return an EGLDisplay, or die trying.
    private static EGLDisplay getDefaultDisplay() {
        EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (display == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("Unable to get EGL14 display: 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
            throw new RuntimeException("Unable to initialize EGL14: 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
        return display;
    }

    private static EGLConfig getDefaultConfig(EGLDisplay display, int[] configAttribute) {
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(display, configAttribute, 0, configs, 0, configs.length, numConfigs, 0)) {
            throw new RuntimeException("eglChooseConfig failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
        if (numConfigs[0] <= 0) {
            throw new RuntimeException("Unable to find any matching EGL config");
        }
        final EGLConfig eglConfig = configs[0];
        if (eglConfig == null) {
            throw new RuntimeException("eglChooseConfig returned null");
        }
        return eglConfig;
    }

    private static EGLContext createEglContext(@Nullable EGLContext sharedContext,
                                               EGLDisplay eglDisplay, EGLConfig eglConfig, int openGlesVersion) {
        if (sharedContext != null && sharedContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("Invalid sharedContext");
        }
        int[] contextAttributes = {EGL14.EGL_CONTEXT_CLIENT_VERSION, openGlesVersion, EGL14.EGL_NONE};
        EGLContext rootContext = sharedContext == null ? EGL14.EGL_NO_CONTEXT : sharedContext;
        final EGLContext eglContext;
        synchronized (lock) {
            eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, rootContext, contextAttributes, 0);
        }
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("Failed to create EGL context: 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
        return eglContext;
    }

    public static ConfigAttributeBuilder configBuilder() {
        return new ConfigAttributeBuilder();
    }

    public static class ConfigAttributeBuilder {
        private int openGlesVersion = 2;
        private boolean hasAlphaChannel;
        private boolean supportsPixelBuffer;
        private boolean isRecordable;

        public ConfigAttributeBuilder setOpenGlesVersion(int version) {
            if (version < 1 || version > 3) {
                throw new IllegalArgumentException("OpenGL ES version " + version + " not supported");
            }
            this.openGlesVersion = version;
            return this;
        }

        public ConfigAttributeBuilder setHasAlphaChannel(boolean hasAlphaChannel) {
            this.hasAlphaChannel = hasAlphaChannel;
            return this;
        }

        public ConfigAttributeBuilder setSupportsPixelBuffer(boolean supportsPixelBuffer) {
            this.supportsPixelBuffer = supportsPixelBuffer;
            return this;
        }

        public ConfigAttributeBuilder setIsRecordable(boolean isRecordable) {
            this.isRecordable = isRecordable;
            return this;
        }

        public int[] createConfigAttributes() {
            ArrayList<Integer> list = new ArrayList<>();
            list.add(EGL10.EGL_RED_SIZE);
            list.add(8);
            list.add(EGL10.EGL_GREEN_SIZE);
            list.add(8);
            list.add(EGL10.EGL_BLUE_SIZE);
            list.add(8);
            if (hasAlphaChannel) {
                list.add(EGL10.EGL_ALPHA_SIZE);
                list.add(8);
            }
            if (openGlesVersion == 2 || openGlesVersion == 3) {
                list.add(EGL10.EGL_RENDERABLE_TYPE);
                list.add(openGlesVersion == 3 ? EGL_OPENGL_ES3_BIT : EGL_OPENGL_ES2_BIT);
            }
            if (supportsPixelBuffer) {
                list.add(EGL10.EGL_SURFACE_TYPE);
                list.add(EGL10.EGL_PBUFFER_BIT);
            }
            if (isRecordable) {
                list.add(EGL_RECORDABLE_ANDROID);
                list.add(1);
            }
            list.add(EGL10.EGL_NONE);

            final int[] res = new int[list.size()];
            for (int i = 0; i < list.size(); ++i) {
                res[i] = list.get(i);
            }
            return res;
        }
    }

    public static final int[] CONFIG_PLAIN = configBuilder().createConfigAttributes();
    public static final int[] CONFIG_RGBA = configBuilder().setHasAlphaChannel(true).createConfigAttributes();
    public static final int[] CONFIG_PIXEL_BUFFER = configBuilder().setSupportsPixelBuffer(true).createConfigAttributes();
    public static final int[] CONFIG_PIXEL_RGBA_BUFFER = configBuilder()
            .setHasAlphaChannel(true)
            .setSupportsPixelBuffer(true)
            .createConfigAttributes();
    public static final int[] CONFIG_RECORDABLE = configBuilder().setIsRecordable(true).createConfigAttributes();

    static int getOpenGlesVersionFromConfig(int[] configAttributes) {
        for (int i = 0; i < configAttributes.length - 1; ++i) {
            if (configAttributes[i] == EGL10.EGL_RENDERABLE_TYPE) {
                switch (configAttributes[i + 1]) {
                    case EGL_OPENGL_ES2_BIT:
                        return 2;
                    case EGL_OPENGL_ES3_BIT:
                        return 3;
                    default:
                        return 1;
                }
            }
        }
        // Default to V1 if no renderable type is specified.
        return 1;
    }

    /**
     * Create a new context with the specified config attributes, sharing data with `sharedContext`.
     * If `sharedContext` is null, a root context is created. This function will try to create an EGL
     * 1.4 context if possible, and an EGL 1.0 context otherwise.
     */
    public static EGLHelper create(@Nullable Context sharedContext, int[] configAttributes) {
        return new EGLHelper(sharedContext, configAttributes);
    }

    private EGLContext mEGLContext;
    private EGLConfig mEGLConfig;
    private EGLDisplay mEGLDisplay;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;

    public static class Context {
        private final EGLContext mEGL14Context;

        public Context(EGLContext context) {
            mEGL14Context = context;
        }

        public EGLContext getEGL14Context() {
            return mEGL14Context;
        }

        public long getNativeEGLContext() {
            return CURRENT_SDK_VERSION >= Build.VERSION_CODES.LOLLIPOP ?
                    mEGL14Context.getNativeHandle() : mEGL14Context.getHandle();
        }
    }

    private EGLHelper(Context context, int[] configAttributes) {
        mEGLDisplay = getDefaultDisplay();
        mEGLConfig = getDefaultConfig(mEGLDisplay, configAttributes);
        final int openGlesVersion = getOpenGlesVersionFromConfig(configAttributes);
        SdkLog.color("Using OpenGL ES version " + openGlesVersion);
        mEGLContext = createEglContext(context == null ? null : context.getEGL14Context(), mEGLDisplay, mEGLConfig, openGlesVersion);
    }

    public void createSurface(Surface surface) {
        createSurfaceInternal(surface);
    }

    public void createSurface(SurfaceTexture texture) {
        createSurfaceInternal(texture);
    }

    // Create EGLSurface from either Surface or SurfaceTexture.
    private void createSurfaceInternal(Object surface) {
        if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture)) {
            throw new IllegalStateException("Input must be either a Surface or SurfaceTexture");
        }
        checkIsNotReleased();
        if (mEGLSurface != EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("Already has an EGLSurface");
        }
        int[] surfaceAttributes = {EGL14.EGL_NONE};
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttributes, 0);
        if (mEGLSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException(
                    "Failed to create window surface: 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
    }

    public void makeCurrent() {
        checkIsNotReleased();
        if (mEGLSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("No EGLSurface - can't make current");
        }
        synchronized (lock) {
            if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
                throw new RuntimeException("eglMakeCurrent failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
            }
        }
    }

    // Detach the current EGL context, so that it can be made current on another thread.
    public void detachCurrent() {
        synchronized (lock) {
            if (!EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
                throw new RuntimeException("eglDetachCurrent failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
            }
        }
    }

    public void releaseSurface() {
        if (mEGLSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
            mEGLSurface = EGL14.EGL_NO_SURFACE;
        }
    }

    public void release() {
        checkIsNotReleased();
        releaseSurface();
        detachCurrent();
        synchronized (lock) {
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
        }
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEGLDisplay);
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLConfig = null;
    }

    public Context getEglBaseContext() {
        return new Context(mEGLContext);
    }

    public void swapBuffers() {
        checkIsNotReleased();
        if (mEGLSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("No EGLSurface - can't swap buffers");
        }
        synchronized (lock) {
            EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
        }
    }

    public void swapBuffers(long timeStampNs) {
        checkIsNotReleased();
        if (mEGLSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("No EGLSurface - can't swap buffers");
        }
        synchronized (lock) {
            // See
            // https://android.googlesource.com/platform/frameworks/native/+/tools_r22.2/opengl/specs/EGL_ANDROID_presentation_time.txt
            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, timeStampNs);
            EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
        }
    }

    public void createDummyPbufferSurface() {
        createPbufferSurface(1, 1);
    }

    public void createPbufferSurface(int width, int height) {
        checkIsNotReleased();
        if (mEGLSurface != EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("Already has an EGLSurface");
        }
        int[] surfaceAttributes = {EGL14.EGL_WIDTH, width, EGL14.EGL_HEIGHT, height, EGL14.EGL_NONE};
        mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttributes, 0);
        if (mEGLSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("Failed to create pixel buffer surface with size " + width + "x"
                    + height + ": 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
    }

    public boolean hasSurface() {
        return mEGLSurface != EGL14.EGL_NO_SURFACE;
    }

    public int surfaceWidth() {
        final int widthArray[] = new int[1];
        EGL14.eglQuerySurface(mEGLDisplay, mEGLSurface, EGL14.EGL_WIDTH, widthArray, 0);
        return widthArray[0];
    }

    public int surfaceHeight() {
        final int heightArray[] = new int[1];
        EGL14.eglQuerySurface(mEGLDisplay, mEGLSurface, EGL14.EGL_HEIGHT, heightArray, 0);
        return heightArray[0];
    }

    private void checkIsNotReleased() {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY || mEGLContext == EGL14.EGL_NO_CONTEXT
                || mEGLConfig == null) {
            throw new RuntimeException("This object has been released");
        }
    }
}
