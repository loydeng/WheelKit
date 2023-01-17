package com.loy.kit.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.view.Choreographer;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.loy.kit.Utils;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class AppUtil {
    private static final int NONE_UID = -1;
    // app uid cache
    private static int sUid = NONE_UID;

    public static String getProcessName() {
        int pid = Process.myPid();
        ActivityManager activityManager = ServiceManagerUtil.getActivityManager();
        List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();
        if (runningApps != null) {
            for (ActivityManager.RunningAppProcessInfo info : runningApps) {
                if (pid == info.pid) {
                    return info.processName;
                }
            }
        }
        return null;
    }

    public static boolean isAppForeground() {
        ActivityManager manager = ServiceManagerUtil.getActivityManager();
        List<ActivityManager.RunningAppProcessInfo> infoList = manager.getRunningAppProcesses();
        if (infoList == null || infoList.size() == 0)
            return false;
        for (ActivityManager.RunningAppProcessInfo info : infoList) {
            if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return info.processName.equals(Utils.getAppContext().getPackageName());
            }
        }
        return false;
    }

    public static PackageInfo getPackageInfo() {
        Context context = Utils.getAppContext();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }

    public static String getPackageName() {
        return Utils.getAppContext().getPackageName();
    }

    public static String getVersionName() {
        PackageInfo pi = getPackageInfo();
        if (pi == null) {
            return null;
        } else {
            return pi.versionName;
        }
    }

    public static int getVersionCode() {
        PackageInfo pi = getPackageInfo();
        if (pi == null) {
            return -1;
        } else {
            return pi.versionCode;
        }
    }

    public static int getAppUid() {
        if (sUid == NONE_UID) {
            PackageManager packageManager = ServiceManagerUtil.getPackageManager();
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
                sUid = applicationInfo.uid;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return sUid;
    }

    public static Signature[] getAppSignatures() {
        return getAppSignatures(getPackageName());
    }

    public static Signature[] getAppSignatures(@NonNull String packageName) {
        try {
            PackageManager pm = ServiceManagerUtil.getPackageManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                if (pi == null)
                    return null;
                SigningInfo signingInfo = pi.signingInfo;
                if (signingInfo.hasMultipleSigners()) {
                    return signingInfo.getApkContentsSigners();
                } else {
                    return signingInfo.getSigningCertificateHistory();
                }
            } else {
                PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                if (pi == null)
                    return null;
                return pi.signatures;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void installApp(final File file) {
        Context appContext = Utils.getAppContext();

        Uri uri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        } else {
            String authority = appContext.getPackageName() + ".utilcode.provider";
            uri = FileProvider.getUriForFile(appContext, authority, file);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(uri, type);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        appContext.startActivity(intent);
    }

    public static String getSDKVersionName() {
        return Build.VERSION.RELEASE;
    }

    public static int getSDKVersionCode() {
        return Build.VERSION.SDK_INT;
    }

    // 监听屏幕刷新率 FPS
    public static boolean startMonitorFPS(FPSCallback callback) {
        Choreographer.FrameCallback frameCallback = FPSCallback.sCallbackMap.get(callback);
        if (frameCallback != null) {
            return false;
        }else {
            frameCallback = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) { // 当前这一帧的时间戳, 单位为纳秒
                    callback.addFrame(frameTimeNanos);
                    Choreographer.getInstance().postFrameCallback(this);
                }
            };
            FPSCallback.sCallbackMap.put(callback, frameCallback);
            Choreographer.getInstance().postFrameCallback(frameCallback);
            return true;
        }
    }

    public static boolean stopMonitorFPS(FPSCallback callback) {
        Choreographer.FrameCallback frameCallback = FPSCallback.sCallbackMap.remove(callback);
        boolean hasRegister = frameCallback != null;
        if (hasRegister) {
            Choreographer.getInstance().removeFrameCallback(frameCallback);
        }
        return hasRegister;
    }

    public static abstract class FPSCallback {
        private static final ConcurrentHashMap<FPSCallback, Choreographer.FrameCallback> sCallbackMap = new ConcurrentHashMap<>();
        // 16 ms send VSYNC semaphore to refresh view
        // calculate in nanos time, and callback in 160 ms (default 10 fps)
        public static final long FPS10 = 160 * 1000 * 1000;
        public static final long SECOND = 1000 * 1000 * 1000; // fps
        private long frames;
        private long startTime;
        private final long internal; // 帧率的计算与回调时间间隔, 默认 1s , 最小不低于 160 ms

        public FPSCallback() {
            this(SECOND);
        }

        public FPSCallback(long internal) {
            frames = 0;
            startTime = 0;
            this.internal = Math.max(internal, FPS10);
        }

        public void addFrame(long frameTimestamp) {
            frames++;
            if (startTime == 0) {
                startTime = frameTimestamp;
            } else {
                long gap = frameTimestamp - startTime;
                if (gap >= this.internal) {
                    double fps = ((double) frames * 1000 * 1000 / internal) * 1000;
                    startTime = 0;
                    frames = 0;
                    onResult(fps);
                }
            }
        }

        abstract void onResult(double fps);
    }
}
