package com.loy.kit.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.loy.kit.R;
import com.loy.kit.Utils;


public class DialogUtil {

    /**
     * @param tip             提示信息
     * @param confirmCallback 确认的回调，取消默认隐藏
     */
    public static void showConfirmDialog(String tip, ConfirmCallback confirmCallback) {
        showConfirmDialog(Utils.currentActivity(), tip, confirmCallback);
    }

    public static void showConfirmDialog(Context context , String tip, ConfirmCallback confirmCallback) {
        showConfirmDialog(context, tip, null, confirmCallback, null);
    }

    public static void showConfirmDialog(Context context, String tip, String content, ConfirmCallback confirmCallback, CancelCallback cancelCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(tip)
                .setPositiveButton(ResourceUtil.getString(R.string.confirm), (dialog, which) -> {
                    if (confirmCallback != null) {
                        confirmCallback.onConfirmClick();
                    }
                })
                .setNegativeButton(ResourceUtil.getString(R.string.cancel), (dialog, which) -> {
                    if (cancelCallback != null) {
                        cancelCallback.onCancelClick();
                    }
                    dialog.dismiss();
                })
                .setCancelable(false);
        if (EmptyUtil.isStringNotEmpty(content)) {
            builder.setMessage(content);
        }
        builder.create().show();
    }

    public interface ConfirmCallback {
        void onConfirmClick();
    }

    public interface CancelCallback {
        void onCancelClick();
    }

    public interface EditDialogCallback {
        void onEditFinish(String info);
    }

    public static void showEditDialog(String title, EditDialogCallback editDialogCallback) {
        Context context = Utils.currentActivity();
        final EditText inputServer = new EditText(context);
        new AlertDialog.Builder(context).setTitle(title).setView(inputServer)
                                        .setNegativeButton(ResourceUtil.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).setPositiveButton(ResourceUtil.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String text = inputServer.getText().toString();
                editDialogCallback.onEditFinish(text);
            }
        }).show();
    }

    public static void showSelectDialog(String title, String[] selectTitles, int checkedItem, SingleSelectCallback callback) {
        Context context = Utils.currentActivity();
        if (checkedItem < 0 || checkedItem > selectTitles.length - 1) {
            checkedItem = -1;// 之前没有选中
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (EmptyUtil.isStringNotEmpty(title)) {
            builder.setTitle(title);
        }
        builder.setSingleChoiceItems(selectTitles, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null) {
                    callback.selected(which, selectTitles[which]);
                }
                dialog.dismiss();
            }
        })
               .setNegativeButton(ResourceUtil.getString(R.string.cancel), null)
               .show();
    }

    public interface SingleSelectCallback {
        void selected(int index, String title);
    }

    private static ProgressDialog sWaitDialog;

    public static void showWaitDialog(String msg) {
        hideWaitDialog();
        sWaitDialog = new ProgressDialog(Utils.currentActivity());
        sWaitDialog.setIndeterminate(false);
        sWaitDialog.setCancelable(false);
        sWaitDialog.setMessage(msg);
        sWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        sWaitDialog.show();
        //ProgressBar progressBar = new ProgressBar(Utils.currentActivity());
        //progressBar.setIndeterminate(false);

    }

    public static boolean hideWaitDialog() {
        if (sWaitDialog != null) {
            sWaitDialog.dismiss();
            sWaitDialog = null;
            return true;
        }
        return false;
    }
}