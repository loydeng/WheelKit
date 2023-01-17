package com.loy.kit.tool;

import android.os.Looper;
import android.util.Log;
import android.util.LogPrinter;

import com.loy.kit.log.SdkLog;

/**
 * 用于监控主线程卡顿
 * @author loyde
 * @des
 * @time 2022/12/11 13:35
 */
public class MainThreadMonitor {
    public static final String TAG = "MainThreadMonitor";
    private static final long DEFAULT_THRESHOLD_TIME = 500;
    private static long sThresholdTime = DEFAULT_THRESHOLD_TIME;
    private static boolean isStart = false;
    private static long sStartTime = 0;

    public interface BlockCallback{
        void onBlock(long startTime, long endTime, StackTraceElement[] stackTrace);
    }

    public static void monitorBlock(BlockCallback callback){
        monitorBlock(DEFAULT_THRESHOLD_TIME, callback);
    }

    // 实现可参考: https://github.com/markzhai/AndroidPerformanceMonitor
    public static void monitorBlock(long blockTime, BlockCallback callback) {
        sThresholdTime = blockTime;
        Looper.getMainLooper().setMessageLogging(new LogPrinter(Log.DEBUG, TAG) {
            @Override
            public void println(String x) {
                if (!isStart) { // 开始消息处理
                    sStartTime = System.currentTimeMillis();
                    isStart = true;
                }else {// 结束消息处理
                    long endTime = System.currentTimeMillis();
                    long useTime = endTime - sStartTime;
                    if (useTime >= sThresholdTime) {
                        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                        SdkLog.color(TAG, stackTrace.toString());
                        if (callback != null) {
                            callback.onBlock(sStartTime, endTime, stackTrace);
                        }
                    }
                    isStart = false;
                }
            }
        });
    }

    public static void stopMonitorBlock() {
        sThresholdTime = DEFAULT_THRESHOLD_TIME;
        Looper.getMainLooper().setMessageLogging(null);
    }
}
