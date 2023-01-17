package com.loy.kit.utils;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.loy.kit.Utils;
import com.loy.kit.constants.Constants;
import com.loy.kit.log.core.Logger;
import com.loy.kit.log.core.LoggerManager;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class CrashUtil {

    public static void init() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(final Thread thread, final Throwable th) {
                //1.如果是lopper持有的线程（即ui线程） 则另开线程记录崩溃信息  并锁死ui线程500ms等待写入完毕 并结束进程
                if (thread == Looper.getMainLooper().getThread()) {
                    Observable.create(new ObservableOnSubscribe<Object>() {
                        @Override
                        public void subscribe(ObservableEmitter<Object> e) throws Exception {
                            outputLog(th);
                        }
                    }).subscribeOn(Schedulers.io()).subscribe();
                    SystemClock.sleep(500);
                    killProcess();
                } else {
                    //2.如果是除ui线程之外的线程捕获到未处理异常（包括rxjava创建的主线程，以及rxjava、asynctask、usecase创建的子线程等）
                    //则先另起一个线程输出日志  然后转入主线程  锁死主线程500ms等待子线程写入完毕后  再结束进程
                    Observable.create(new ObservableOnSubscribe<Object>() {
                        @Override
                        public void subscribe(ObservableEmitter<Object> e) throws Exception {
                            outputLog(th);
                            e.onNext(1);
                        }
                    }).subscribeOn(Schedulers.io())
                              .observeOn(AndroidSchedulers.mainThread())
                              .subscribe(new Consumer<Object>() {
                                  @Override
                                  public void accept(Object o) throws Exception {
                                      SystemClock.sleep(500);
                                      killProcess();
                                  }
                              });
                }
            }
        });
    }

    private static void outputLog(Throwable th) {
        String lineSeparate = FileIOUtil.LINE_SEP;
        StringBuilder sb = new StringBuilder();
        PackageInfo packageInfo = AppUtil.getPackageInfo();
        String timeStampNow = TimeUtil.getTimeStampNow();
        // 收集 app 信息
        sb.append("appId: ").append(packageInfo.packageName).append(lineSeparate)
          .append("手机型号: ").append(Build.MODEL).append(lineSeparate)
          .append("系统版本: ").append(Build.VERSION.RELEASE).append(lineSeparate)
          .append("CPU arch: ").append(Build.CPU_ABI).append(lineSeparate)
          .append("versionCode: ").append(packageInfo.versionCode).append(lineSeparate)
          .append("versionName: ").append(packageInfo.versionName).append(lineSeparate)
          .append("crash time: ").append(timeStampNow).append(lineSeparate);
        // 收集崩溃堆栈
        sb.append("引起崩溃 ")
          .append(Logger.getThrowableString(th));
        String crashInfo = sb.toString();
        LoggerManager.getCrashLogger().e(crashInfo);
    }

    private static void killProcess() {
        Utils.exit();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }


}
