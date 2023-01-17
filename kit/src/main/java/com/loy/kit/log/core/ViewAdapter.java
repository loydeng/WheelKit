package com.loy.kit.log.core;

/**
 * @author Loy
 * @time 2022/9/2 17:32
 * @des
 */
public class ViewAdapter extends Adapter {
    public ViewAdapter() {
        mFormat = new PlainFormat();
        mPrinter = new ViewPrinter();
    }
}
