package com.loy.kit.media.capture.camrea;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.view.TextureView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.loy.kit.log.SdkLog;
import com.loy.kit.media.capture.camrea.bean.Error;
import com.loy.kit.media.capture.camrea.bean.Profile;
import com.loy.kit.media.capture.camrea.bean.SensorInfo;
import com.loy.kit.media.render.TexturePreview;
import com.loy.kit.utils.FileIOUtil;
import com.loy.kit.utils.ThreadUtil;
import com.loy.kit.utils.ToastUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author loy
 * @tiem 2023/7/25 12:12
 * @des
 */
public class Camera1Impl extends BaseCamera {
    private Camera mCamera;

    public Camera1Impl(Handler handler, Context context) {
        super(handler, context);
        mAPI = CameraClient.API.Camera;
    }

    @Override
    public List<SensorInfo> querySensorInfo() {
        mSensorInfoList = new ArrayList<>();
        try {
            int number = Camera.getNumberOfCameras();
            for (int i = 0; i < number; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                Camera camera = Camera.open(i);
                SensorInfo sensorInfo = new SensorInfo(i, info, camera.getParameters());
                mSensorInfoList.add(sensorInfo);
            }
        } catch (Exception e) {
            // 相机被占用 或 不存在相机
            SdkLog.e(TAG, "camera open failure, error:" + e.getMessage());
        }
        return mSensorInfoList;
    }

    @Override
    public Error open(@NonNull SensorInfo sensorInfo, @NonNull FrameLayout preview) {
        mSensorInfo = sensorInfo;
        mPreviewContainer = preview;
        Profile expectProfile = mSensorInfo.getExpectProfile();
        mRealProfile = CameraHelper.getClosestProfile(sensorInfo.getProfiles(), expectProfile);

        int fps = sensorInfo.getExpectFps();
        mBestFpsRange = CameraHelper.getClosestFPSRange(sensorInfo.getFpsRanges(), fps);

        try {
            mCamera = Camera.open(sensorInfo.getCameraId());
            mCamera.setDisplayOrientation(CameraHelper.getPreviewOrientation(sensorInfo));

            setCommonParams();

            mPreviewView = new TexturePreview(mContext,  new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                    SdkLog.e(TAG, "onSurfaceTextureAvailable, width:" + width + ", height:" + height);
                    try {
                        mSurfaceTexture = surface;
                        mSurfaceTexture.setDefaultBufferSize(mRealProfile.getWidth(), mRealProfile.getHeight());
                        mCamera.setPreviewTexture(mSurfaceTexture);
                        mCamera.startPreview();
                    } catch (RuntimeException | IOException e) {
                        SdkLog.e(TAG, "set texture error:" + e.getMessage());
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                    SdkLog.e(TAG, "onSurfaceTextureSizeChanged, width:" + width + ", height:" + height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    SdkLog.e(TAG, "onSurfaceTextureDestroyed");
                    try {
                        mCamera.stopPreview();
                    } catch (Exception e) {
                    }
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                    // SdkLog.e(TAG, "onSurfaceTextureUpdated");
                }
            });

            showPreview();
        } catch (Exception e) {
            // 相机被占用 或 不存在相机
            SdkLog.e(TAG, "camera open failure, error:" + e.getMessage());
            return Error.OPEN_FAILURE;
        }
        return Error.OK;
    }

