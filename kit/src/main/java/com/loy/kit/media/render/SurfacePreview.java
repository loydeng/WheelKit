package com.loy.kit.media.render;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.loy.kit.log.SdkLog;
import com.loy.kit.utils.ThreadUtil;
import com.loy.kit.utils.ViewScaleUtil;

/**
 * @author Loy
 * @time 2022/1/11 15:45
 * @des
 */
public class SurfacePreview extends SurfaceView implements SurfaceHolder.Callback, RendererEvents {
    private static final String TAG = "SurfacePreview";

    // Cached resource name.
    private final String resourceName;

    private RendererEvents mRendererEvents;

    // surface 视图大小
    private int surfaceWidth;
    private int surfaceHeight;

    // 视频帧大小
    private int rotatedFrameWidth;
    private int rotatedFrameHeight;
    private ViewScaleUtil.ScaleType mScaleType;

    private boolean mirrorHorizontally;
    private boolean mirrorVertically;

    private EGLHelper.Context sharedContext;
    private EGLHelper mEGLHelper;

    private RectShader mShader;
    private Handler mRenderHandler;

    // `renderThreadHandler` is a handler for communicating with `renderThread`, and is synchronized
    // on `handlerLock`.
    private final Object handlerLock = new Object();

    private final android.graphics.Matrix projectMatrix = new android.graphics.Matrix();
    private final android.graphics.Matrix viewMatrix = new android.graphics.Matrix();

    public SurfacePreview(Context context) {
        this(context, null);
    }

