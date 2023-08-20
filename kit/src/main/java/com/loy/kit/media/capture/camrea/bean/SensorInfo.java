package com.loy.kit.media.capture.camrea.bean;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.util.Range;
import android.util.Size;

import androidx.annotation.NonNull;

import com.loy.kit.media.capture.camrea.BaseCamera;
import com.loy.kit.media.capture.camrea.CameraHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author loy
 * @tiem 2023/2/26 14:45
 * @des
 */
public class SensorInfo {
    public static final int BACK = 0;
    public static final int FRONT = 1;
    public static final int EXTERNAL = 2; //  外部相机（例如USB相机）

    private final int mCameraId;
    private final int type;
    private final int orientation;
    private final boolean canMute;

    // 仅用于查询 CameraSensor 支持的规格和特性， 不能查询可设置参数， 因为不是实际的实时设置值。免除调用和解析
    private final Camera.Parameters mParameters;

    private final CameraCharacteristics mCharacteristics;

    private final List<Resolution> previewResolutions;
    private final List<FrameRateRange> fpsRanges;
    private final List<Resolution> pictureResolutions;
    private final List<Resolution> videoResolutions;

    private final List<Profile> profiles;

    private Profile expectProfile;
    private int expectFps;

    public SensorInfo(int cameraId, @NonNull Camera.CameraInfo info, @NonNull Camera.Parameters parameters) {
        this.mCameraId = cameraId;
        this.type = info.facing;
        this.orientation = info.orientation;
        this.canMute = info.canDisableShutterSound;
        this.mParameters = parameters;
        this.mCharacteristics = null;
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        previewResolutions = convertSize(supportedPreviewSizes);
        List<int[]> supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();
        fpsRanges = convertFPS(supportedPreviewFpsRange);
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        pictureResolutions = convertSize(supportedPictureSizes);
        List<Camera.Size> supportedVideoSizes = parameters.getSupportedVideoSizes();
        videoResolutions = convertSize(supportedVideoSizes);
        profiles = new ArrayList<>();
        for (Profile value : Profile.values()) {
            for (Resolution resolution : previewResolutions) { // 以预览分辨率为准
                if (value.isMatchResolution(resolution)) {
                    profiles.add(value);
                }
            }
        }
    }

    public SensorInfo(int cameraId, @NonNull CameraCharacteristics characteristics) {
        this.mCameraId = cameraId;
        this.mCharacteristics = characteristics;
        this.mParameters = null;
        this.type = getCamera2Type(characteristics.get(CameraCharacteristics.LENS_FACING));
        this.orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        this.canMute = false;

        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        Size[] previewSizes = map.getOutputSizes(SurfaceTexture.class);
        previewResolutions = convertSize(previewSizes);

        Size[] pictureSizes = map.getOutputSizes(ImageFormat.JPEG);
        pictureResolutions = convertSize(pictureSizes);
        Size[] videoSizes = map.getOutputSizes(MediaRecorder.class);
        videoResolutions = convertSize(videoSizes);
        Range<Integer>[] ranges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        fpsRanges = convertFPS(ranges);

        profiles = new ArrayList<>();
        for (Profile value : Profile.values()) {
            for (Resolution resolution : previewResolutions) {
                if (value.isMatchResolution(resolution)) {
                    profiles.add(value);
                }
            }
        }
    }

    public static List<Resolution> convertSize(List<android.hardware.Camera.Size> cameraSizes) {
        final List<Resolution> sizes = new ArrayList<>();
        for (android.hardware.Camera.Size size : cameraSizes) {
            sizes.add(new Resolution(size.width, size.height));
        }
        return sizes;
    }

    public static List<FrameRateRange> convertFPS(List<int[]> arrayRanges) {
        final List<FrameRateRange> ranges = new ArrayList<>();
        for (int[] range : arrayRanges) {
            ranges.add(new FrameRateRange(range[android.hardware.Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                    range[android.hardware.Camera.Parameters.PREVIEW_FPS_MAX_INDEX],1));
        }
        return ranges;
    }

    public static List<Resolution> convertSize(@NonNull Size[] cameraSizes) {
        final List<Resolution> sizes = new ArrayList<>();
        for (android.util.Size size : cameraSizes) {
            sizes.add(new Resolution(size.getWidth(), size.getHeight()));
        }
        return sizes;
    }

    public static List<FrameRateRange> convertFPS(Range<Integer>[] arrayRanges) {
        final List<FrameRateRange> ranges = new ArrayList<>();
        int factor = CameraHelper.getFpsUnitFactor(arrayRanges);
        for (Range<Integer> range : arrayRanges) {
            ranges.add(new FrameRateRange(range.getLower() * factor, range.getUpper() * factor, factor));
        }
        return ranges;
    }

    public SensorInfo setExpectFps(int expectFps) {
        this.expectFps = Math.max(expectFps, 0);
        return this;
    }

    public int getExpectFps() {
        if (expectFps == 0) {
            expectFps = BaseCamera.DEFAULT_FPS; // 没有设置时默认使用 30 帧率
        }
        return expectFps;
    }

    public SensorInfo setExpectProfile(Profile expectProfile) {
        this.expectProfile = expectProfile;
        return this;
    }

    public Profile getExpectProfile() {
        if (expectProfile == null) {
            expectProfile = profiles.get(0); // 没有设置时使用第一个
        }
        return expectProfile;
    }

    public Camera.Parameters getParameters() {
        return mParameters;
    }

    public CameraCharacteristics getCharacteristics() {
        return mCharacteristics;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public List<Resolution> getPreviewResolutions() {
        return previewResolutions;
    }

    public List<FrameRateRange> getFpsRanges() {
        return fpsRanges;
    }

    public List<Resolution> getPictureResolutions() {
        return pictureResolutions;
    }

    public List<Resolution> getVideoResolutions() {
        return videoResolutions;
    }

    private int getCamera2Type(int lensFacing) {
        if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
            return BACK;
        } else if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            return FRONT;
        } else {
            return EXTERNAL;
        }
    }

    public int getCameraId() {
        return mCameraId;
    }

    public String getCameraIdString() {
        return String.valueOf(mCameraId);
    }

    public boolean isFront() {
        return type == FRONT;
    }

    public boolean isBack() {
        return type == BACK;
    }

    public boolean isExternal() {
        return type == EXTERNAL;
    }

    public int getOrientation() {
        return orientation;
    }

    public boolean canMuteShutter() {
        return canMute;
    }

    @Override
    public String toString() {
        return "SensorInfo{" +
                "mCameraId=" + mCameraId +
                ", type=" + type +
                ", orientation=" + orientation +
                ", canMute=" + canMute +
                '}';
    }
}
