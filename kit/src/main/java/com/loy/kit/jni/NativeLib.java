package com.loy.kit.jni;

import com.loy.kit.log.SdkLog;
import com.loy.kit.utils.EmptyUtil;
import com.loy.kit.utils.ReflectUtil;
import com.loy.kit.utils.ThreadUtil;

public class NativeLib {

    // Used to load the 'kit' library on application startup.
    static {
        System.loadLibrary("kit");
    }

    public interface Callback {
        // user Stop or play finish
        void onStop();
        void onProgress(long current, long total);
    }

    public NativeLib(Callback callback) {
        mCallback = (callback == null ? EmptyUtil.getEmptyImpl(callback.getClass()) : callback);
    }

    private Callback mCallback;

    public void play() {
        nativePlay();
    }

    public void stop() {
        nativeStop();
    }

    public boolean isPlaying() {
        return nativeIsPlaying();
    }

    public void record() {
        nativeRecord();
    }

    public void stopRecord() {
        nativeStopRecord();
    }

    public void onPlayFinish() {
        ThreadUtil.runOnUIThread(()-> mCallback.onStop());
    }

    public void onUpdateProgress(long current, long total) {
        long currentSecond = (current + 500) / 1000;
        long totalSecond = (total + 500) / 1000;
        ThreadUtil.runOnUIThread(()-> mCallback.onProgress(currentSecond, totalSecond));
    }

    public boolean isRecording() {
        return nativeIsRecording();
    }

    private native void nativePlay();

    private native void nativeStop();

    private native boolean nativeIsPlaying();

    private native void nativeRecord();

    private native void nativeStopRecord();

    private native boolean nativeIsRecording();

}