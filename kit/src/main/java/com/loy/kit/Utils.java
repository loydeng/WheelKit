package com.loy.kit;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;

import com.loy.kit.utils.CrashUtil;
import com.loy.kit.utils.EmptyUtil;

/**
 * @author Loy
 * @time 2022/8/18 16:34
 * @des
 */
public final class Utils {
    private static Application sApplication;
    private static ActivityObserver sActivityObserver;
    private static Config sConfig;

    private Utils() {
    }

    public static boolean isInit() {
        return sApplication != null;
    }


    public static void init(Config config) {
        Context context = config.getContext();
        if (context instanceof Application) {
            sApplication = (Application) context;
        } else if (context instanceof Activity) {
            Activity activity = (Activity) context;
            sApplication = activity.getApplication();
        } else if (context instanceof Service) {
            Service service = (Service) context;
            sApplication = service.getApplication();
        } else {
            throw new IllegalArgumentException(
                    "context must be Application or Activity or Service");
        }
        sActivityObserver = new ActivityObserver();

        sApplication.registerActivityLifecycleCallbacks(sActivityObserver);

        if (EmptyUtil.isNotNull(config)) {
            sConfig = config;
        }else {
            sConfig = new Config(sApplication);
        }
        if (sConfig.isEnableCrashHandler()) {
            CrashUtil.init();
        }
    }

    public static Context getAppContext() {
        if (sApplication == null) {
            throw new IllegalStateException("must call Utils init first");
        }
        return sApplication;
    }

    public static Activity currentActivity() {
        return sActivityObserver.currentActivity();
    }

    public static Config getConfig() {
        return sConfig;
    }

    public static void exit() {
        sApplication.unregisterActivityLifecycleCallbacks(sActivityObserver);
        sActivityObserver.finishAll();
    }
}
