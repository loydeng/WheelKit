package com.loy.kit.base;

import android.app.Application;

import androidx.multidex.MultiDex;

import com.loy.kit.Config;
import com.loy.kit.Utils;
import com.loy.kit.constants.Constants;

/**
 * @author Loy
 * @time 2022/8/18 17:49
 * @des
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);

        Config config = new Config(this).setGlobalTag(Constants.Log.GLOBlE_TAG);

        Utils.init(config);
    }
}
