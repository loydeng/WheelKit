package com.loy.kit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 一些标志性的注解, 可参考 androidx.annotation 包下的注解
 * 常用的, 运行线程的标注,
 * @see androidx.annotation.MainThread
 * @see androidx.annotation.WorkerThread
 * 资源 ID 类标准
 * @see androidx.annotation.IdRes
 * @see androidx.annotation.RawRes
 * @see androidx.annotation.ColorRes
 * @see androidx.annotation.ColorInt
 * 值是否可空
 * @see androidx.annotation.NonNull
 * @see androidx.annotation.Nullable
 * @author Loy
 * @time 2022/8/26 18:00
 * @des
 */
public interface Flag {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Async{}


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Sync{}


}
