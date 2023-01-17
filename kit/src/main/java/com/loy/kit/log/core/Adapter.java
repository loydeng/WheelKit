package com.loy.kit.log.core;

/**
 * 组合不同 format 和 printer
 *
 * @author Loy
 * @time 2022/8/26 14:28
 * @des
 */
public abstract class Adapter {
    protected Format mFormat;
    protected Printer mPrinter;

    public Adapter() {
    }

    public void log(Config config, @Level int level, String tag, String message) {
        String wrapperMsg = (mFormat == null ? message : mFormat.log(config, level, tag, message));
        mPrinter.log(config, level, tag, wrapperMsg);
    }
}
