package com.loy.kit.log.core;

/**
 * 抽象加工日志格式的方式, 如美化格式, 日期格式, 堆栈等
 * @author Loy
 * @time 2022/8/26 14:20
 * @des
 */
public interface Format {
    String log(Config config, @Level int level, String tag, String message);
}
