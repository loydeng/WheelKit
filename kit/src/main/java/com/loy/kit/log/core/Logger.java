package com.loy.kit.log.core;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.ColorInt;

import com.loy.kit.constants.Constants;
import com.loy.kit.log.ILog;
import com.loy.kit.log.SdkLog;
import com.loy.kit.utils.FileIOUtil;
import com.loy.kit.utils.ThreadUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Loy
 * @time 2022/8/26 14:39
 * @des
 */
public abstract class Logger implements ILog {
    private static final int JSON_INDENT = 4;
    private static final int XML_INDENT = 4;

    public static final int V = Log.VERBOSE;
    public static final int D = Log.DEBUG;
    public static final int I = Log.INFO;
    public static final int W = Log.WARN;
    public static final int E = Log.ERROR;
    public static final int A = Log.ASSERT;

    private static final String[] LEVELS = {"V", "D", "I", "W", "E", "A"};

    private static final int[] COLORS = {
            Color.rgb(187, 187, 187),
            Color.rgb(0, 248, 248),
            Color.rgb(26, 255, 0),
            Color.rgb(213, 231, 47),
            Color.rgb(255, 107, 104),
            Color.rgb(255, 3, 0)
    };

    public static String Level(@Level int level) {
        return LEVELS[level - V];
    }

    // 根据等级定义, 返回显示的颜色
    public static @ColorInt int Color(@Level int level) {
        return COLORS[level - V];
    }

    private final Config mConfig;

    private final ArrayList<Adapter> mAdapters;

    public Logger() {
        this(new Config());
    }

    public Logger(Config config) {
        this.mConfig = config;
        mAdapters = new ArrayList<>();
        initAdapter(mAdapters);
    }

    abstract void initAdapter(ArrayList<Adapter> adapters);

    void log(@Level int level, String tag, String content) {
        if(mConfig.level > level){
            return;
        }
        for (Adapter adapter : mAdapters) {
            if (adapter instanceof ViewAdapter) { // ui
                ThreadUtil.runOnUIThread(()->{
                    adapter.log(mConfig, level, tag, content);
                });
            } else if (adapter instanceof FileAdapter || adapter instanceof CrashAdapter) { // io
                ThreadUtil.runOnIOThread(()->{
                    adapter.log(mConfig, level, tag, content);
                });
            } else {
                adapter.log(mConfig, level, tag, content);
            }
        }
    }

    public ViewPrinterProvider getViewPrinterProvider() {
        for (Adapter adapter : mAdapters) {
            if (adapter instanceof ViewAdapter && adapter.mPrinter instanceof ViewPrinter) {
                ViewPrinter viewPrinter = (ViewPrinter) adapter.mPrinter;
                return viewPrinter.getViewPrinterProvider();
            }
        }
        return null;
    }

    @Override
    public void v(String tag, String content) {
        log(V, tag, content);
    }

    @Override
    public void v(String content) {
        v(mConfig.tag, content);
    }

    @Override
    public void i(String tag, String content) {
        log(I, tag, content);
    }

    @Override
    public void i(String content) {
        i(mConfig.tag, content);
    }

    @Override
    public void d(String tag, String content) {
        log(D, tag, content);
    }

    @Override
    public void d(String content) {
        d(mConfig.tag, content);
    }

    @Override
    public void w(String tag, String content) {
        log(W, tag, content);
    }

    @Override
    public void w(String content) {
        w(mConfig.tag, content);
    }

    @Override
    public void e(String tag, String content) {
        log(E, tag, content);
    }

    @Override
    public void e(String content) {
        e(mConfig.tag, content);
    }

    public static StackTraceElement[] currentStackStrace() {
        return new Throwable().getStackTrace();
    }

    public static String getThrowableStringByLog(Throwable throwable) {
        return Log.getStackTraceString(throwable);
    }

