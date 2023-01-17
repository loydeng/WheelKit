package com.loy.kit.log.core;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Loy
 * @time 2022/9/1 16:56
 * @des
 */
@IntDef({Logger.V, Logger.D, Logger.I, Logger.W, Logger.E, Logger.A})
@Retention(RetentionPolicy.SOURCE)
public @interface Level {
}
