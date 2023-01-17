package com.loy.kit.log;

import android.util.Log;

import com.loy.kit.BuildConfig;
import com.loy.kit.log.core.LoggerManager;

/**
 * @author Loy
 * @time 2022/8/26 12:25
 * @des
 */
public class SdkLog {

    public static void i(String content) {
        LoggerManager.getDebugLogger().i(content);
    }

    public static void i(String tag, String content) {
        LoggerManager.getDebugLogger().i(tag, content);
    }

    public static void d(String content) {
        LoggerManager.getDebugLogger().d(content);
    }

    public static void d(String tag, String content) {
        LoggerManager.getDebugLogger().d(tag, content);
    }

    public static void d(String tag, String prefix, String content) {
        d(tag, prefix + " " + content);
    }

    public static void w(String content) {
        LoggerManager.getDebugLogger().w(content);
    }

    public static void w(String tag, String content) {
        LoggerManager.getDebugLogger().w(tag, content);
    }

    public static void e(String content) {
        LoggerManager.getDebugLogger().e(content);
    }

    public static void e(String tag, String content) {
        LoggerManager.getDebugLogger().e(tag, content);
    }

    public static void e(String tag, String prefix, String content) {
        e(tag, prefix + " " + content);
    }

    public static void color(String content) {
        if (BuildConfig.DEBUG) {
            e(content);
        }else {
            d(content);
        }
    }

    public static void color(String tag, String content) {
        if (BuildConfig.DEBUG) {
            e(tag, content);
        }else {
            d(tag, content);
        }
    }

    public static void color(String tag, String prefix, String content) {
        if (BuildConfig.DEBUG) {
            e(tag, prefix, content);
        }else {
            d(tag, prefix, content);
        }
    }

    public static void printErrorStack(Throwable th) {
        String stackTraceString = Log.getStackTraceString(th);
        e(stackTraceString);
    }

    public static void printCurrentThreadStack() {
        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stackTrace = currentThread.getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append("Thread name:" + currentThread.getName()
                          + " ,tid:" + currentThread.getId() + " ,stackInfo:\n");
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append("\tat " + stackTraceElement + "\n");
        }
        i(sb.toString());
    }
}
