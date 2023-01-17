package com.loy.kit.tool;

import android.os.StrictMode;

import com.loy.kit.BuildConfig;

/**
 * @author loyde
 * @des
 * @time 2022/12/11 12:33
 */
public class StrictModeSwitcher {
    /**
     * 开启 StrictMode
     * @param clazz 限定为单例的类
     */
    public static void openStrictMode(Class<?>... clazz) {
        if (BuildConfig.DEBUG) { // debug 模式才开启
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectCustomSlowCalls()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog() // 基于log打印出违规信息
                    .build());
            StrictMode.VmPolicy.Builder vmBuilder = new StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .penaltyLog();
            if (clazz != null && clazz.length > 0) {
                for (Class c : clazz) {
                    vmBuilder.setClassInstanceLimit(c, 1); //检测单例
                }
            }
            StrictMode.setVmPolicy(vmBuilder.build());
        }
    }
}
