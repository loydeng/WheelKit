package com.loy.kit.utils;

import android.app.ProgressDialog;
import android.content.Context;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ProgressDialogUtil {
    private static ProgressDialog mProgressDialog;
    private static final int DEFAULT_DELAY = 500;
    private static AtomicBoolean mCanceled = new AtomicBoolean(false);

    public static void showProgressSimpleDialog(Context context, String str) {
        showProgressSimpleDialog(context, str, DEFAULT_DELAY);
    }

    public static void showProgressSimpleDialog(Context context, String str, int delay) {
        mCanceled = new AtomicBoolean(false);

        Single.timer(delay, TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aLong -> {
                  if (mCanceled.get()) {
                      return;
                  }
                  if (mProgressDialog == null) {
                      mProgressDialog = new ProgressDialog(context);
                      mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                      mProgressDialog.setIndeterminate(true);
                      mProgressDialog.setMessage(str);
                      mProgressDialog.setCanceledOnTouchOutside(false);
                      mProgressDialog.setCancelable(false);
                  }
                  mProgressDialog.show();
              });
    }


    public static void closeProgressSimpleDialog() {
        mCanceled.set(true);
        Single.create(e -> {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        })
              .subscribeOn(AndroidSchedulers.mainThread())
              .subscribe();
    }
}
