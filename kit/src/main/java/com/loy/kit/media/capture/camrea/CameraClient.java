package com.loy.kit.media.capture.camrea;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.FrameLayout;

import com.loy.kit.media.capture.camrea.bean.Error;

import androidx.annotation.NonNull;

import com.loy.kit.media.capture.camrea.bean.SensorInfo;
import com.loy.kit.utils.OrientationUtil;

import java.util.List;

/**
 * @author loy
 * @tiem 2023/7/14 10:18
 * @des
 */
public class CameraClient {
    private final HandlerThread mHandlerThread;
    private final Handler mHandler;
    private BaseCamera mCamera;
    private boolean isRecording = false;

    public enum API {
        Camera,
        Camera2,
        //CameraX
    }

    public interface Callback<T> {
        void onSuccess(T result);

        void onFailure(Error error);
    }

    public static boolean isCameraAvailable(Context context) {
        return CameraHelper.hasCamera(context);
    }

    public CameraClient(Context context) {
        this(context, null);
    }

    public CameraClient(Context context, API api) {
        mHandlerThread = new HandlerThread("CameraClient");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        if (CameraHelper.isSupportCamera2()) {
            mCamera = getCameraByAPI(api, context, mHandler);
        } else {
            mCamera = new Camera1Impl(mHandler, context);
        }
    }

    private BaseCamera getCameraByAPI(API api, Context context, Handler handler) {
        switch (api) {
            case Camera:
                return new Camera1Impl(handler, context);
            case Camera2:
            default:
                return new Camera2Impl(handler, context);
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    private void doWork(Runnable runnable) {
        mHandler.post(runnable);
    }

    public <T> void onCallback(Callback<T> callback, boolean condition, T t, Error error) {
        if (condition) {
            callback.onSuccess(t);
        } else {
            callback.onFailure(error);
        }
    }

    public void querySensorInfo(@NonNull Callback<List<SensorInfo>> callback) {
        doWork(() -> {
            List<SensorInfo> sensorInfoList = mCamera.querySensorInfo();
            onCallback(callback, !sensorInfoList.isEmpty(), sensorInfoList, Error.OPEN_FAILURE);
        });
    }

    public void open(SensorInfo sensorInfo, FrameLayout preview) {
        doWork(() -> {
            OrientationUtil.getInstance().startOrientationListener();
            mCamera.open(sensorInfo, preview);
        });
    }

    public void close() {
        doWork(()->{
            mCamera.close();
            mHandler.removeCallbacksAndMessages(null);
            mHandlerThread.quitSafely();
            OrientationUtil.getInstance().stopOrientationListener();
        });
    }

    public void outputPreview() {

    }

    public void takePicture(String path) {
        doWork(() -> {
            mCamera.takePicture(path);
        });
    }

    public void startRecord(String path) {
        doWork(() -> {
            if (!isRecording) {
                mCamera.startRecord(path);
                isRecording = true;
            }
        });
    }

    public void stopRecord() {
        doWork(() -> {
            if (isRecording) {
                mCamera.stopRecord();
                isRecording = false;
            }
        });
    }

    public void switcher() {
        doWork(()->{
            mCamera.switchCamera();
        });
    }
}
