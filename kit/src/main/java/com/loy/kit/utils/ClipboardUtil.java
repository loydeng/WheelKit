package com.loy.kit.utils;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;

import com.loy.kit.Utils;

/**
 * @author Loy
 * @time 2021/12/13 19:11
 * @des
 */
public class ClipboardUtil {

    /**
     * 将文本复制到剪切板
     * @param text 待复制的文本
     */
    public static void copyText(CharSequence text) {
        ClipboardManager cm = ServiceManagerUtil.getClipboardManager();
        cm.setPrimaryClip(ClipData.newPlainText(AppUtil.getPackageName(),text));
    }

    /**
     * 将文本复制到剪切板
     * @param label 标签
     * @param text 待复制文本
     */
    public static void copyText(final CharSequence label, final CharSequence text) {
        ClipboardManager cm = ServiceManagerUtil.getClipboardManager();
        cm.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    /**
     * 清空剪切板内容
     */
    public static void clear() {
        ClipboardManager cm = ServiceManagerUtil.getClipboardManager();
        cm.setPrimaryClip(ClipData.newPlainText(null,""));
    }

    public static String getLabel() {
        ClipboardManager cm = ServiceManagerUtil.getClipboardManager();
        ClipDescription clipDescription = cm.getPrimaryClipDescription();
        if (clipDescription == null) {
            return "";
        }
        CharSequence label = clipDescription.getLabel();
        if (label == null) {
            return "";
        }
        return label.toString();
    }

    /**
     * 获取剪切板文本
     * @return 剪切板内容
     */
    public static String getText() {
        ClipboardManager cm = ServiceManagerUtil.getClipboardManager();
        ClipData clipData = cm.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            CharSequence text = clipData.getItemAt(0).coerceToText(Utils.getAppContext());
            if (text != null) {
                return text.toString();
            }
        }
        return "";
    }

    /**
     * 添加剪切板内容监听器
     * @param listener 监听器
     */
    public static void addClipChangeListener(ClipboardManager.OnPrimaryClipChangedListener listener) {
        ClipboardManager cm = (ClipboardManager) Utils.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.addPrimaryClipChangedListener(listener);
    }

    /**
     * 移除剪切板内容监听器
     * @param listener 监听器
     */
    public static void removeClipChangeListener(ClipboardManager.OnPrimaryClipChangedListener listener) {
        ClipboardManager cm = (ClipboardManager) Utils.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.removePrimaryClipChangedListener(listener);
    }
}
