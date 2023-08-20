package com.loy.kit.media.capture.camrea;

import android.Manifest;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.AndroidException;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.loy.kit.log.SdkLog;
import com.loy.kit.media.capture.camrea.bean.Error;
import com.loy.kit.media.capture.camrea.bean.Profile;
import com.loy.kit.media.capture.camrea.bean.SensorInfo;
import com.loy.kit.media.render.TexturePreview;
import com.loy.kit.utils.FileIOUtil;
import com.loy.kit.utils.ServiceManagerUtil;
import com.loy.kit.utils.ThreadUtil;
import com.loy.kit.utils.ToastUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author loy
 * @tiem 2023/7/25 12:31
 * @des
 */
public class Camera2Impl extends BaseCamera {

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private SessionState mSessionState = SessionState.RUNNING;

    private Surface mCameraPreviewSurface;

    //private Surface mPreviewSurface;

    private ImageReader mImageReader;

    private Camera2MediaRecord mCamera2MediaRecord;

    private boolean isRecording = false;

    public Camera2Impl(Handler handler, Context context) {
        super(handler, context);
        mAPI = CameraClient.API.Camera2;
    }

    private enum SessionState {RUNNING, STOPPED}

    // 该回调接口用于监听相机设备的状态变化。它包括以下几个方法：
    private class CameraStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // 当相机设备打开时调用，你可以在此方法中执行相机设置和创建相机会话等操作。
            SdkLog.color("Camera opened.");
            mCameraDevice = cameraDevice;


