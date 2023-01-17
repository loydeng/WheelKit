package com.loy.kit.utils;

import android.Manifest;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

/**
 * @author Loy
 * @time 2022/8/30 15:50
 * @des
 */
public class VibrateUtil {

    private static Vibrator sVibrator;

    @RequiresPermission(Manifest.permission.VIBRATE)
    public static boolean vibrate(long time) {
        boolean ret = checkVibrator();
        if (ret) {
            sVibrator.vibrate(time);
        }
        return ret;
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    public static boolean vibrate(long[] pattern, int repeat) {
        boolean ret = checkVibrator();
        if (ret) {
            sVibrator.vibrate(pattern, repeat);
        }
        return ret;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.VIBRATE)
    public static boolean vibrate(VibrationEffect effect) {
        boolean ret = checkVibrator();
        if (ret) {
            sVibrator.vibrate(effect);
        }
        return ret;
    }


    /**
     * @param effectId {@link VibrationEffect.EFFECT_CLICK etc}
     * @return VibrationEffect
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public static VibrationEffect getVibrationEffect(int effectId) {
        return VibrationEffect.createPredefined(effectId);
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    public static boolean cancel() {
        boolean ret = checkVibrator();
        if (ret) {
            sVibrator.cancel();
        }
        return ret;
    }

    private static boolean checkVibrator() {
        if (sVibrator == null) {
            sVibrator = ServiceManagerUtil.getVibrator();
        }
        return sVibrator.hasVibrator();
    }
}
