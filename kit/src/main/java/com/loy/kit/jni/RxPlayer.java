package com.loy.kit.jni;

import android.os.Handler;
import android.os.HandlerThread;

import com.loy.kit.utils.EmptyUtil;
import com.loy.kit.utils.ThreadUtil;

public class RxPlayer {

    // Used to load the 'kit' library on application startup.
    static {
        System.loadLibrary("kit");
    }

    public interface Callback {
        // user Stop or Play finish
        void onLoad(boolean isLoading);

        void onStop(boolean isFinish);

        void onProgress(long current, long total);
    }

    private long mNativePlayer; // mNativePtr
    private Callback mCallback;
    //private HandlerThread worker;
    //private Handler mHandler;

    public RxPlayer(Callback callback) {
        mCallback = (callback == null ? EmptyUtil.getEmptyImpl(callback.getClass()) : callback);
        //worker = new HandlerThread("RxPlayer");
        //worker.start();
        //mHandler = new Handler(worker.getLooper());
        mNativePlayer = nativeNewPlayer(this);
    }

    public void release() {

        if (mNativePlayer > 0) {
            nativeReleasePlayer(mNativePlayer);
            mNativePlayer = 0;
        }

    }

    public void play(String url) {
        nativePlay(mNativePlayer, url);
    }

    public void stop() {
        nativeStop(mNativePlayer);
    }

    public boolean isPlaying() {
        return nativeIsPlaying(mNativePlayer);
    }

    public void record() {
        nativeRecord();
    }

    public void stopRecord() {
        nativeStopRecord();
    }

    public void onLoading(boolean isLoading) {
        ThreadUtil.runOnUIThread(() -> mCallback.onLoad(isLoading));
    }

    public void onPlayFinish(boolean isFinish) {
        ThreadUtil.runOnUIThread(() -> mCallback.onStop(isFinish));
    }

    public void onUpdateProgress(long current, long total) {
        long currentSecond = (current + 500) / 1000;
        long totalSecond = (total + 500) / 1000;
        ThreadUtil.runOnUIThread(() -> mCallback.onProgress(currentSecond, totalSecond));
    }

    public boolean isRecording() {
        return nativeIsRecording();
    }

    private static native long nativeNewPlayer(Object callback);

    private static native void nativeReleasePlayer(long ptr);

    private static native void nativePlay(long ptr, String url);

    private static native void nativeStop(long ptr);

    private static native boolean nativeIsPlaying(long ptr);

    private native void nativeRecord();

    private native void nativeStopRecord();

    private native boolean nativeIsRecording();

}