    private void setCommonParams() {
        Camera.Parameters parameters = mCamera.getParameters();

        // 自动曝光
        boolean autoExposure = parameters.getAutoExposureLock();
        if (!autoExposure) { // 未开启则开启
            if (parameters.isAutoExposureLockSupported()) {
                parameters.setAutoExposureLock(true);
            } else {
                SdkLog.e("not support AutoExposure");
            }
        }

        // 自动白平衡
        boolean autoWhiteBalance = parameters.getAutoWhiteBalanceLock();
        if (!autoWhiteBalance) { // 未开启则开启
            if (parameters.isAutoWhiteBalanceLockSupported()) {
                parameters.setAutoWhiteBalanceLock(true);
            } else {
                SdkLog.e("not support AutoWhiteBalance");
            }
        }

        // 设置对焦模式， 自动对焦
        String focusMode = parameters.getFocusMode();
        String autoFocus = FocusMode.CONTINUOUS_VIDEO.value;
        if (!focusMode.equals(autoFocus)) {
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (supportedFocusModes.contains(autoFocus)) {
                parameters.setFocusMode(autoFocus);
            } else {
                SdkLog.e("not support " + autoFocus);
            }
        }

        // 视频防抖
        if (parameters.isVideoStabilizationSupported()) {
            parameters.setVideoStabilization(true);
        }

        parameters.setPreviewFpsRange(mBestFpsRange.getMin(), mBestFpsRange.getMax());
        parameters.setPreviewSize(mRealProfile.getWidth(), mRealProfile.getHeight());
        parameters.setPictureSize(mRealProfile.getWidth(), mRealProfile.getHeight());
        mCamera.setParameters(parameters);
    }

    @Override
    public void takePicture(@NonNull String path) {
        ThreadUtil.runOnUIThread(() -> {
            ToastUtil.show("拍照中...");
        });
        Camera.Parameters parameters = mCamera.getParameters();
        int rotation = CameraHelper.getCaptureRotation(mSensorInfo);
        parameters.setRotation(rotation); // 拍照时图片方向设置
        if (mSensorInfo.isFront()) {
            parameters.set("preview-flip","off");
        }else {
            parameters.set("preview-flip","flip-h");
        }

        parameters.setPictureSize(mRealProfile.getWidth(), mRealProfile.getHeight());
        mCamera.setParameters(parameters);
        mCamera.takePicture(null, null, (data, camera) -> {
            File pictureFile = new File(path);

            if (!Objects.requireNonNull(pictureFile.getParentFile()).exists()) {
                pictureFile.getParentFile().mkdirs();
            }
            FileIOUtil.writeFile(pictureFile, new ByteArrayInputStream(data), false);
            if (mCamera != null) { // 拍照中会自动停止预览, 这里返回后需要重启预览
                mCamera.startPreview();
            }
            ThreadUtil.runOnUIThread(() -> {
                ToastUtil.show("拍照完成!");
            });
        });
    }

    @Override
    public void startRecord(@NonNull String path) {
        if (mCamera1MediaRecord == null) {
            mCamera1MediaRecord = new Camera1MediaRecord(path, mCamera, mSensorInfo, mRealProfile, mPreviewView.getSurfaceTexture());
            mCamera1MediaRecord.startVideoRecord();
            ThreadUtil.runOnUIThread(() -> {
                ToastUtil.show("开始录制!");
            });
        }
    }

    @Override
    public void stopRecord() {
        if (mCamera1MediaRecord != null) {
            mCamera1MediaRecord.stopVideoRecord();
            mCamera1MediaRecord = null;
            ThreadUtil.runOnUIThread(() -> {
                ToastUtil.show("停止录制!");
            });
        }
    }

    @Override
    public void close() {
        stopRecord();
        mCamera.stopPreview();
        mCamera.release();
        super.close();
    }

    // 相机功能:
    // -------------------------------------------------------------------------------------
    // Android 支持多种相机功能（例如，照片格式、闪光灯模式、对焦设置等），
    // 您可以使用相机应用控制这些功能。本部分列出了一些常用的相机功能，并简要讨论了如何使用它们。
    // 您可以使用 Camera.Parameters 对象访问和设置大多数相机功能。
    //
    // 但有几项重要功能需要的不仅仅是在 Camera.Parameters 中进行简单设置。需要更多代码才能实现的相机功能包括：
    // 区域测光和对焦
    // 人脸检测
    // 延时摄影视频

