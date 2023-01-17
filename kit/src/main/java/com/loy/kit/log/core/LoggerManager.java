package com.loy.kit.log.core;

import com.loy.kit.Utils;
import com.loy.kit.constants.Constants;
import com.loy.kit.utils.DirUtil;

import java.util.ArrayList;

/**
 * @author Loy
 * @time 2022/9/2 17:03
 * @des
 */
public abstract class LoggerManager {
    private LoggerManager() {}

    private static class DebugLogger {
        private static final Logger INSTANCE = new Logger() {
            @Override
            void initAdapter(ArrayList<Adapter> adapters) {
                adapters.add(new DebugAdapter());
            }
        };
    }

    private static class FileLogger{
        private static final Logger INSTANCE = new Logger() {
            @Override
            void initAdapter(ArrayList<Adapter> adapters) {
                adapters.add(new FileAdapter());
            }
        };
    }

    private static class CrashLogger {
        private static final Logger INSTANCE = new Logger(
                new Config().setPath(Utils.getConfig().getCrashDir())
                            .setFilePrefix(Utils.getConfig().getCrashPrefix())
        ) {
            @Override
            void initAdapter(ArrayList<Adapter> adapters) {
                adapters.add(new PrettyDebugAdapter());
                adapters.add(new CrashAdapter());
            }
        };
    }

    private static class ViewLogger {
        private static final Logger INSTANCE = new Logger() {
            @Override
            void initAdapter(ArrayList<Adapter> adapters) {
                adapters.add(new ViewAdapter());
            }
        };
    }

    public static Logger getDebugLogger() {
        return DebugLogger.INSTANCE;
    }

    public static Logger getFileLogger() {
        return FileLogger.INSTANCE;
    }

    public static Logger getCrashLogger() {
        return CrashLogger.INSTANCE;
    }

    public static Logger getViewLogger() {
        return ViewLogger.INSTANCE;
    }
}
