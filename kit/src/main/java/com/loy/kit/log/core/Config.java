package com.loy.kit.log.core;

import com.loy.kit.Utils;

/**
 * @author Loy
 * @time 2022/8/26 15:35
 * @des
 */
public class Config {
    String tag;
    @Level int level;
    int lineMaxLen;
    String path;
    String filePrefix;
    String fileExtension;
    String uploadUrl;
    int stackTraceDepth;

    public Config() {
        this.tag = Utils.getConfig().getGlobalTag();
        this.level = Utils.getConfig().getLevel();
        this.path = Utils.getConfig().getLogDir();
        this.filePrefix = Utils.getConfig().getLogPrefix();
        this.stackTraceDepth = Utils.getConfig().getStackTraceDepth();
        this.fileExtension = Utils.getConfig().getLogFileExtension();
        this.uploadUrl = Utils.getConfig().getUploadUrl();
    }

    public Config setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public Config setLevel(@Level int level) {
        this.level = level;
        return this;
    }

    public Config setLineMaxLen(int lineMaxLen) {
        this.lineMaxLen = lineMaxLen;
        return this;
    }

    public Config setPath(String path) {
        this.path = path;
        return this;
    }

    public Config setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
        return this;
    }

    public Config setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
        return this;
    }

    public Config setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
        return this;
    }
}