    // 常用相机功能（按引入时所在 Android API 级别排序）。
    //
    //功能	                API级别	 说明
    //人脸检测	               14	 识别照片中的人脸，并将其用于对焦、测光和白平衡
    //区域测光	               14	 指定图片中的一个或多个区域以计算白平衡
    //区域对焦	               14	 在图片中设置一个或多个区域以用于对焦
    //White Balance Lock	   14	 停止或开始自动白平衡调整
    //Exposure Lock	           14	 停止或开始自动曝光调整
    //Video Snapshot	       14	 在录制视频的同时拍摄照片（帧捕获）
    //延时摄影视频	           11	 以固定延迟时间录制帧，以录制延时摄影视频
    //Multiple Cameras	       9	 支持设备上的多个摄像头，包括前置摄像头和后置摄像头
    //Focus Distance	       9	 报告相机和焦点对象之间的距离
    //Zoom	                   8	 设置图片的放大比例
    //Exposure Compensation	   8	 提高或降低曝光等级
    //GPS Data	               5	 在图片中添加或省略地理位置数据
    //White Balance	           5	 设置白平衡模式，该模式会影响所拍摄图片的颜色值
    //Focus Mode	           5	 设置相机对拍摄主体进行对焦的方式，例如自动对焦、固定对焦、微距对焦或无限远对焦
    //Scene Mode	           5	 对特定类型的摄影场景（例如夜景、海滩场景、雪景或烛光场景）应用预设模式
    //JPEG Quality	           5	 为 JPEG 图片设置压缩级别，这可提高或降低图片输出文件的质量和大小
    //Flash Mode	           5	 开启或关闭闪光灯，或使用自动设置
    //Color Effects	           5	 对所拍摄图片应用色彩效果，例如黑白效果、深褐色调或底片效果。
    //Anti-Banding	           5	 减少由于 JPEG 压缩导致的颜色渐变条纹效果
    //Picture Format	       1	 指定照片的文件格式
    //Picture Size	           1	 指定已保存照片的像素尺寸


    // 由于硬件差异和软件实现方式，并非所有设备都支持这些功能。需要检查设备硬件是否支持这些功能，以及能否在功能不可用时安全地退出。
    // 使用以下方法检查大多数相机功能。
    // Camera.Parameters 对象提供 getSupported...()、is...Supported() 或 getMax...() 方法，
    // 用于确定是否支持（以及在多大程度上支持）某项功能。
    public boolean isSupportFocusMode(FocusMode mode) {
        List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
        return supportedFocusModes.contains(mode.value);
    }

    // 是否支持录像时拍照(帧捕获)
    public boolean isSupportVideoSnapShot() {
        return mCamera.getParameters().isVideoSnapshotSupported();
    }

    // 获取支持识别最大人脸数, 大于0 ,表示支持人脸识别
    public int getMaxDetectedFaces() {
        return mCamera.getParameters().getMaxNumDetectedFaces();
    }


    // 可以使用 Camera.Parameters 对象启用和控制大多数相机功能。
    // 如需获取该对象，首先要获取 Camera 对象实例，调用 getParameters() 方法，
    // 更改返回的参数对象，然后将其设置回相机对象
    // 此方法适用于几乎所有的相机功能，并且在获取 Camera 对象实例后，您可以随时更改大部分参数。
    // 对参数进行更改后，用户通常能够立即在应用的相机预览中看到相关更改。
    // 在软件方面，参数更改可能需要经过几个帧才能真正生效，因为相机硬件需要先处理新指令，然后才会发送更新后的图片数据。
    // 重要提示：某些相机功能无法随意更改。
    // 具体来说，在更改相机预览的大小或屏幕方向时，首先需要停止预览，更改预览大小，然后再重启预览。
    // 从 Android 4.0（API 级别 14）开始，更改预览屏幕方向无需重启预览。

