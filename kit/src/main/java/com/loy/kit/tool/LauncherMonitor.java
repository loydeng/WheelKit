package com.loy.kit.tool;

import android.os.Debug;
import android.os.Trace;

import androidx.core.os.TraceCompat;

/**
 * @author loyde
 * @des monitor app launcher by use startMonitor and stopMonitor, then generate app.trace that
 * can use Android Studio Open it in profiler
 * @time 2022/12/2 16:49
 */
public class LauncherMonitor {
    private static final String DEFAULT_TRACE_FILE_NAME = "app.trace";
    private static final String DEFAULT_SYS_TRACE_FILE_NAME = "app.systrace";

    public static void startMonitor() {
        startMonitor(DEFAULT_TRACE_FILE_NAME);
    }

    public static void startMonitor(String fileName) {
        Debug.startMethodTracing(fileName);
    }

    public static void stopMonitor() {
        Debug.stopMethodTracing();
    }


    // android 9 及以上设备可开启跟踪记录, 参考链接: https://developer.android.google.cn/topic/performance/tracing/command-line
    // 1. 有安装 python 解释器环境
    // 2. 有android sdk, 在 sdk\platform-tools\systrace 目录下有 python 脚本 systrace.py
    // 3. 在脚本所在目录下, 执行以下命令, 监听程序执行, 程序中有执行Trace.beginSection / Trace.endSection 的API , 则会输出记录文件 mynewtrace.html
    public static void startSysTrace() {
        TraceCompat.beginAsyncSection(DEFAULT_SYS_TRACE_FILE_NAME,0);
        //Trace.beginSection(DEFAULT_SYS_TRACE_FILE_NAME);
    }

    public static void stopSysTrace() {
        TraceCompat.endAsyncSection(DEFAULT_SYS_TRACE_FILE_NAME, 0);
        //Trace.endSection();
    }
}
