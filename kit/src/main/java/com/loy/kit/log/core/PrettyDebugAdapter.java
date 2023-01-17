package com.loy.kit.log.core;

/**
 * @author Loy
 * @time 2022/9/2 18:09
 * @des
 */
public class PrettyDebugAdapter extends Adapter{
    public PrettyDebugAdapter() {
        mFormat = new PrettyFormat();
        mPrinter = new ConsolePrinter();
    }
}
