package com.loy.kit.log.core;

/**
 * @author Loy
 * @time 2022/8/30 17:32
 * @des
 */
public class CrashAdapter extends Adapter {
    public CrashAdapter() {
        mFormat = new PrettyFormat();
        mPrinter = new FilePrinter();
    }
}
