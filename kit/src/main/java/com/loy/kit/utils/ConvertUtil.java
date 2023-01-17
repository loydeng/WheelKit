package com.loy.kit.utils;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.loy.kit.Utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Loy
 * @time 2022/8/19 16:22
 * @des
 */
public class ConvertUtil {

    public static <T> List<T> array2List(T[] array) {
        if (array != null && array.length > 0) {
            return Arrays.asList(array);
        } else {
            return new ArrayList<>();
        }
    }

    public static <T> T[] list2Array(List<T> list) {
        if (list != null && list.size() > 0) {
            return (T[]) list.toArray();
        } else {
            return null;
        }
    }

    public static int dp2px(float dp) {
        return (int) (applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp) + 0.5f);
    }

    public static int px2dp(float px) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int sp2px(float sp) {
        return (int) (applyDimension(TypedValue.COMPLEX_UNIT_SP, sp) + 0.5f);
    }

    public static int px2sp(float px) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (px / fontScale + 0.5f);
    }

    @IntDef({TypedValue.COMPLEX_UNIT_DIP,
            TypedValue.COMPLEX_UNIT_SP,
            TypedValue.COMPLEX_UNIT_PX})
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
    }

    public static float applyDimension(@Type int type, float value) {
        return TypedValue.applyDimension(type, value, Resources.getSystem().getDisplayMetrics());
    }


    /**
     * 将颜色值类型由 color-string 转换为 color-int.
     * color-string 可以是 rgb 或 argb, 格式: #RRGGBB 或 #AARRGGBB
     *
     */
    public static int string2Int(@NonNull String colorString) {
        return Color.parseColor(colorString);
    }

    /**
     *  将颜色值类型由 color-int 转换为 color-string(rgb)
     */
    public static String int2RgbString(@ColorInt int colorInt) {
        colorInt = colorInt & 0x00ffffff;
        String color = Integer.toHexString(colorInt);
        while (color.length() < 6) {
            color = "0" + color;
        }
        return "#" + color;
    }

    /**
     * 将颜色值类型由 color-int 转换为 color-string(argb)
     */
    public static String int2ArgbString(@ColorInt final int colorInt) {
        String color = Integer.toHexString(colorInt);
        while (color.length() < 6) {
            color = "0" + color;
        }
        while (color.length() < 8) {
            color = "f" + color;
        }
        return "#" + color;
    }

}
