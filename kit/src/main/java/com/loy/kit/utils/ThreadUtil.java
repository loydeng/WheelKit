package com.loy.kit.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.loy.kit.log.SdkLog;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    // 主线程 handler
    private static final Handler mMainHandler = new Handler(Looper.getMainLooper());

    // 检验是否在主线程
    public static void checkIsOnMainThread() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("Not on main thread!");
        }
    }

    // 检验是否运行在指定的线程
    public static class ThreadChecker {
        @Nullable
        private Thread thread;

        public static ThreadChecker newAndAttachCurrentThread() {
            return new ThreadChecker(Thread.currentThread()).attachThread();
        }

        public ThreadChecker(@Nullable Thread thread) {
            this.thread = thread;
        }

        public ThreadChecker attachThread() {
            thread = Thread.currentThread();
            return this;
        }

        public void checkIsOnValidThread() {
            if (thread == null) {
                SdkLog.w("ThreadChecker not attach thread");
                return;
            }
            Thread currentThread = Thread.currentThread();
            if (thread != currentThread) {
                throw new IllegalStateException("Wrong thread," + "attached thread id: " + thread.getId() + ",name:" + thread.getName() +
                        ",but currentThread id:" + currentThread.getId() + ",name:" + currentThread.getName());
            }
        }

        public void detachThread() {
            thread = null;
        }
    }

    public static class HandlerWithExceptionCallback extends Handler {
        private final static String TAG = "HandlerWithExceptionCallback";
        private final Runnable exceptionCallback;

        public HandlerWithExceptionCallback(Looper looper, Runnable exceptionCallback) {
            super(looper);
            this.exceptionCallback = exceptionCallback;
        }

        @Override
        public void dispatchMessage(Message msg) {
            try {
                super.dispatchMessage(msg);
            } catch (Exception e) {
                SdkLog.printErrorStack(TAG, "Exception on HandlerThread:" + Thread.currentThread().getName(), e);
                exceptionCallback.run();
                throw e;
            }
        }
    }

    // 阻塞接口
    public interface BlockingOperation {
        void run() throws InterruptedException;
    }

    // 执行不可打断的阻塞操作直至结束
    public static void executeUninterruptibly(BlockingOperation operation) {
        boolean wasInterrupted = false;
        while (true) {
            try {
                operation.run();
                break;
            } catch (InterruptedException e) {
                // 记录打断
                wasInterrupted = true;
            }
        }
        if (wasInterrupted) {
            // 标记打断信号
            Thread.currentThread().interrupt();
        }
    }

    // 不可打断地等待线程结束直至超时, 返回是否等到结束
    public static boolean joinUninterruptibly(final Thread thread, long timeoutMs) {
        final long startTimeMs = SystemClock.elapsedRealtime();
        long timeRemainingMs = timeoutMs;
        boolean wasInterrupted = false;
        while (timeRemainingMs > 0) {
            try {
                thread.join(timeRemainingMs);
                break;
            } catch (InterruptedException e) {
                // 记录打断
                wasInterrupted = true;
                final long elapsedTimeMs = SystemClock.elapsedRealtime() - startTimeMs;
                // 剩余等待时长
                timeRemainingMs = timeoutMs - elapsedTimeMs;
            }
        }

        if (wasInterrupted) {
            // 标记打断信号
            Thread.currentThread().interrupt();
        }
        return !thread.isAlive();
    }

    // 不可打断地等待线程结束
    public static void joinUninterruptibly(final Thread thread) {
        executeUninterruptibly(new BlockingOperation() {
            @Override
            public void run() throws InterruptedException {
                thread.join();
            }
        });
    }

    // 不可打断地等待通知
    public static void awaitUninterruptibly(final CountDownLatch latch) {
        executeUninterruptibly(new BlockingOperation() {
            @Override
            public void run() throws InterruptedException {
                latch.await();
            }
        });
    }

    // 不可打断地等待通知直至超时, 返回是否等到通知
    public static boolean awaitUninterruptibly(CountDownLatch barrier, long timeoutMs) {
        final long startTimeMs = SystemClock.elapsedRealtime();
        long timeRemainingMs = timeoutMs;
        boolean wasInterrupted = false;
        boolean result = false;
        do {
            try {
                result = barrier.await(timeRemainingMs, TimeUnit.MILLISECONDS);
                break;
            } catch (InterruptedException e) {
                wasInterrupted = true;
                final long elapsedTimeMs = SystemClock.elapsedRealtime() - startTimeMs;
                timeRemainingMs = timeoutMs - elapsedTimeMs;
            }
        } while (timeRemainingMs > 0);
        if (wasInterrupted) {
            Thread.currentThread().interrupt();
        }
        return result;
    }

    // 在指定线程(handler绑定的线程)执行操作, 并返回结果.
    public static <V> V invokeAtFrontUninterruptibly(final Handler handler, final Callable<V> callable) {
        if (handler.getLooper().getThread() == Thread.currentThread()) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        class Result {
            public V value;
            public Exception e;
        }
        final Result result = new Result();
        final CountDownLatch barrier = new CountDownLatch(1);
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    result.value = callable.call();
                } catch (Exception e) {
                    result.e = e;
                }
                barrier.countDown();
            }
        });
        awaitUninterruptibly(barrier);
        if (result.e != null) {
            throw new RuntimeException(result.e);
        }
        return result.value;
    }

    // 在指定线程(handler绑定的线程)执行操作
    public static void invokeAtFrontUninterruptibly(final Handler handler, final Runnable runner) {
        invokeAtFrontUninterruptibly(handler, new Callable<Void>() {
            @Override
            public Void call() {
                runner.run();
                return null;
            }
        });
    }

    public static void runOnUIThread(Runnable runnable) {
        // 非主线程, 基于主线程 Handler 执行接口
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            mMainHandler.post(runnable);
        } else {// 是主线程,直接执行
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
         *
         * @return 返回封装的结果对象, 提供给UI线程更新
         * @throws Exception
         */
        public abstract T doWork() throws Exception;

        /**
         * 运行在UI线程,
         *
         * @param result 执行耗时操作后的数据结果
         */
        public void runUI(T result) {
        }

        /**
         * 执行耗时操作时,出现异常的回调
         *
         * @param throwable 抛出的异常
         */
        public void onError(Throwable throwable) {
        }
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

    public interface AsyncCall<T> {
        interface Callback<T> {
            void success(T t);

            void failure(String errInfo);
        }

        void call(Callback<T> callback);
    }

}