    public SurfacePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.resourceName = getResourceName();
        this.mScaleType = ViewScaleUtil.ScaleType.FIT;
        getHolder().addCallback(this);
    }

    public void init(EGLHelper.Context sharedContext, RendererEvents rendererEvents) {
        init(sharedContext, rendererEvents, EGLHelper.CONFIG_PLAIN, new BeautyShader()); //TODO
    }

    public void init(final EGLHelper.Context sharedContext,
                     RendererEvents rendererEvents, final int[] configAttributes,
                     RectShader shader) {
        this.sharedContext = sharedContext;
        this.mRendererEvents = rendererEvents;
        rotatedFrameWidth = 0;
        rotatedFrameHeight = 0;
        this.mShader = shader;
        final HandlerThread renderThread = new HandlerThread(getResourceName() + "TexturePreview");
        renderThread.start();
        mRenderHandler = new ThreadUtil.HandlerWithExceptionCallback(renderThread.getLooper(), () -> {
            synchronized (handlerLock) {
                mRenderHandler = null;
            }
        });

        ThreadUtil.invokeAtFrontUninterruptibly(mRenderHandler, () -> {
            mEGLHelper = EGLHelper.create(sharedContext, configAttributes != null ? configAttributes : EGLHelper.CONFIG_PLAIN);
            initSurface();
        });
    }

    public void renderFrame(VideoFrame frame) {
        frame.retain();
        mRenderHandler.post(() -> {
            int viewWidth = getWidth();
            int viewHeight = getHeight();

            final float frameAspectRatio = frame.getRotatedWidth() / (float) frame.getRotatedHeight();
            final float drawnAspectRatio = viewHeight == 0 ? frameAspectRatio : (float) viewWidth / viewHeight;

            final float scaleX;
            final float scaleY;

            if (frameAspectRatio > drawnAspectRatio) {
                scaleX = drawnAspectRatio / frameAspectRatio;
                scaleY = 1f;
            } else {
                scaleX = 1f;
                scaleY = frameAspectRatio / drawnAspectRatio;
            }
            projectMatrix.reset();
            projectMatrix.preTranslate(0.5f, 0.5f);
            projectMatrix.preScale(mirrorHorizontally ? -1f : 1f, mirrorVertically ? -1f : 1f);
            projectMatrix.preScale(scaleX, scaleY);
            projectMatrix.preTranslate(-0.5f, -0.5f);

            viewMatrix.reset();
            viewMatrix.preTranslate(0.5f, 0.5f);
            //if (!isTextureFrame) {
            //    viewMatrix.preScale(1f, -1f); // I420-frames are upside down
            //}
            viewMatrix.preRotate(frame.getRotation());
            viewMatrix.preTranslate(-0.5f, -0.5f);

            viewMatrix.preConcat(projectMatrix);

            android.graphics.Matrix finalMatrix = new android.graphics.Matrix(frame.getTransformMatrix());
            finalMatrix.preConcat(viewMatrix);

            mShader.draw(0, 0, mEGLHelper.surfaceWidth(), mEGLHelper.surfaceHeight(), frame);

            mEGLHelper.swapBuffers();

            frame.release();
        });
    }

    public void setScaleType(ViewScaleUtil.ScaleType scaleType) {
        mScaleType = scaleType;
        requestLayout();
    }

    public void release() {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Point size = measure(widthMeasureSpec, heightMeasureSpec, rotatedFrameWidth, rotatedFrameHeight);
        setMeasuredDimension(size.x, size.y);
        SdkLog.color("onMeasure(). New size: " + size.x + "x" + size.y);
    }

    public Point measure(int widthSpec, int heightSpec, int frameWidth, int frameHeight) {
        final int maxWidth = View.getDefaultSize(Integer.MAX_VALUE, widthSpec);
        final int maxHeight = View.getDefaultSize(Integer.MAX_VALUE, heightSpec);
        if (frameWidth == 0 || frameHeight == 0 || maxWidth == 0 || maxHeight == 0) {
            return new Point(maxWidth, maxHeight);
        }
        Size presentSize = ViewScaleUtil.getInstance().getPresentSize(mScaleType, maxWidth, maxHeight, frameWidth, frameHeight);
        Point layoutSize = new Point(presentSize.getWidth(), presentSize.getHeight());
        if (View.MeasureSpec.getMode(widthSpec) == View.MeasureSpec.EXACTLY) {
            layoutSize.x = maxWidth;
        }
        if (View.MeasureSpec.getMode(heightSpec) == View.MeasureSpec.EXACTLY) {
            layoutSize.y = maxHeight;
        }
        return layoutSize;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        updateSurfaceSize();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        initSurface();
        surfaceWidth = surfaceHeight = 0;
        updateSurfaceSize();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        SdkLog.color("PixelFormat:" + format);
        surfaceWidth = width;
        surfaceHeight = height;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }


    @Override
    public void onFirstFrameRendered() {
        if (mRendererEvents != null) {
            mRendererEvents.onFirstFrameRendered();
        }
    }

    @Override
    public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
        if (mRendererEvents != null) {
            mRendererEvents.onFrameResolutionChanged(videoWidth, videoHeight, rotation);
        }
        int rotatedWidth = rotation == 0 || rotation == 180 ? videoWidth : videoHeight;
        int rotatedHeight = rotation == 0 || rotation == 180 ? videoHeight : videoWidth;

        ThreadUtil.runOnUIThread(() -> {
            rotatedFrameWidth = rotatedWidth;
            rotatedFrameHeight = rotatedHeight;
            updateSurfaceSize();
            requestLayout();
        });
    }

    private void initSurface() {
        if (getHolder().getSurface() != null && !mEGLHelper.hasSurface()) {
            mEGLHelper.createSurface(getHolder().getSurface());
            mEGLHelper.makeCurrent();
            // Necessary for YUV frames with odd width.
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        }
    }

    private String getResourceName() {
        try {
            return getResources().getResourceEntryName(getId());
        } catch (Resources.NotFoundException e) {
            return "";
        }
    }

    private void updateSurfaceSize() {
        surfaceWidth = surfaceHeight = 0;

        getHolder().setSizeFromLayout();
    }

    /**
     * Returns layout transformation matrix that applies an optional mirror effect and compensates
     * for video vs display aspect ratio.
     */
    public static float[] getLayoutMatrix(boolean mirror, float videoAspectRatio, float displayAspectRatio) {
        float scaleX = 1;
        float scaleY = 1;
        // Scale X or Y dimension so that video and display size have same aspect ratio.
        if (displayAspectRatio > videoAspectRatio) {
            scaleY = videoAspectRatio / displayAspectRatio;
        } else {
            scaleX = displayAspectRatio / videoAspectRatio;
        }
        // Apply optional horizontal flip.
        if (mirror) {
            scaleX *= -1;
        }
        final float matrix[] = new float[16];
        Matrix.setIdentityM(matrix, 0);
        Matrix.scaleM(matrix, 0, scaleX, scaleY, 1);
        adjustOrigin(matrix);
        return matrix;
    }

    /**
     * Move `matrix` transformation origin to (0.5, 0.5). This is the origin for texture coordinates
     * that are in the range 0 to 1.
     */
    private static void adjustOrigin(float[] matrix) {
        // Note that OpenGL is using column-major order.
        // Pre translate with -0.5 to move coordinates to range [-0.5, 0.5].
        matrix[12] -= 0.5f * (matrix[0] + matrix[4]);
        matrix[13] -= 0.5f * (matrix[1] + matrix[5]);
        // Post translate with 0.5 to move coordinates to range [0, 1].
        matrix[12] += 0.5f;
        matrix[13] += 0.5f;
    }
}
