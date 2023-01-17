package com.loy.kit.log.core;

import com.loy.kit.constants.Constants;
import com.loy.kit.utils.DirUtil;
import com.loy.kit.utils.EmptyUtil;
import com.loy.kit.utils.FileIOUtil;
import com.loy.kit.utils.TimeUtil;

import java.io.File;

/**
 * 日志记录至文件中
 * @author Loy
 * @time 2022/8/26 14:52
 * @des
 */
public class FilePrinter implements Printer {

    // 控制单个文件的日志最大值 512kb, 避免日志过多
    public static final int MAX_SIZE = 512 * 1024;
    // 控制记录文件的最大数量, 优先删除最旧的日志文件
    // public static final int MAX_COUNT = 10;

    @Override
    public void log(Config config, int level, String tag, String message) {
        String content = message.endsWith(FileIOUtil.LINE_SEP) ? message : message + FileIOUtil.LINE_SEP;
        File file = getLogFile(config);
        FileIOUtil.writeStringToFile(file, content, true);
    }

    // 提供文件名的前缀, 后缀以当前日期加数字开始, 当文件达到限制大小后, 后缀数字会递增
    private File getLogFile(Config config) {
        String path = config.path;
        if (EmptyUtil.isStringEmpty(path)) {
            path = DirUtil.compatFileDir(Constants.Log.LOG_DIR);
        }

        String prefixName = config.filePrefix;
        if (EmptyUtil.isStringEmpty(prefixName)) {
            prefixName = Constants.Log.LOG_PREFIX;
        }

        String extension = config.fileExtension;
        if (EmptyUtil.isStringEmpty(extension)) {
            extension = Constants.Log.LOG_FILE_EXTENSION;
        }

        String time = TimeUtil.getDayNow();

        int newFileCount = 0;

        String format = prefixName + "_%s_%s." + extension;

        File newFile;
        File existingFile = null;

        newFile = new File(path, String.format(format, time, newFileCount));

        while (newFile.exists()) {
            existingFile = newFile;
            newFileCount++;
            newFile = new File(path, String.format(format, time, newFileCount));
        }

        if (existingFile != null) {
            if (existingFile.length() >= MAX_SIZE) {
                return newFile;
            }
            return existingFile;
        }

        return newFile;
    }
}
