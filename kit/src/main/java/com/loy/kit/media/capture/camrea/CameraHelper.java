package com.loy.kit.media.capture.camrea;

import static java.lang.Math.abs;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import com.loy.kit.log.SdkLog;
import com.loy.kit.media.capture.camrea.bean.CaptureFormat;
import com.loy.kit.media.capture.camrea.bean.FrameRateRange;
import com.loy.kit.media.capture.camrea.bean.Profile;
import com.loy.kit.media.capture.camrea.bean.Resolution;
import com.loy.kit.media.capture.camrea.bean.SensorInfo;
import com.loy.kit.utils.DirUtil;
import com.loy.kit.utils.OrientationUtil;
import com.loy.kit.utils.ServiceManagerUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author loy
 * @tiem 2023/7/24 9:12
 * @des
 */
public class CameraHelper {
    public static final String TAG = "CameraHelper";

    private static int cameraLevel = -1;

    private final static double NANO_SECONDS_PER_SECOND = 1.0e9;

    // 相机是否可用
    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public static boolean isSupportCamera2() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        } else {
            if (cameraLevel == -1) {
                cameraLevel = 2;
                CameraManager cameraManager = ServiceManagerUtil.getCameraManager();
                try {
                    String[] cameraIdList = cameraManager.getCameraIdList();
                    for (String cameraId : cameraIdList) {
                        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                        int level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                        // INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY: 表示相机只支持相机1.0 API级别，没有支持相机2.0及以上的新特性。
                        // 通常是旧一些的设备或相机实现，功能较为有限。
                        // INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED: 表示相机支持相机2.0 API级别，但可能不支持所有的高级特性。
                        // 相机2.0 API提供了更多的功能，如手动控制、原生拍摄RAW照片等，但限制了某些高级功能的支持。
                        // INFO_SUPPORTED_HARDWARE_LEVEL_FULL: 表示相机完全支持相机2.0 API级别，并提供了所有高级特性。相机2.0 API提供了更多的功能和控制选项，以满足专业摄影师和开发者的需求。
                        // INFO_SUPPORTED_HARDWARE_LEVEL_3: 表示相机除了支持完全可用的相机2.0 API级别外，还提供了额外的高级功能和性能。
                        // 该级别在 Android 5.0（API级别 21）之后引入，支持额外的高级特性，如更快的捕捉速度、更高的帧率和更大的图像传感器。
                        if (level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                            cameraLevel = 1;
                            break;
                        }
                    }
                } catch (CameraAccessException e) {
                    cameraLevel = 1;
                    SdkLog.printErrorStack(e);
                }
            }
            return cameraLevel == 2;
        }
    }


    // 比较器
    private static abstract class ClosestComparator<T> implements Comparator<T> {
        abstract int diff(T supportedParameter);

        @Override
        public int compare(T t1, T t2) {
            return diff(t1) - diff(t2);
        }
    }

    // 计算最匹配的尺寸, 分别结算宽高的差值, 其和最小即为最接近尺寸
    public static Resolution getClosestResolution(List<Resolution> resolutionList, final int requestedWidth, final int requestedHeight) {
        return Collections.min(resolutionList, new ClosestComparator<Resolution>() {
            @Override
            int diff(Resolution resolution) {
                return abs(requestedWidth - resolution.getWidth()) + abs(requestedHeight - resolution.getHeight());
            }
        });
    }

    public static Profile getClosestProfile(List<Profile> profileList, final Profile targetProfile) {
        return Collections.min(profileList, new ClosestComparator<Profile>() {
            @Override
            int diff(Profile supportedParameter) {
                return abs(supportedParameter.getWidth() - targetProfile.getWidth()) + abs(supportedParameter.getHeight() - targetProfile.getHeight());
            }
        });
    }

    // 根据权重计算 最匹配的 FPS 值
    public static FrameRateRange getClosestFPSRange(List<FrameRateRange> supportedFPSRanges, final int requestedFps) {
        return Collections.min(supportedFPSRanges, new ClosestComparator<FrameRateRange>() {
            private static final int MAX_FPS_DIFF_THRESHOLD = 5000;
            private static final int MAX_FPS_LOW_DIFF_WEIGHT = 1;
            private static final int MAX_FPS_HIGH_DIFF_WEIGHT = 3;

            private static final int MIN_FPS_THRESHOLD = 8000;
            private static final int MIN_FPS_LOW_VALUE_WEIGHT = 1;
            private static final int MIN_FPS_HIGH_VALUE_WEIGHT = 4;

            private int progressivePenalty(int value, int threshold, int lowWeight, int highWeight) {
                return (value < threshold) ? value * lowWeight : threshold * lowWeight + (value - threshold) * highWeight;
            }

            @Override
            int diff(FrameRateRange range) {
                final int minFpsError = progressivePenalty(range.getMin(), MIN_FPS_THRESHOLD, MIN_FPS_LOW_VALUE_WEIGHT, MIN_FPS_HIGH_VALUE_WEIGHT);
                final int maxFpsError = progressivePenalty(abs(requestedFps * 1000 - range.getMax()), MAX_FPS_DIFF_THRESHOLD, MAX_FPS_LOW_DIFF_WEIGHT, MAX_FPS_HIGH_DIFF_WEIGHT);
                return minFpsError + maxFpsError;
            }
        });
    }

    public static int getFpsUnitFactor(Range<Integer>[] fpsRanges) {
        if (fpsRanges.length == 0) {
            return 1000;
        }
        return fpsRanges[0].getUpper() < 1000 ? 1000 : 1;
    }

    static List<CaptureFormat> getCaptureFormats(SensorInfo sensorInfo) {
        List<FrameRateRange> ranges = sensorInfo.getFpsRanges();
        int defaultMaxFps = 0;
        for (FrameRateRange range : ranges) {
            defaultMaxFps = Math.max(defaultMaxFps, range.getMax());
        }

        List<CaptureFormat> formats = new ArrayList<>();
        List<Resolution> resolutions = sensorInfo.getPreviewResolutions();
        for (Resolution resolution : resolutions) {
            long minFrameDurationNs = 0;
            try {
                minFrameDurationNs = sensorInfo.getCharacteristics()
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputMinFrameDuration(SurfaceTexture.class, new Size(resolution.getWidth(), resolution.getHeight()));
            } catch (Exception e) {
                // getOutputMinFrameDuration() is not supported on all devices. Ignore silently.
            }
            final int maxFps = (minFrameDurationNs == 0) ? defaultMaxFps
                    : (int) Math.round(NANO_SECONDS_PER_SECOND / minFrameDurationNs) * 1000;
            for (Profile profile : Profile.values()) {
                if (profile.isMatchResolution(resolution)) {
                    formats.add(new CaptureFormat(profile, new FrameRateRange(0, maxFps, ranges.get(0).getFactor())));
                }
            }
        }
        return formats;
    }

    public static int getDeviceRotation() {
        int rotation = ServiceManagerUtil.getWindowManager().getDefaultDisplay().getRotation();
        int degree;
        switch (rotation) {
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
            case Surface.ROTATION_0:
            default:
                degree = 0;
                break;
        }
        return degree;
    }

    public static int getPreviewOrientation(@NonNull SensorInfo sensorInfo) {
        int degree = getDeviceRotation();
        if (sensorInfo.isFront()) {
            return (720 - (degree + sensorInfo.getOrientation())) % 360;
        } else {
            return (360 - degree + sensorInfo.getOrientation()) % 360;
        }
    }

    public static int getCaptureRotation(@NonNull SensorInfo sensorInfo) {
        int orientation = OrientationUtil.getInstance().getCameraOrientation();
        int rotation;
        if (sensorInfo.isFront()) {
            rotation = (sensorInfo.getOrientation() - orientation + 360) % 360;
        } else {
            rotation = (sensorInfo.getOrientation() + orientation) % 360;
        }
        return rotation;
    }

    public static int getFrameRotation(@NonNull SensorInfo sensorInfo) {
        int degree = getDeviceRotation();
        if (sensorInfo.isBack()) {
            return 360 - degree;
        } else {
            return (sensorInfo.getOrientation() + degree) % 360;
        }
    }

    // 改变持久化后的图片方向
    public void rotateImage(String path, int degree) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            exifInterface.rotate(degree);
            exifInterface.saveAttributes();
        } catch (IOException e) {
            SdkLog.e(TAG, "rotateImage IO Error:" + e.getMessage());
        }
    }

    /**
     * Create a file Uri for saving an image or video
     */
    public static Uri getOutputMediaFileUri(String fileExtensionName) {
        return Uri.fromFile(DirUtil.nameMediaFileCurrentTime(fileExtensionName));
    }

    // 按实际显示的大小压缩图片，节省内存。最大采样为原始图片大小；例如，采样因子为2，则宽高都是原图1/2。总体大小为原始图片1/4
    public static void setPic(ImageView imageView, String currentPhotoPath) {
        // 获取图片显示尺寸
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // 仅获取原始图片尺寸
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // 计算采样因子
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // 按比例因子缩放图片
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        // 显示图片
        imageView.setImageBitmap(bitmap);
    }

    //获取视频时长
    public static long getDuration(Context context, Uri uri) {
        int duration = 0;
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration() / 1000;   // 获取到的是毫秒值
            mediaPlayer.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return duration;
    }

    //获取视频的第一帧的图片
    public static Bitmap getFirstFrame(Uri uri) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        String videoPath = uri.getPath();            // 通过Uri获取绝对路径
        media.setDataSource(videoPath);
        Bitmap bitmap = media.getFrameAtTime();      // 视频的第一帧图片
        return bitmap;
    }
}
