package com.loy.kit;

import android.content.Context;

import com.loy.kit.constants.Constants;
import com.loy.kit.log.core.Level;
import com.loy.kit.utils.DirUtil;

/**
 * @author Loy
 * @time 2022/9/5 10:52
 * @des
 */
public class Config {
    private final Context context;
    private String globalTag;
    @Level
    private int level;
    private boolean enableCrashHandler;
    private String logDir;
    private String logPrefix;
    private String crashDir;
    private String crashPrefix;
    int stackTraceDepth;
    private String logFileExtension;
    private String uploadUrl;

    public Config(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
        this.globalTag = Constants.Log.GLOBlE_TAG;
        this.level = Constants.Log.LEVEL;
        this.enableCrashHandler = true;
        this.logDir = DirUtil.compatFileDir(context, Constants.Log.LOG_DIR);
        this.logPrefix = Constants.Log.LOG_PREFIX;
        this.crashDir = DirUtil.compatFileDir(context, Constants.Log.CRASH_DIR);
        this.crashPrefix = Constants.Log.CRASH_PREFIX;
        this.stackTraceDepth = Constants.Log.STACK_TRACE_DEPTH;
        this.logFileExtension = Constants.Log.LOG_FILE_EXTENSION;
        this.uploadUrl = Constants.Log.UPLOAD_URL;
    }

    public Context getContext() {
        return context;
    }

    public String getGlobalTag() {
        return globalTag;
    }

    public Config setGlobalTag(String globalTag) {
        this.globalTag = globalTag;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public Config setLevel(int level) {
        this.level = level;
        return this;
    }

    public boolean isEnableCrashHandler() {
        return enableCrashHandler;
    }

    public Config setEnableCrashHandler(boolean enableCrashHandler) {
        this.enableCrashHandler = enableCrashHandler;
        return this;
    }

    public String getLogDir() {
        return logDir;
    }

    public Config setLogDir(String logDir) {
        this.logDir = logDir;
        return this;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public Config setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
        return this;
    }

    public String getCrashDir() {
        return crashDir;
    }

    public Config setCrashDir(String crashDir) {
        this.crashDir = crashDir;
        return this;
    }

    public String getCrashPrefix() {
        return crashPrefix;
    }

    public Config setCrashPrefix(String crashPrefix) {
        this.crashPrefix = crashPrefix;
        return this;
    }

    public int getStackTraceDepth() {
        return stackTraceDepth;
    }

    public Config setStackTraceDepth(int stackTraceDepth) {
        this.stackTraceDepth = stackTraceDepth;
        return this;
    }

    public String getLogFileExtension() {
        return logFileExtension;
    }

    public Config setLogFileExtension(String logFileExtension) {
        this.logFileExtension = logFileExtension;
        return this;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public Config setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
        return this;
    }
}
