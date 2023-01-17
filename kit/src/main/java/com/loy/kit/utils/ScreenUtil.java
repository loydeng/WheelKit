package com.loy.kit.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.loy.kit.Utils;

/**
 * @author Loy
 * @time 2021/6/7 15:30
 * @des
 */
public class ScreenUtil {

    // 返回屏幕的最远点point, x -> width , y -> height
    public static Point getScreenSize() {
        WindowManager wm = (WindowManager) Utils.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point(0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point;
    }

    // 返回屏幕的参数对象, 不包含系统UI 占用, 如: 导航栏和状态栏
    public static DisplayMetrics getScreenMetrics() {
        WindowManager wm = (WindowManager) Utils.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    // 返回屏幕的参数对象, 全屏
    public static DisplayMetrics getScreenRealMetrics() {
        WindowManager wm = (WindowManager) Utils.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics;
    }

    public static boolean isLandscape() {
        return Utils.getAppContext().getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isPortrait() {
        return Utils.getAppContext().getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }

    public static int getScreenRotation(@NonNull final Activity activity) {
        switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            default:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
    }
}
