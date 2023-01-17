package com.loy.kit.utils;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

/**
 * @author Loy
 * @time 2021/12/13 15:37
 * @des
 */
public class SnackBarUtil {
    public static void showTextTips(View holder, String content) {
        Snackbar.make(holder,content, Snackbar.LENGTH_SHORT).show();
    }

    public static void showAction(View holder, String content, String buttonText, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(holder, content, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(buttonText, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(v);
                }
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}
