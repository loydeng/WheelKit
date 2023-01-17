package com.loy.kit;

public class NativeLib {

    // Used to load the 'kit' library on application startup.
    static {
        System.loadLibrary("kit");
    }

    /**
     * A native method that is implemented by the 'kit' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}