    public static String getThrowableString(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        StackTraceElement[] croppedRealStackTrack = getCroppedRealStackTrack(stackTrace, Logger.class.getPackage().getName(), Constants.Log.STACK_TRACE_DEPTH);
        StringBuilder sb = new StringBuilder();

        sb.append(throwable.getClass().getName())
          .append(FileIOUtil.COLON)
          .append(FileIOUtil.SPACE)
          .append(throwable.getMessage())
          .append(FileIOUtil.LINE_SEP);

        int len = croppedRealStackTrack.length;
        for (int i = 0; i < len; i++) {
            sb.append(FileIOUtil.TAB)
              .append(i != len - 1? PrettyFormat.MIDDLE_CORNER : PrettyFormat.BOTTOM_CORNER)
              .append(croppedRealStackTrack[i].toString()).append(FileIOUtil.LINE_SEP);
        }
        return sb.toString();
    }

    public static String printStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder(128);
        if (stackTrace == null || stackTrace.length == 0) {
            return null;
        } else if (stackTrace.length == 1) {
            return "\t─ " + stackTrace[0].toString();
        } else {
            for (int i = 0, len = stackTrace.length; i < len; i++) {
                if (i == 0) {
                    sb.append("stackTrace:  \n");
                }
                if (i != len - 1) {
                    sb.append("\t├ ");
                    sb.append(stackTrace[i].toString());
                    sb.append("\n");
                } else {
                    sb.append("\t└ ");
                    sb.append(stackTrace[i].toString());
                }
            }
            return sb.toString();
        }
    }

    /**
     * Get the real stack trace and then crop it with a max depth.
     *
     * @param stackTrace the full stack trace
     * @param maxDepth   the max depth of real stack trace that will be cropped, 0 means no limitation
     * @return the cropped real stack trace
     */
    private static StackTraceElement[] getCroppedRealStackTrack(StackTraceElement[] stackTrace, String ignorePackage, int maxDepth) {
        return cropStackTrace(getRealStackTrack(stackTrace, ignorePackage), maxDepth);
    }

    /**
     * Get the real stack trace, all elements that come from XLog library would be dropped.
     *
     * @param stackTrace the full stack trace
     * @return the real stack trace, all elements come from system and library user
     */
    private static StackTraceElement[] getRealStackTrack(StackTraceElement[] stackTrace, String ignorePackage) {
        int ignoreDepth = 0;
        int allDepth = stackTrace.length;
        String className;
        for (int i = allDepth - 1; i >= 0; i--) {
            className = stackTrace[i].getClassName();
            if (ignorePackage != null && className.startsWith(ignorePackage)) {
                ignoreDepth = i + 1;
                break;
            }
        }
        int realDepth = allDepth - ignoreDepth;
        StackTraceElement[] realStack = new StackTraceElement[realDepth];
        System.arraycopy(stackTrace, ignoreDepth, realStack, 0, realDepth);
        return realStack;
    }

    /**
     * Crop the stack trace with a max depth.
     *
     * @param callStack the original stack trace
     * @param maxDepth  the max depth of real stack trace that will be cropped,
     *                  0 means no limitation
     * @return the cropped stack trace
     */
    private static StackTraceElement[] cropStackTrace(StackTraceElement[] callStack, int maxDepth) {
        int realDepth = callStack.length;
        if (maxDepth > 0) {
            realDepth = Math.min(maxDepth, realDepth);
        }
        StackTraceElement[] realStack = new StackTraceElement[realDepth];
        System.arraycopy(callStack, 0, realStack, 0, realDepth);
        return realStack;
    }

    public static String json(String json) {
        String formattedString = null;
        if (json == null || json.trim().length() == 0) {
            SdkLog.w("JSON empty.");
            return "";
        }
        try {
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                formattedString = jsonObject.toString(JSON_INDENT);
            } else if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                formattedString = jsonArray.toString(JSON_INDENT);
            } else {
                SdkLog.w("JSON should start with { or [");
                return json;
            }
        } catch (Exception e) {
            SdkLog.w(e.getMessage());
            return json;
        }
        return formattedString;
    }

    public String xml(String xml) {
        String formattedString;
        if (xml == null || xml.trim().length() == 0) {
            SdkLog.w("XML empty.");
            return "";
        }
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                                          String.valueOf(XML_INDENT));
            transformer.transform(xmlInput, xmlOutput);
            formattedString = xmlOutput.getWriter().toString().replaceFirst(">", ">"
                    + FileIOUtil.LINE_SEP);
        } catch (Exception e) {
            SdkLog.w(e.getMessage());
            return xml;
        }
        return formattedString;
    }
}