            mPreviewView = new TexturePreview(mContext, new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                    mSurfaceTexture = surface;

                    Size size = mRealProfile.getOrientationViewSize();
                    mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                    mCameraPreviewSurface = new Surface(mSurfaceTexture);

                    startPreview();
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

                }
            });

            showPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            // 当相机设备断开连接时调用，表示相机无法继续使用。在此方法中应释放相机资源。
            final boolean openFailure = (mCaptureSession == null) && (mSessionState != SessionState.STOPPED);
            mSessionState = SessionState.STOPPED;
            if (openFailure) { //
                SdkLog.e(Error.OPEN_FAILURE.getMessage());
            } else {
                SdkLog.color("Camera disconnected");
            }
            stopInternal();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            // 当相机设备遇到错误时调用，例如相机设备打开失败。在此方法中应处理错误情况并释放相机资源。
            resolveError(Error.getError(error));
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            // 当相机设备已关闭时调用，表示相机不再可用。在此方法中应释放相机资源。
            SdkLog.color("Camera device closed.");
        }
    }

    // 该回调接口用于监听相机会话的状态变化
    private class CaptureSessionCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            // 当相机会话配置完成时调用，表示可以开始进行捕捉操作。在此方法中可以创建相机预览请求或其他捕捉请求。
            SdkLog.color("Camera capture session configured.");
            mCaptureSession = session;

            try {
                CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                use3AIfSupported(builder);
                useVideoStableIfSupported(builder);
                builder.addTarget(mCameraPreviewSurface);

                mCaptureSession.setRepeatingRequest(builder.build(), new CameraCaptureCallback(), mHandler);
            } catch (CameraAccessException e) {
                resolveError(Error.STRATEGY_REFUSE);
                return;
            }

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            // 当相机会话配置失败时调用。在此方法中应处理失败情况并释放相机会话资源。
            session.close();
            resolveError(Error.SESSION_CONFIG_FAILURE);
        }
    }

    private class CameraCaptureCallback extends CameraCaptureSession.CaptureCallback {

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            SdkLog.e("Capture failed:" + failure);
        }
    }

    @Override
    public List<SensorInfo> querySensorInfo() {
        mSensorInfoList = new ArrayList<>();
        CameraManager cameraManager = ServiceManagerUtil.getCameraManager();
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String s : cameraIdList) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(s);
                int cameraId = Integer.parseInt(s);
                SensorInfo sensorInfo = new SensorInfo(cameraId, cameraCharacteristics);
                mSensorInfoList.add(sensorInfo);
            }
        } catch (AndroidException | RuntimeException e) {
            SdkLog.e(e.getMessage());
        }
        return mSensorInfoList;
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @Override
    public Error open(@NonNull SensorInfo sensorInfo, @NonNull FrameLayout preview) {
        mSensorInfo = sensorInfo;
        mPreviewContainer = preview;
        Profile expectProfile = mSensorInfo.getExpectProfile();
        mRealProfile = CameraHelper.getClosestProfile(sensorInfo.getProfiles(), expectProfile);

        int fps = sensorInfo.getExpectFps();
        mBestFpsRange = CameraHelper.getClosestFPSRange(sensorInfo.getFpsRanges(), fps);

        CameraManager cameraManager = ServiceManagerUtil.getCameraManager();
        try {
            SdkLog.color("Opening camera.");
            cameraManager.openCamera(sensorInfo.getCameraIdString(), new CameraStateCallback(), mHandler);
        } catch (CameraAccessException e) {
            resolveError(Error.OPEN_FAILURE);
            return Error.OPEN_FAILURE;
        }
        return Error.OK;
    }

    private void startPreview() {
        if (mCameraDevice != null) {

            if (mImageReader == null) {
                // ImageFormat.YUV_420_888, ImageFormat.NV21
                mImageReader = ImageReader.newInstance(mRealProfile.getWidth(), mRealProfile.getHeight(), ImageFormat.JPEG, 2);
            }

            try {
                mCameraDevice.createCaptureSession(Arrays.asList(mCameraPreviewSurface, mImageReader.getSurface()),
                        new CaptureSessionCallback(), mHandler);
            } catch (CameraAccessException e) {
                resolveError(Error.STRATEGY_REFUSE);
                return;
            }
        }
    }

    private void closePreviewSession() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
    }

    private void use3AIfSupported(CaptureRequest.Builder builder) {
        // 查询3A支持模式,并开启自动模式
        int[] afModes = mSensorInfo.getCharacteristics().get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        if (afModes != null && Arrays.asList(afModes).contains(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)) {
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
        }

        //int[] aeModes = mSensorInfo.getCharacteristics().get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
        // 自动曝光都支持,不用查询,注意关闭曝光锁定
        builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                new Range<Integer>(mBestFpsRange.getMin() / mBestFpsRange.getFactor(),
                        mBestFpsRange.getMax() / mBestFpsRange.getFactor()));
        builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        builder.set(CaptureRequest.CONTROL_AE_LOCK, false);

        int[] awbModes = mSensorInfo.getCharacteristics().get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
        if (awbModes != null && Arrays.asList(awbModes).contains(CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
            builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
        }
    }

    private void useVideoStableIfSupported(CaptureRequest.Builder builder) {
        // 开启视频防抖,优先使用硬件视频防抖,注意不要同时开启,否则会有奇怪效果
        int[] stableModes = mSensorInfo.getCharacteristics().get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
        if (stableModes != null && Arrays.asList(stableModes).contains(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON)) {
            builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
            builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
        } else {
            stableModes = mSensorInfo.getCharacteristics().get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
            if (stableModes != null && Arrays.asList(stableModes).contains(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON)) {
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);
                builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);
            }
        }
    }

    @Override
    public void close() {
        if (mSessionState != SessionState.STOPPED) {
            mSessionState = SessionState.STOPPED;
        }
        stopInternal();
        super.close();
    }

    private void stopInternal() {
        SdkLog.color("stop Internal ");

        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }

        if (mCamera2MediaRecord != null) {
            mCamera2MediaRecord.release();
            mCamera2MediaRecord = null;
        }

        SdkLog.color("stop done");
    }

    private void resolveError(Error error) {
        SdkLog.e(error.getMessage());
        final boolean startFailure = (mCaptureSession == null) && (mSessionState != SessionState.STOPPED);
        mSessionState = SessionState.STOPPED;
        stopInternal();
    }

    @Override
    public void takePicture(@NonNull String path) {
        if (mCameraDevice != null && mCaptureSession != null) {

            try {
                ThreadUtil.runOnUIThread(() -> {
                    ToastUtil.show("拍照中...");
                });

                mImageReader.setOnImageAvailableListener(reader -> {

                    File pictureFile = new File(path);
                    if (!Objects.requireNonNull(pictureFile.getParentFile()).exists()) {
                        pictureFile.getParentFile().mkdirs();
                    }

                    Image image = reader.acquireLatestImage();

                    SdkLog.color("image format:" + image.getFormat());
                    ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                    FileIOUtil.writeFile(pictureFile, byteBuffer, false);

                    image.close();

                    ThreadUtil.runOnUIThread(() -> {
                        ToastUtil.show("拍照完成!");
                    });

                }, mHandler);

                CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(isRecording ? CameraDevice.TEMPLATE_VIDEO_SNAPSHOT :
                        CameraDevice.TEMPLATE_STILL_CAPTURE);
                use3AIfSupported(builder);
                builder.addTarget(mImageReader.getSurface());
                builder.set(CaptureRequest.JPEG_ORIENTATION, CameraHelper.getCaptureRotation(mSensorInfo));

                mCaptureSession.capture(builder.build(), new CameraCaptureCallback(), mHandler);

            } catch (CameraAccessException e) {
                resolveError(Error.STRATEGY_REFUSE);
            }
        }
    }

    @Override
    public void startRecord(@NonNull String path) {
        if (mCameraDevice != null) {

            if (mCamera2MediaRecord == null) {
                mCamera2MediaRecord = new Camera2MediaRecord();
            }

            if (prepareMediaRecorder(path)) {

                closePreviewSession();

                try {
                    mCameraDevice.createCaptureSession(Arrays.asList(mCameraPreviewSurface, mImageReader.getSurface(), mCamera2MediaRecord.getSurface()), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCaptureSession = session;
                            try {
                                CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                                use3AIfSupported(builder);
                                useVideoStableIfSupported(builder);
                                builder.addTarget(mCameraPreviewSurface);
                                builder.addTarget(mCamera2MediaRecord.getSurface());

                                mCaptureSession.setRepeatingRequest(builder.build(), new CameraCaptureCallback(), mHandler);

                                mCamera2MediaRecord.startRecord();

                                ThreadUtil.runOnUIThread(() -> {
                                    ToastUtil.show("开始录制!");
                                });

                            } catch (CameraAccessException e) {
                                resolveError(Error.STRATEGY_REFUSE);
                                return;
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, mHandler);

                    isRecording = true;
                } catch (CameraAccessException e) {
                    resolveError(Error.STRATEGY_REFUSE);
                }
            }
        }

    }

    private boolean prepareMediaRecorder(String path) {
        int rotation = CameraHelper.getCaptureRotation(mSensorInfo);
        return mCamera2MediaRecord.prepare(path, rotation, mRealProfile);
    }

    @Override
    public void stopRecord() {

        mCamera2MediaRecord.stopRecord();

        closePreviewSession();

        startPreview();

        isRecording = false;

        ThreadUtil.runOnUIThread(() -> {
            ToastUtil.show("停止录制!");
        });
    }
}
