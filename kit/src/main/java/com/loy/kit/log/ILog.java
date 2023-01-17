package com.loy.kit.log;

/**
 * 门面接口
 * @author Loy
 * @time 2022/8/18 17:48
 * @des
 */
public interface ILog {

    void v(String tag, String content);

    void v(String content);

    void i(String tag, String content);

    void i(String content);

    void d(String tag, String content);

    void d(String content);

    void w(String tag, String content);

    void w(String content);

    void e(String tag, String content);

    void e(String content);
}
