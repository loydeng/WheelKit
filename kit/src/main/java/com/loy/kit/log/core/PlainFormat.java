package com.loy.kit.log.core;

import android.os.Process;

import com.loy.kit.utils.AppUtil;
import com.loy.kit.utils.FileIOUtil;
import com.loy.kit.utils.TimeUtil;

/**
 * @author Loy
 * @time 2022/9/2 14:51
 * @des
 */
public class PlainFormat implements Format {

    @Override
    public String log(Config config, int level, String tag, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(TimeUtil.getTimeStampNow()).append(FileIOUtil.SPACE)
          .append(Process.myPid()).append(FileIOUtil.MINUS).append(Thread.currentThread().getId())
          .append(FileIOUtil.SLASH).append(AppUtil.getPackageName()).append(FileIOUtil.SPACE)
          .append(Logger.Level(level)).append(FileIOUtil.SLASH).append(tag).append(FileIOUtil.COLON).append(FileIOUtil.SPACE)
          .append(message).append(FileIOUtil.LINE_SEP)
        ;
        return sb.toString();
    }
}
