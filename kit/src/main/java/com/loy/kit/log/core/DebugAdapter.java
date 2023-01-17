package com.loy.kit.log.core;

/**
 * @author Loy
 * @time 2022/8/30 17:30
 * @des
 */
public class DebugAdapter extends Adapter {
    public DebugAdapter() {
        mPrinter = new ConsolePrinter();
    }
}