    // 触发对焦， 如
    public void autoFocus(Camera.AutoFocusCallback callback) {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (callback != null) {
                    callback.onAutoFocus(success, camera);
                }
                SdkLog.e("onAutoFocus success state:" + success);
            }
        });
    }

    // 设置区域测光
    // Camera.Area 对象包含两个数据参数：
    // 一个 Rect 对象, 用于指定相机视野范围内的区域）, 坐标范围(-1000, -1000)至(1000, 1000)，即左上到右下
    // 一个 weight 值， 用于指定区域的权重级别。1至1000
    public void setAreaToMeteringAndFocus(List<Camera.Area> meteringAreas) {
        Camera.Parameters params = mCamera.getParameters();
        if (params.getMaxNumMeteringAreas() > 0) { // check that metering areas are supported
            params.setMeteringAreas(meteringAreas);
        }
        mCamera.setParameters(params);
    }

    // 人脸检测
    // 对于包含人物的照片，人脸通常是照片中最重要的部分，在拍摄图片时，应使用人脸确定焦点和白平衡。
    // Android 4.0（API 级别 14）框架提供了一系列 API，可利用人脸识别技术识别人脸并计算照片设置值。
    // 注意：当人脸检测功能处于启用状态时，
    // setWhiteBalance(String)、setFocusAreas(List<Camera.Area>)
    // 和 setMeteringAreas(List<Camera.Area>) 不起任何作用。
    // 在相机应用中使用人脸检测功能时，需要执行若干个一般步骤：
    // 检查设备是否支持人脸检测
    // 创建人脸检测监听器
    // 将人脸检测监听器添加到相机对象
    // 在预览（以及每次重启预览）后启用人脸检测
    public void setDetectedFaceListener(Camera.FaceDetectionListener listener) {
        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                // 人脸总区域, 左右眼和嘴巴区域
                if (listener != null) {
                    listener.onFaceDetection(faces, camera);
                }
                SdkLog.e("face num:" + faces.length);
            }
        });
    }

    // 开始人脸检测, 每次启动（或重启）相机预览时，必须启用人脸检测.
    // 如 预览类中的 surfaceCreated() 和 surfaceChanged() 方法中启动
    // 请记得先调用 startPreview()，然后再调用此方法。
    // 请勿试图在相机应用主 Activity 的 onCreate() 方法中启用人脸检测，因为此时预览在应用的执行过程中尚不可用。
    public boolean startFaceDetection() {
        if (getMaxDetectedFaces() > 0) {
            mCamera.startFaceDetection();
            SdkLog.e("startFaceDetection");
            return true;
        }
        SdkLog.e("not support FaceDetection");
        return false;
    }

    // 停止人脸检测
    public void stopFaceDetection() {
        mCamera.stopFaceDetection();
        mCamera.setFaceDetectionListener(null);
    }

    // 连拍
    // 如果要连续抓取图片，您可以创建 Camera.PreviewCallback，用于实现 onPreviewFrame()。
    // 对于这些图片之间的图片，您可以仅捕获选定的预览画面，或设置延迟操作以调用 takePicture()。


    // 延时摄影视频
    // 借助延时摄影视频功能，用户可以将间隔几秒钟或几分钟拍摄的照片串联起来，创建视频剪辑。
    // 此功能使用 MediaRecorder 以延时摄影顺序录制图片。
    //
    // 如需使用 MediaRecorder 录制延时摄影视频，您必须像录制常规视频一样配置录制器对象，
    // 将每秒捕获的帧数设置成较小的数值并使用一个延时摄影质量设置，如以下代码示例所示。
    // // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
    // mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH));
    //...
    // // Step 5.5: Set the video capture rate to a low number
    //mediaRecorder.setCaptureRate(0.1); // capture a frame every 10 seconds


    public enum FocusMode {

        AUTO(Camera.Parameters.FOCUS_MODE_AUTO), // 需要调用
        INFINITY(Camera.Parameters.FOCUS_MODE_INFINITY),
        MACRO(Camera.Parameters.FOCUS_MODE_MACRO),
        FIXED(Camera.Parameters.FOCUS_MODE_FIXED),
        EDOF(Camera.Parameters.FOCUS_MODE_EDOF),
        CONTINUOUS_PICTURE(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE),
        CONTINUOUS_VIDEO(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO),
        ;
        private String value;

        private FocusMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
