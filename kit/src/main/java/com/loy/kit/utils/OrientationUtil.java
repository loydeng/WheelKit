package com.loy.kit.utils;

import android.view.OrientationEventListener;

import com.loy.kit.Utils;
import com.loy.kit.log.SdkLog;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author loy
 * @tiem 2023/2/26 15:00
 * @des
 */
public class OrientationUtil {
    public static final int ORIENTATION_0 = 0;
    public static final int ORIENTATION_90 = 90;
    public static final int ORIENTATION_180 = 180;
    public static final int ORIENTATION_270 = 270;

    private int useCount;
    private final AtomicInteger mOrientation;
    private OrientationEventListener mOrientationEventListener;

    private OrientationUtil() {
        useCount = 0;
        mOrientation = new AtomicInteger(0);
    }

    private static class Holder {
        public static final OrientationUtil INSTANCE = new OrientationUtil();
    }

    public static OrientationUtil getInstance() {
        return Holder.INSTANCE;
    }

    public int getCameraOrientation() {
        // 手机中心至底边中心的线与垂线夹角. 顺时针转的方向.
        //  |----180----|
        //  |           |
        //  |           |
        // 90   手机    270
        //  |   /|      |
        //  | /  |      |
        //  |----0------|
        /*int cameraOrientation = ORIENTATION_0;
        int orientation = mOrientation.get();
        if (orientation > 45 && orientation <= 135) {
            cameraOrientation = ORIENTATION_90;
        } else if (orientation > 135 && orientation <= 225) {
            cameraOrientation = ORIENTATION_180;
        } else if (orientation > 225 && orientation <= 315) {
            cameraOrientation = ORIENTATION_270;
        }*/
        return (mOrientation.get() + 45) / 90 * 90;
    }

    public int getOrientation() {
        return mOrientation.get();
    }

    public void startOrientationListener() {
        synchronized (OrientationUtil.class) {
            if (++useCount == 1) {
                if (mOrientationEventListener == null) {
                    mOrientationEventListener = new OrientationEventListener(Utils.getAppContext()) {
                        @Override
                        public void onOrientationChanged(int orientation) {
                            mOrientation.set(orientation);
                        }
                    };
                }
                if (mOrientationEventListener.canDetectOrientation()) {
                    mOrientationEventListener.enable();
                } else {
                    SdkLog.e(this.getClass().getSimpleName(), "no sensor to detect orientation");
                }
            }
        }
    }

    public void stopOrientationListener() {
        synchronized (OrientationUtil.class) {
            if (useCount == 0) {
                return;
            }
            if (--useCount == 0) {
                if (mOrientationEventListener != null && mOrientationEventListener.canDetectOrientation()) {
                    mOrientationEventListener.disable();
                } else {
                    SdkLog.e(this.getClass().getSimpleName(), "no sensor to detect orientation");
                }
            }
        }
    }

/*
    private DisplayManager displayManager;
    private DisplayManager.DisplayListener mDisplayListener;
    public void startDisplayListener(Context context) {
        if (displayManager == null) {
            displayManager = (DisplayManager) context.getSystemService(Activity.DISPLAY_SERVICE);
        }
        if (mDisplayListener == null) {
            mDisplayListener = new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {

                }

                @Override
                public void onDisplayRemoved(int displayId) {

                }

                @Override
                public void onDisplayChanged(int displayId) {
                    int rotation = displayManager.getDisplay(displayId).getRotation();
                    mOrientation.set(rotation);
                }
            };
        }
        displayManager.registerDisplayListener(mDisplayListener, null);
    }

    public void stopDisplayListener() {
        if (displayManager != null) {
            displayManager.unregisterDisplayListener(mDisplayListener);
        }
    }
*/

}
