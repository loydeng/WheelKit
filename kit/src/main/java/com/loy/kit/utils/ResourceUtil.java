package com.loy.kit.utils;


import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.RawRes;
import androidx.annotation.StringRes;

import com.loy.kit.Utils;

import java.io.IOException;
import java.io.InputStream;

public final class ResourceUtil {

    /**
     * @param resId
     * @param objects 替换占位符变量，可为空。 当字符串资源含有占位符时可替换，如 %n$[s|d]  %n表示是第几个替换变量，$s表示是字符串变量，$d表示是整型变量。
     * @return
     */
    public static String getString(int resId, Object... objects) {
        return Utils.getAppContext().getResources().getString(resId, objects);
    }

    public static String getStringByName(String stringName) {
        Context appContext = Utils.getAppContext();
        return getString(appContext.getResources().getIdentifier(stringName, "string", appContext.getPackageName()));
    }

    public static int getColor(int resId) {
        return Utils.getAppContext().getResources().getColor(resId);
    }

    public static int getColorByName(String colorName) {
        Context appContext = Utils.getAppContext();
        return getColor(appContext.getResources().getIdentifier(colorName, "color", appContext.getPackageName()));
    }

    public static Drawable getDrawable(int resId) {
        return Utils.getAppContext().getResources().getDrawable(resId);
    }

    // 获取asset资源文件流, 有子目录时, 需要带上子目录
    public static InputStream getAssetFile(String fileName) {
        try {
            return Utils.getAppContext().getAssets().open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 打开raw文件流, 需传入R.raw.fileId
    public static InputStream getRawFileInputStream(@RawRes int resId) {
        return Utils.getAppContext().getResources().openRawResource(resId);
    }
}


