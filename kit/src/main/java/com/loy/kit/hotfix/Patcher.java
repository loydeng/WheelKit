package com.loy.kit.hotfix;

import android.content.Context;

import com.loy.kit.Utils;
import com.loy.kit.log.SdkLog;
import com.loy.kit.utils.ReflectUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

/**
 * @author loyde
 * @des
 * @time 2022/12/12 11:46
 */
public class Patcher {
    public static final String TAG = Patcher.class.getSimpleName();
    private static final String FIELD_PATH_LIST = "pathList";
    private static final String FIELD_DEX_ELEMENTS = "dexElements";
    private static final String METHOD_MAKE_PATH_ELEMENTS = "makePathElements";

    //         ClassLoader
    //            /|\
    //             |
    //       BaseDexClassLoader
    //       /|\        /|\  |- DexPathList pathList
    //        |          |        |-  Element[] dexElements
    //        |          |        |-  private static Element[] makePathElements(List<File>, File, List<IOException>)
    //        |          |
    //  DexClassLoader  PathClassLoader (from context.getClassLoader())
    //  应用补丁, 文件类型为.apk or .dex 均可, 一般是 .dex 的差分包, 使用 javac .java -> .class, d8 .class -> .dex
    public static boolean injectDex(ArrayList<File> extraDexFiles) {
        if (extraDexFiles == null || extraDexFiles.isEmpty()) {
            SdkLog.color(TAG, "injectDex", "extraDexFiles is empty!");
            return false;
        }

        // 获取系统 ClassLoader 的 pathList 对象
        Context appContext = Utils.getAppContext();
        ClassLoader classLoader = appContext.getClassLoader(); // PatchClassLoader

        try {
            // 获取系统ClassLoader 的 DexPathList 对象
            Object patchList = ReflectUtil.getFiledObject(classLoader, FIELD_PATH_LIST);
            File parentFile = extraDexFiles.get(0).getParentFile();
            ArrayList<IOException> suppressedExceptions = new ArrayList<>();
            // 基于DexPathList 对象构造补丁的 DexElements
            Object[] patchDexElements = ReflectUtil.callMethod(patchList, METHOD_MAKE_PATH_ELEMENTS,
                    new Object[]{extraDexFiles, parentFile, suppressedExceptions});
            if (!suppressedExceptions.isEmpty()) {
                throw suppressedExceptions.get(0);
            }
            // 预先取出原有的 DexElements
            Object[] originalDexElements = ReflectUtil.getFiledObject(patchList, FIELD_DEX_ELEMENTS);
            // 将补丁的 DexElements 注入系统ClassLoader 的 DexPathList 对象的 DexElements 最前面
            Object[] combinedDexElements = (Object[]) Array.newInstance(originalDexElements.getClass().getComponentType(),
                    patchDexElements.length + originalDexElements.length);
            System.arraycopy(patchDexElements, 0, combinedDexElements, 0, patchDexElements.length);
            System.arraycopy(originalDexElements, 0, combinedDexElements, 0, originalDexElements.length);
            // 将合并补丁的 DexElements 设置回去
            ReflectUtil.setFieldObject(patchList, FIELD_DEX_ELEMENTS, combinedDexElements);
            return true;
        } catch (Exception e) {
            SdkLog.e(TAG, e.getMessage());
        }
        return false;
    }


    // pluginDexFile 为 .apk or .dex
    public static boolean loadClass(File pluginDexFile, String className) {
        DexClassLoader classLoader = new DexClassLoader(pluginDexFile.getPath(), pluginDexFile.getParent(), null, null);
        try {
            Class<?> loadClass = classLoader.loadClass(className);
            //Method method = loadClass.getDeclaredMethod("method");
            //method.invoke(obj, args);
            return true;
        } catch (ClassNotFoundException e) {
            SdkLog.e(TAG, e.getMessage());
        }
        return false;
    }
}
