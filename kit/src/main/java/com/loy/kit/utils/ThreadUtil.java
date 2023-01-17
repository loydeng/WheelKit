package com.loy.kit.utils;

import android.os.Handler;
import android.os.Looper;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Loy
 * @time 2021/3/22 11:06
 * @des
 */
public class ThreadUtil {
    private static final Handler mMainHandler = new Handler(Looper.getMainLooper());

    public static void runOnUIThread(Runnable runnable){
        // 非主线程, 基于主线程 Handler 执行接口
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            mMainHandler.post(runnable);
        }else {// 是主线程,直接执行
            runnable.run();
        }
    }

    public static void postDelayOnUI(Runnable runnable, long delay) {
        mMainHandler.postDelayed(runnable, delay);
    }

    public static void runOnBackground(Runnable runnable) {
        Single.create(e -> {
            runnable.run();
        }).subscribeOn(Schedulers.newThread()).subscribe();
    }

    public static void runOnIOThread(Runnable runnable) {
        Single.create(e -> {
            runnable.run();
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public static void runOnComputeThread(Runnable runnable) {
        Single.create(e -> {
            runnable.run();
        }).subscribeOn(Schedulers.computation()).subscribe();
    }

    // 耗时操作在工作线程, 传递数据给 UI 线程 更新
    public static <T> void doWork(Worker<T> worker) {
        Disposable subscribe = Single.create((SingleOnSubscribe<T>) e -> {
            T result = worker.doWork();
            if (result == null) {
                return;
            }
            e.onSuccess(result);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    worker.runUI(result);
                }, throwable -> {
                    worker.onError(throwable);
                });
    }

    public abstract static class Worker<T> {
        /**
         * 执行耗时操作, 运行在子线程
         * @return 返回封装的结果对象, 提供给UI线程更新
         * @throws Exception
         */
        public abstract T doWork() throws Exception;

        /**
         * 运行在UI线程,
         * @param result 执行耗时操作后的数据结果
         */
        public void runUI(T result){}

        /**
         * 执行耗时操作时,出现异常的回调
         * @param throwable 抛出的异常
         */
        public void onError(Throwable throwable){}
    }


    public static <T> T asyncToSyncCall(AsyncCall<T> asyncCall) {
        Single<T> single = Single.create(singleEmitter -> asyncCall.call(new AsyncCall.Callback<T>() {
            @Override
            public void success(T t) {
                singleEmitter.onSuccess(t);
            }

            @Override
            public void failure(String errInfo) {
                singleEmitter.onError(new Exception(errInfo));
            }
        }));
        return single.blockingGet();
    }

    public interface AsyncCall<T>{
        interface Callback<T>{
            void success(T t);

            void failure(String errInfo);
        }
        void call(Callback<T> callback);
    }

}
