package com.loy.kit.media.capture.camrea;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.util.Size;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.loy.kit.log.SdkLog;
import com.loy.kit.media.capture.camrea.bean.Error;
import com.loy.kit.media.capture.camrea.bean.FrameRateRange;
import com.loy.kit.media.capture.camrea.bean.Profile;
import com.loy.kit.media.capture.camrea.bean.SensorInfo;
import com.loy.kit.media.render.EGLHelper;
import com.loy.kit.media.render.TextureObject;
import com.loy.kit.media.render.TexturePreview;
import com.loy.kit.media.render.VideoFrame;
import com.loy.kit.utils.MatrixUtil;
import com.loy.kit.utils.ThreadUtil;
import com.loy.kit.utils.ViewScaleUtil;

import java.util.Arrays;
import java.util.List;

/**
 * @author loy
 * @tiem 2023/7/24 18:49
 * @des
 */
public abstract class BaseCamera {
    public static final String TAG = "BaseCamera";

    public static final int DEFAULT_FPS = 30;
    protected CameraClient.API mAPI;

    protected List<SensorInfo> mSensorInfoList;
    protected Handler mHandler;
    protected Context mContext;
    protected SensorInfo mSensorInfo;

    protected FrameLayout mPreviewContainer;
    protected Profile mRealProfile;
    protected FrameRateRange mBestFpsRange;

    protected TexturePreview mPreviewView;
    protected Camera1MediaRecord mCamera1MediaRecord;


    protected TextureObject mTextureObject;

    protected SurfaceTexture mSurfaceTexture;

    // EGL
    protected EGLHelper mEGLHelper;

    protected boolean isQuitting;
    protected boolean hasPendingTexture;
    protected boolean isTextureInUse;

    public BaseCamera(Handler handler, Context context) {
        mHandler = handler;
        mContext = context;

        //mFrameBufferHandle = new ShaderHelper.FrameBufferHandle();

        /*
        int texture = ShaderHelper.createTexture(ShaderHelper.TextureType.OES);
        mTextureObject = new TextureObject(ShaderHelper.TextureType.OES, texture);

        mEGLHelper = EGLHelper.create(null, EGLHelper.CONFIG_PIXEL_BUFFER);

        mHandler.post(() -> {

            mEGLHelper.createDummyPbufferSurface();

            // 绑定EGL到当前线程
            mEGLHelper.makeCurrent();

            setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    if (hasPendingTexture) {
                        SdkLog.color(TAG, "A frame is already pending, dropping frame.");
                    }

                    hasPendingTexture = true;
                    tryDeliverTextureFrame();
                }
            });
        });
        */

    }

    private void setOnFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener listener) {
        mSurfaceTexture = new SurfaceTexture(mTextureObject.getTextureId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSurfaceTexture.setOnFrameAvailableListener(listener, mHandler);
        } else {
            mSurfaceTexture.setOnFrameAvailableListener(listener);
        }
    }

    private void updateTexImage() {
        // SurfaceTexture.updateTexImage apparently can compete and deadlock with eglSwapBuffers,
        // as observed on Nexus 5. Therefore, synchronize it with the EGL functions.
        // See https://bugs.chromium.org/p/webrtc/issues/detail?id=5702 for more info.
        synchronized (EGLHelper.lock) {
            mSurfaceTexture.updateTexImage();
        }
    }

    private void tryDeliverTextureFrame() {

        if (isQuitting || !hasPendingTexture || isTextureInUse || mPreviewView == null) {
            return;
        }

        isTextureInUse = true;
        hasPendingTexture = false;

        updateTexImage();

        final float[] transformMatrix = new float[16];
        mSurfaceTexture.getTransformMatrix(transformMatrix);
        Matrix surfaceMatrix = MatrixUtil.convertMatrixToAndroidGraphicsMatrix(transformMatrix);

        Matrix cameraMatrix = new Matrix();
        cameraMatrix.preTranslate(0.5f, 0.5f);
        if (mSensorInfo.isFront()) {
            cameraMatrix.preScale(-1f, 1f);
        }
        cameraMatrix.preRotate(mAPI == CameraClient.API.Camera2 ? mSensorInfo.getOrientation() : 0);
        cameraMatrix.preTranslate(-0.5f, -0.5f);
        long timestampNs = mSurfaceTexture.getTimestamp();

        surfaceMatrix.preConcat(cameraMatrix);

        // pre-processing

        final VideoFrame frame = new VideoFrame(Arrays.asList(mTextureObject), mRealProfile.getWidth(), mRealProfile.getHeight(),
                CameraHelper.getFrameRotation(mSensorInfo),
                surfaceMatrix, timestampNs,
                new VideoFrame.RefCountMonitor(() -> mHandler.post(() -> {
                    isTextureInUse = false;
                    if (isQuitting) {
                        close();
                    }
                }))
        );

        //mPreviewView.renderFrame(frame);
        frame.release();
    }

    public abstract List<SensorInfo> querySensorInfo();

    public abstract Error open(@NonNull SensorInfo sensorInfo, @NonNull FrameLayout preview);

    public abstract void takePicture(@NonNull String path);

    public abstract void startRecord(@NonNull String path);

    public abstract void stopRecord();

    public SensorInfo getOppositeSensorInfo() {
        SensorInfo ret = null;
        for (SensorInfo sensorInfo : mSensorInfoList) {
            if ((sensorInfo.isBack() && mSensorInfo.isFront()) || (sensorInfo.isFront() && mSensorInfo.isBack())) {
                sensorInfo.setExpectProfile(mSensorInfo.getExpectProfile());
                sensorInfo.setExpectFps(mSensorInfo.getExpectFps());
                ret = sensorInfo;
                break;
            }
        }
        return ret;
    }

    public void switchCamera() {
        SensorInfo next = getOppositeSensorInfo();
        if (next == null) {
            SdkLog.e(Error.SWITCH_FAILURE.toString());
        } else {
            mSensorInfo = next;
            close();
            open(next, mPreviewContainer);
        }
    }

    public void close() {
        //mSurfaceTexture.release();
        //mTextureObject.release();
        //mEGLHelper.release();
        if (mPreviewView.getParent() == mPreviewContainer) {
            ThreadUtil.runOnUIThread(() -> {
                mPreviewContainer.removeView(mPreviewView);
            });
        }
    }

    protected void showPreview() {
        int viewWidth = mPreviewContainer.getMeasuredWidth();
        if (viewWidth == 0) {
            mPreviewContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mPreviewContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    initSurfacePreview();
                }
            });
        } else {
            initSurfacePreview();
        }
    }

    private void initSurfacePreview() {
        int viewWidth = mPreviewContainer.getMeasuredWidth();
        int viewHeight = mPreviewContainer.getMeasuredHeight();

        Size size = mRealProfile.getOrientationViewSize();

        Size presentSize = ViewScaleUtil.getInstance().getPresentSize(ViewScaleUtil.ScaleType.FILL_WIDTH, viewWidth, viewHeight, size.getWidth(), size.getHeight());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(presentSize.getWidth(), presentSize.getHeight(), Gravity.START);

        ThreadUtil.runOnUIThread(() -> {
            if (mPreviewView.getParent() != null) {
                ViewGroup parent = (ViewGroup) mPreviewView.getParent();
                parent.removeView(mPreviewView);
            }
            mPreviewContainer.addView(mPreviewView, 0, params);
        });
    }
}
