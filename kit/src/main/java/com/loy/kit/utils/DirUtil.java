package com.loy.kit.utils;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import com.loy.kit.Utils;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 提供获取 android app 各个目录的api
 *
 * @author Loy
 * @time 2022/8/30 15:48
 * @des
 */
public class DirUtil {

    /**
     * 兼容方式获取缓存目录
     * @return 返回缓存目录, 若已挂载, 则优先返回外部存储的缓存目录, 否则返回内部存储缓存目录
     */
    public static String compatCacheDir(@NonNull Context context) {
        if (isMounted()) {
            return getExternalCacheDir(context);
        }else {
            return getInternalCacheDir(context);
        }
    }

    public static String compatCacheDir() {
        return compatCacheDir(Utils.getAppContext());
    }

    public static String compatFileDir(@NonNull Context context, String name) {
        if (isMounted()) {
            return getExternalFileDir(context, name);
        }else {
            return getInternalFileDir(context) + FileIOUtil.FILE_SEP + name;
        }
    }

    public static String compatFileDir(String name) {
        return compatFileDir(Utils.getAppContext(), name);
    }

    /**
     * app 内部存储, 总是可获得, 系统设置中可清除, 外部应用不可见, 需root权限后才能读取查看
     *
     * @return /data/data/<PackageName>/cache
     */
    public static String getInternalCacheDir() {
        return getInternalCacheDir(Utils.getAppContext());
    }

    public static String getInternalCacheDir(@NonNull Context context) {
        return context.getCacheDir().getAbsolutePath();
    }

    /**
     * app 内部存储, 总是可获得, 应用卸载自动删除, 外部应用不可见, 需root权限后才能读取查看
     *
     * @return /data/data/<PackageName>/files
     */
    public static String getInternalFileDir() {
        return getInternalFileDir(Utils.getAppContext());
    }

    public static String getInternalFileDir(@NonNull Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    /**
     * app 内部存储, 数据库目录路径
     *
     * @return the path of /data/data/package/databases
     */
    public static String getInternalDBDir() {
        return Utils.getAppContext().getApplicationInfo().dataDir + "/databases";
    }

    /**
     * app 内部存储, SP 目录路径
     *
     * @return the path of /data/data/package/shared_prefs
     */
    public static String getInternalSPDir() {
        return Utils.getAppContext().getApplicationInfo().dataDir + "/shared_prefs";
    }


    /**
     * 判断外部存储是否挂载
     *
     * @return 应用外部存储是否挂载
     */
    public static boolean isMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    /**
     * app 外部存储, 挂载状态下可见, 清理缓存不被删除, 应用卸载自动删除, 外部应用不可见, 不用 root可用文件管理器查看
     *
     * @return 已挂载返回 /storage/emulated/0/Android/<PackageName>/cache 否则返回 ""
     */
    public static String getExternalCacheDir() {
        return getExternalCacheDir(Utils.getAppContext());
    }

    public static String getExternalCacheDir(@NonNull Context context) {
        return getAbsolutePath(context.getExternalCacheDir());
    }


    /**
     * app 外部存储, 挂载状态下可见, 应用卸载自动删除, 外部应用不可见, 不用 root可用文件管理器查看
     *
     * @param name
     * @return 已挂载返回 /storage/emulated/0/Android/<PackageName>/<name> 否则返回 ""
     */
    public static String getExternalFileDir(String name) {
        return getExternalFileDir(Utils.getAppContext(), name);
    }

    public static String getExternalFileDir(@NonNull Context context, String name) {
        return getAbsolutePath(context.getExternalFilesDir(name));
    }

    /**
     * 返回公共存储目录的根路径
     * android 10 (29) 有创建目录的限制, 需要在manifest文件的application节点中添加属性
     * android:requestLegacyExternalStorage="true"
     * 才能创建文件夹
     * 应用卸载后不删除
     * @return 已挂载返回  /storage/emulated/0 否则返回 ""
     */
    public static String getPublicRootDir() {
        return getAbsolutePath(Environment.getExternalStorageDirectory());
    }


    /**
     * @see "android.os.Environment.STANDARD_DIRECTORIES" 外部不可见, 源码可见
     */
    @StringDef({"Music", "Podcasts", "Ringtones", "Alarms", "Notifications", "Pictures", "Movies", "Download", "DCIM", "Documents", "Screenshots", "Audiobooks"})
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
    }

    /**
     * 返回公共存储目录中不同类型文件夹路径
     *
     * @param type 文件类型 @see android.os.Environment.STANDARD_DIRECTORIES
     * @return 已挂载返回 /storage/emulated/0/<type> 否则返回 ""
     */
    public static String getPublicFileDirByName(@Type String type) {
        return getAbsolutePath(Environment.getExternalStoragePublicDirectory(type));
    }

    /**
     *
     * @return /system
     */
    public static String getRootDir() {
        return getAbsolutePath(Environment.getRootDirectory());
    }

    /**
     *
     * @return /data
     */
    public static String getDataDir() {
        return getAbsolutePath(Environment.getDataDirectory());
    }

    private static String getAbsolutePath(File file) {
        if (file == null) {
            return "";
        }else{
            return file.getAbsolutePath();
        }
    }

}
