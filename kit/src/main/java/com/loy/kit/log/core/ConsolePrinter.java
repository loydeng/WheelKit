package com.loy.kit.log.core;

import static com.loy.kit.constants.Constants.Log.MAX_LEN;

import android.util.Log;

import com.loy.kit.utils.FileIOUtil;

/**
 * 输出到控制台, AS logcat
 *
 * @author Loy
 * @time 2022/8/26 14:47
 * @des
 */
public class ConsolePrinter implements Printer {

    @Override
    public void log(Config config, int level, String tag, String message) {
        String subStr = message;
        while (subStr.length() > MAX_LEN) { // 为确保全部输出则分次打印
            String unitMsg = subStr.substring(0, MAX_LEN);
            String[] lineMessages = unitMsg.split(FileIOUtil.LINE_SEP);
            for (String lineMessage : lineMessages) {
                Log.println(level, tag, lineMessage);
            }
            subStr = subStr.substring(MAX_LEN);
        }
        String[] lineMessages = subStr.split(FileIOUtil.LINE_SEP);
        for (String lineMessage : lineMessages) {
            Log.println(level, tag, lineMessage);
        }
    }
}
