package com.loy.kit.utils;


import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.loy.kit.Utils;
import com.loy.kit.log.SdkLog;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 集成 文件和流的常规操作工具类
 */
public final class FileIOUtil {

    public static final String FILE_SEP = System.getProperty("file.separator");

    public static final String LINE_SEP = System.getProperty("line.separator");

    public static final String SPACE = " ";

    public static final String TAB = "    ";

    public static final String COLON = ":";

    public static final char MINUS = '-';

    public static final char SLASH = '/';

    public static final char REV_SLASH = '\\';

    public static final String FILE_EXTENSION_SEPARATOR = ".";

    public static final String DEFAULT_CHARSET = "UTF-8";

    // 默认缓存字节数量 512 Kb
    private static int sBufferSize = 512 * 1024;

    // 提供设定缓存数量
    public static void setBufferSize(final int bufferSize) {
        sBufferSize = bufferSize;
    }

    // 较大文件读写时, 可能需要进度回调, 若不需要直接传 null
    public interface OnProgressUpdateListener {
        void onProgressUpdate(double progress);
    }

    // 由路径创建文件
    public static File getFileByPath(final String filePath) {
        return EmptyUtil.isStringSpace(filePath) ? null : new File(filePath);
    }

    // 判断文件或目录是否存在
    public static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }

    // 判断文件是否存在
    public static boolean isFileExist(File file) {
        if (EmptyUtil.isStringSpace(file.getAbsolutePath())) {
            return false;
        }
        return (file.exists() && file.isFile());
    }

    public static boolean isFileExist(String filePath) {
        return isFileExist(new File(filePath));
    }

    // 判断目录是否存在
    public static boolean isFolderExist(File folder) {
        if (EmptyUtil.isStringSpace(folder.getAbsolutePath())) {
            return false;
        }
        return (folder.exists() && folder.isDirectory());
    }

    // 获取文件的拓展名
    public static String getFileExtension(File file) {
        String extension = "";
        if (!isFileExist(file)) {
            return extension;
        }

        String filePath = file.getAbsolutePath();

        int extPosition = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosition = filePath.lastIndexOf(File.separator);
        if (extPosition == -1) {
            return extension;
        }
        return (filePosition >= extPosition) ? "" : filePath.substring(extPosition + 1);
    }

    // 获取文件名不包含拓展名,不能是目录,目录返回空串
    // 若要获得文件名包含拓展名,直接使用File.getName(),是目录时返回叶子目录名
    public static String getFileNameWithoutExtension(String filePath) {
        File file = new File(filePath);
        if (!isFileExist(file)) {
            return "";
        }
        String fileName = file.getName();
        int extPosition = fileName.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        if (extPosition > 0) {
            return file.getName().substring(0, extPosition);
        }
        return fileName;
    }

    // 获取文件名, 返回叶子目录名或文件名
    public static String getFileName(File file) {
        if (!isFileExists(file)) {
            return "";
        }
        return file.getName();
    }

    public static String getFileName(String filePath) {
        return getFileName(new File(filePath));
    }


    // 检测输出文件合法性, 判空 / 存在时是否为文件 / 不存在时是否需要创建父级目录
    public static boolean checkFileOrCreate(final File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) { // 存在则判断属性是否为文件
            return file.isFile();
        }
        if (!checkFolderOrCreate(file.getParentFile())) { // 不存在则先创建父目录
            return false;
        }
        // 下面的步骤可以省略, 只要父目录存在, 本文件不存在时输出流会自动创建文件并写入数据
        try {
            return file.createNewFile(); // 创建新的空文件
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 创建父目录
    private static boolean checkFolderOrCreate(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    public static boolean createFileByDeleteOldFile(File file) {
        if (file != null) {
            deleteFiles(file);
            return checkFileOrCreate(file);
        }
        return false;
    }

    // 删除文件或文件夹
    public static boolean deleteFiles(File file) {
        if (!isFileExists(file)) {
            return true;
        }

        if (file.isFile()) { //独立文件直接删除
            return file.delete();
        }

        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    f.delete();
                } else if (f.isDirectory()) {
                    deleteFiles(f);
                }
            }
        }
        return file.delete();
    }

    // 拷贝文件
    public static boolean copyFile(File srcFile, File destFile) {
        if (!isFileExist(srcFile)) {
            return false;
        }
        InputStream is = null;
        try {
            is = new FileInputStream(srcFile);
            return writeFileFromIS(destFile, is, false, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseUtil.closeIO(is);
        }
    }

    // 移动文件
    public static boolean moveFile(File srcFile, File destFile) {
        if (!isFileExist(srcFile)) {
            return false;
        }
        boolean rename = srcFile.renameTo(destFile);
        if (!rename) {
            if (copyFile(srcFile, destFile)) {
                return srcFile.delete();
            }
        }
        return rename;
    }

    // 获取文件大小
    public static long getSize(File file) {
        if (isFileExist(file)) {
            return file.length();
        }
        return 0;
    }

    public static boolean writeFileFromAsset(final File desFile, String filePath) {
        try {
            InputStream inputStream = Utils.getAppContext().getAssets().open(filePath);
            return writeFileFromIS(desFile, inputStream, false, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将输入流的数据写到指定文件
     *
     * @param file     数据输出的目标文件
     * @param is       数据来源的输入流
     * @param append   是否追加
     * @param listener 是否设定进度回调
     * @return 是否成功
     */
    public static boolean writeFileFromIS(final File file,
                                          final InputStream is,
                                          final boolean append,
                                          final OnProgressUpdateListener listener) {

        if (is == null || !checkFileOrCreate(file)) {
            SdkLog.e("FileIOUtils", "create file <" + file + "> failed.");
            return false;
        }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append), sBufferSize);
            if (listener == null) {
                byte[] data = new byte[sBufferSize];
                for (int len; (len = is.read(data)) != -1; ) {
                    os.write(data, 0, len);
                }
            } else {
                double totalSize = is.available();
                int curSize = 0;
                listener.onProgressUpdate(0);
                byte[] data = new byte[sBufferSize];
                for (int len; (len = is.read(data)) != -1; ) {
                    os.write(data, 0, len);
                    curSize += len;
                    listener.onProgressUpdate(curSize / totalSize);
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseUtil.closeIO(is);
            CloseUtil.closeIO(os);
        }
    }

    public static boolean writeFileFromIS(final String filePath,
                                          final InputStream is,
                                          final boolean append,
                                          final OnProgressUpdateListener listener) {
        return writeFileFromIS(getFileByPath(filePath), is, append, listener);
    }

    public static boolean writeFile(File file, InputStream is, boolean append) {
        if (is == null || !checkFileOrCreate(file)) {
            SdkLog.e("FileIOUtils", "create file <" + file + "> failed.");
            return false;
        }
        OutputStream o = null;
        try {
            o = new FileOutputStream(file, append);
            byte[] data = new byte[1024];
            int length = -1;
            while ((length = is.read(data)) != -1) {
                o.write(data, 0, length);
            }
            o.flush();
            return true;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            CloseUtil.closeIO(o);
            CloseUtil.closeIO(is);
        }
    }

    public static boolean writeFile(File file, ByteBuffer buffer, boolean append) {
        if (buffer == null || !checkFileOrCreate(file)) {
            SdkLog.e("FileIOUtils", "create file <" + file + "> failed.");
            return false;
        }
        FileOutputStream o = null;
        try {
            o = new FileOutputStream(file, append);
            o.getChannel().write(buffer);
            return true;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            CloseUtil.closeIO(o);
            buffer.clear();
        }
    }

    public static boolean writeStringToFile(File file, String str) {
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(file), DEFAULT_CHARSET);
            osw.write(str);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseUtil.closeIO(osw);
        }
    }

    public static boolean writeStringToFile(final String filePath, final String content, final boolean append) {
        return writeStringToFile(filePath, content, append, false);
    }

    public static boolean writeStringToFile(final String filePath, final String content,
                                            final boolean append, final boolean isOneLine) {
        return writeStringToFile(getFileByPath(filePath), content, append, isOneLine);
    }

    public static boolean writeStringToFile(final File file, final String content,
                                            final boolean append) {
        return writeStringToFile(file, content, append, false);
    }

    public static boolean writeStringToFile(final File file, final String content,
                                            final boolean append, final boolean isOneLine) {
        if (file == null || content == null) {
            return false;
        }
        if (!checkFileOrCreate(file)) {
            return false;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, append));
            bw.write(content);
            if ('\n' != content.charAt(content.length() - 1) && isOneLine) {
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseUtil.closeIO(bw);
        }
    }

    /**
     * 指定以字符集读取文件内容后返回
     *
     * @param file        数据的来源
     * @param charsetName 数据的字符集编码格式, 传 null时, 默认为utf-8
     * @return 文件中全部
     */
    public static String readFile2String(final File file, final String charsetName) {
        if (!isFileExist(file)) {
            return null;
        }
        BufferedReader reader = null;
        try {
            StringBuilder sb = new StringBuilder();

            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), EmptyUtil.isStringSpace(charsetName) ? DEFAULT_CHARSET : charsetName));

            String line;
            if ((line = reader.readLine()) != null) {
                sb.append(line);
                while ((line = reader.readLine()) != null) {
                    sb.append(LINE_SEP).append(line);
                }
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            CloseUtil.closeIO(reader);
        }
    }

    public static boolean writeListToFile(String filePath, List<String> list) {
        return writeListToFile(new File(filePath), list);
    }

    // 将字符串集合,以每个元素为单独一行写入到文件中
    public static boolean writeListToFile(File file, List<String> list) {
        if (list == null || list.size() == 0 || !checkFileOrCreate(file)) {
            return false;
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
            for (String s : list) {
                if ('\n' == s.charAt(s.length() - 1)) {
                    pw.print(s);
                } else {
                    pw.println(s);
                }
            }
            pw.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseUtil.closeIO(pw);
        }

        return true;
    }

    public static List<String> readFileToList(String filePath, String charsetName) {
        return readFileToList(new File(filePath), charsetName);
    }

    // 将文件中的字符串以每一行为单位读取到List<String>中返回, 每行字符串都不带换行符
    public static List<String> readFileToList(File file, String charsetName) {
        if (file == null || !file.isFile()) {
            return null;
        }
        List<String> fileContent = new ArrayList<>();
        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file),
                                                         EmptyUtil.isStringEmpty(charsetName) ? DEFAULT_CHARSET : charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {// 读取每一行字符串不包含换行字符
                fileContent.add(line);
            }
            return fileContent;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            CloseUtil.closeIO(reader);
        }
    }

    public static Uri file2Uri(final File file) {
        if (!isFileExists(file)) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = AppUtil.getPackageName() + ".utilcode.fileprovider";
            return FileProvider.getUriForFile(Utils.getAppContext(), authority, file);
        } else {
            return Uri.fromFile(file);
        }
    }
}
