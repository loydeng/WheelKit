package com.loy.kit.log.core;

/**
 * @author Loy
 * @time 2022/8/30 17:20
 * @des
 */
public class FileAdapter extends Adapter {
    public FileAdapter() {
        mFormat = new PlainFormat();
        mPrinter = new FilePrinter();
    }
}
