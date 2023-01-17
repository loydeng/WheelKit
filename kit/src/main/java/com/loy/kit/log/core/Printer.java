package com.loy.kit.log.core;

/**
 * 抽象日志输出的目标, 如文件, 控制台等
 * @author Loy
 * @time 2022/8/26 14:20
 * @des
 */
public interface Printer {
    void log(Config config, @Level int level, String tag, String message);
}
