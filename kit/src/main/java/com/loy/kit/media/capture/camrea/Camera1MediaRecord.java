package com.loy.kit.media.capture.camrea;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.Surface;
import com.loy.kit.log.SdkLog;
import com.loy.kit.media.capture.camrea.bean.Profile;
import com.loy.kit.media.capture.camrea.bean.SensorInfo;

import java.io.IOException;

/**
 * @author loy
 * @tiem 2023/7/25 16:23
 * @des
 */
public class Camera1MediaRecord {
    public static final int AUDIO_SAMPLE_RATE = 44100; // 44.1k, 48k
    public static final int AUDIO_BIT_RATE = 128000;   // 64k,128k,192k,256k
    public static final int STEREO_CHANNEL = 2;        // Mono 1, Stereo 2
    public static final int FRAME_RATE = 30;           // 视频录制帧率
    MediaRecorder mediaRecorder;
    Camera mCamera;

    public Camera1MediaRecord(String path, Camera camera, SensorInfo sensorInfo, Profile profile, SurfaceTexture surfaceTexture) {
        mCamera = camera;

        // 创建 媒体录制器
        mediaRecorder = new MediaRecorder();

        // 解锁相机, 并设置给 媒体录制器
        //从 Android 4.0（API 级别 14）开始，系统将自动为您管理 Camera.lock() 和 Camera.unlock() 调用。
        mCamera.unlock(); // 解锁Camera硬件，使其他应用可以访问。
        mediaRecorder.setCamera(mCamera);

        // 设置音视频的来源
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // 初始化媒体录制器
        int rotation = CameraHelper.getCaptureRotation(sensorInfo);
        mediaRecorder.setOrientationHint(rotation);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mediaRecorder.setVideoEncodingBitRate(profile.getBitrate()); // 视频比特率(码率)
        mediaRecorder.setVideoFrameRate(FRAME_RATE);     // 视频帧率
        mediaRecorder.setVideoSize(profile.getWidth(), profile.getHeight());// 视频分辨率

        mediaRecorder.setAudioChannels(STEREO_CHANNEL);        // 音频声道
        mediaRecorder.setAudioEncodingBitRate(AUDIO_BIT_RATE); // 音频比特率(码率)
        mediaRecorder.setAudioSamplingRate(AUDIO_SAMPLE_RATE); // 音频采样率

        // 设置音视频编码器, 默认是 h264 和 aac 编码器
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        // 以上初始化媒体录制器过程可以直接使用 mediaRecorder.setProfile(profile);

        // 设置存储文件路径
        mediaRecorder.setOutputFile(path);

        // 设置预览输出的容器 surface
        mediaRecorder.setPreviewDisplay(new Surface(surfaceTexture));
    }

    // 释放媒体录制器, 并释放相机给最后使用者
    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            // 锁定Camera硬件，使其他应用无法访问。
            mCamera.lock();   // lock camera for later use
        }
    }

    public void startVideoRecord() {
        // 基于 MediaRecorder(媒体录制器) 录制. 主要看 prepareVideoRecorder() 方法
        // 解锁相机 -> 初始化媒体录制器 -> 媒体录制器启动录制 -> 录制中
        // -> 媒体录制器停止录制 -> 释放媒体录制器 -> 锁定相机
        if (mediaRecorder == null) {
            return;
        }
        mCamera.stopPreview();
        try {
            mediaRecorder.prepare(); // 媒体录制器 准备配置
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            SdkLog.e("IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        } catch (IOException e) {
            SdkLog.e("IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        }
    }

    public void stopVideoRecord() {
        if (mediaRecorder == null) {
            return;
        }
        mediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        // 锁定Camera硬件，使其他应用无法访问。
        mCamera.lock();    // take camera access back from MediaRecorder
        mCamera.startPreview();
    }

}
